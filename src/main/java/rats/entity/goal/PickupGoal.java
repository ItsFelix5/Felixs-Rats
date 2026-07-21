package rats.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class PickupGoal extends Goal {
    private final RatEntity mob;
    private ItemEntity target;
    private int tryingTime;

    public PickupGoal(RatEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        List<ItemEntity> list = mob.level()
                .getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(16.0, 16.0, 16.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return mob.getNavigation().moveTo(target, 1F);
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget(target) && tryingTime < 120;
    }

    @Override
    public void tick() {
        tryingTime++;
    }

    private boolean isValidTarget(ItemEntity entity) {
        if (!entity.isAlive() || entity.hasPickUpDelay()) return false;
        ItemStack stack = entity.getItem();
        ItemStack held = mob.getMainHandItem();
        return (held.isEmpty() && (stack.is(RatEntity.USABLE) || stack.is(RatEntity.VALUABLE))) || (held.is(stack.getItem()) && held.getCount() < held.getMaxStackSize());
    }
}
