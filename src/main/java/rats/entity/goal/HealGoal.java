package rats.entity.goal;

import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class HealGoal extends Goal {
    private final RatEntity mob;
    private RatEntity target;
    private int timer;

    public HealGoal(RatEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.getMainHandItem().is(RatEntity.EATABLE)) return false;
        if (isValidTarget(mob)) {
            this.target = mob;
            return true;
        }
        List<RatEntity> list = mob.level()
                .getEntitiesOfClass(RatEntity.class, mob.getBoundingBox().inflate(8.0, 8.0, 8.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget(target) && mob.getMainHandItem().is(RatEntity.EATABLE);
    }

    @Override
    public void tick() {
        if(mob != target) mob.getNavigation().moveTo(target, 1F);
        if (mob.distanceToSqr(target) < 4 && ++timer >= 20) {
            mob.getMainHandItem().shrink(1);
            target.heal(3);
            target.level().broadcastEntityEvent(mob, EntityEvent.LOVE_HEARTS);
            target.playEatingSound();
            target = null;
            timer = 0;
            canUse();
        }
    }

    private boolean isValidTarget(RatEntity entity) {
        return entity != null && entity.isAlive() && entity.getHealth() < entity.getMaxHealth();
    }
}
