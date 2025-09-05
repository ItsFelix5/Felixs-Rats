package rats.entity.goal;

import net.minecraft.entity.ai.goal.Goal;
import rats.entity.RatEntity;

import java.util.EnumSet;

public class ReturnItemGoal extends Goal {
    private final RatEntity mob;

    public ReturnItemGoal(RatEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return mob.getOwner() != null && mob.getMainHandStack().isIn(RatEntity.VALUABLE);
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(mob.getOwner(), 1);
    }
}
