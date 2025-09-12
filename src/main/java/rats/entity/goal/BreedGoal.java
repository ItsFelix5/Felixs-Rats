package rats.entity.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class BreedGoal extends Goal {
    private final RatEntity mob;
    private RatEntity target;

    public BreedGoal(RatEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!mob.getMainHandStack().isIn(RatEntity.EATABLE) || mob.isPanicking() || mob.getBreedingAge() != 0) return false;
        List<RatEntity> list = mob.getWorld()
                .getEntitiesByClass(RatEntity.class, mob.getBoundingBox().expand(8.0, 8.0, 8.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return true;
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(target, 1F);
    }

    @Override
    public boolean shouldContinue() {
        return isValidTarget(target);
    }

    @Override
    public void tick() {
        if (mob.squaredDistanceTo(target) < 4) {
            mob.breed((ServerWorld) mob.getWorld(), target);
            mob.getMainHandStack().decrement(1);
            target = null;
        }
    }

    private boolean isValidTarget(RatEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isBaby() || entity.isPanicking() || mob == entity || entity.getBreedingAge() != 0) return false;
        return mob.getMainHandStack().isIn(RatEntity.EATABLE);
    }
}
