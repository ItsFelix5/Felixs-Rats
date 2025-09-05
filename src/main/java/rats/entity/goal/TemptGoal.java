package rats.entity.goal;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TemptGoal extends Goal {
    private final TargetPredicate predicate;
    protected final MobEntity mob;
    protected PlayerEntity closestPlayer;

    public TemptGoal(MobEntity mob, Predicate<ItemStack> temptItemPredicate) {
        this.mob = mob;
        this.predicate = TargetPredicate.createNonAttackable().ignoreVisibility().setBaseMaxDistance(32)
                .setPredicate((entity, world) -> temptItemPredicate.test(entity.getMainHandStack()) || temptItemPredicate.test(entity.getOffHandStack()));
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.closestPlayer = getServerWorld(this.mob).getClosestPlayer(this.predicate, this.mob);
        return this.closestPlayer != null;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.closestPlayer, this.mob.getMaxHeadRotation() + 20, this.mob.getMaxLookPitchChange());
        if (this.mob.squaredDistanceTo(this.closestPlayer) < 6.25) this.mob.getNavigation().stop();
        else this.mob.getNavigation().startMovingTo(this.closestPlayer, 1);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
