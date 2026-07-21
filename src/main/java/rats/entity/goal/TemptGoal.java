package rats.entity.goal;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TemptGoal extends Goal {
    private final TargetingConditions predicate;
    protected final Mob mob;
    protected Player closestPlayer;

    public TemptGoal(Mob mob, Predicate<ItemStack> temptItemPredicate) {
        this.mob = mob;
        this.predicate = TargetingConditions.forNonCombat().ignoreLineOfSight().range(32)
                .selector((entity, world) -> temptItemPredicate.test(entity.getMainHandItem()) || temptItemPredicate.test(entity.getOffhandItem()));
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.closestPlayer = getServerLevel(this.mob).getNearestPlayer(this.predicate, this.mob);
        return this.closestPlayer != null;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.closestPlayer, this.mob.getMaxHeadYRot() + 20, this.mob.getMaxHeadXRot());
        if (this.mob.distanceToSqr(this.closestPlayer) < 6.25) this.mob.getNavigation().stop();
        else this.mob.getNavigation().moveTo(this.closestPlayer, 1);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
