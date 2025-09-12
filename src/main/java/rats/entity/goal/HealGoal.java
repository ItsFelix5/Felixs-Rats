package rats.entity.goal;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.goal.Goal;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class HealGoal extends Goal {
    private final RatEntity mob;
    private RatEntity target;
    private int timer;

    public HealGoal(RatEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!mob.getMainHandStack().isIn(RatEntity.EATABLE)) return false;
        if (isValidTarget(mob)) {
            this.target = mob;
            return true;
        }
        List<RatEntity> list = mob.getWorld()
                .getEntitiesByClass(RatEntity.class, mob.getBoundingBox().expand(8.0, 8.0, 8.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return isValidTarget(target) && mob.getMainHandStack().isIn(RatEntity.EATABLE);
    }

    @Override
    public void tick() {
        if(mob != target) mob.getNavigation().startMovingTo(target, 1F);
        if (mob.squaredDistanceTo(target) < 4 && ++timer >= 20) {
            mob.getMainHandStack().decrement(1);
            target.heal(3);
            target.getWorld().sendEntityStatus(mob, EntityStatuses.ADD_BREEDING_PARTICLES);
            target.playEatSound();
            target = null;
            timer = 0;
            canStart();
        }
    }

    private boolean isValidTarget(RatEntity entity) {
        return entity != null && entity.isAlive() && entity.getHealth() < entity.getMaxHealth();
    }
}
