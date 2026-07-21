package rats.entity.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class BreedGoal extends Goal {
    private final RatEntity mob;
    private RatEntity target;

    public BreedGoal(RatEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.getMainHandItem().is(RatEntity.EATABLE) || mob.isPanicking() || mob.getAge() != 0) return false;
        List<RatEntity> list = mob.level()
                .getEntitiesOfClass(RatEntity.class, mob.getBoundingBox().inflate(8.0, 8.0, 8.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return true;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(target, 1F);
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget(target);
    }

    @Override
    public void tick() {
        if (mob.distanceToSqr(target) < 4) {
            mob.spawnChildFromBreeding((ServerLevel) mob.level(), target);
            mob.getMainHandItem().shrink(1);
            target = null;
        }
    }

    private boolean isValidTarget(RatEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isBaby() || entity.isPanicking() || mob == entity || entity.getAge() != 0) return false;
        return mob.getMainHandItem().is(RatEntity.EATABLE);
    }
}
