package rats.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import rats.entity.RatEntity;

import java.util.EnumSet;

public class ReturnItemGoal extends Goal {
    private final RatEntity mob;

    public ReturnItemGoal(RatEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.getOwner() != null && mob.getMainHandItem().is(RatEntity.VALUABLE);
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(mob.getOwner(), 1);
    }
}
