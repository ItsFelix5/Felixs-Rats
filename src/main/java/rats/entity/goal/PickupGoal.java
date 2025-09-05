package rats.entity.goal;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import rats.entity.RatEntity;

import java.util.EnumSet;
import java.util.List;

public class PickupGoal extends Goal {
    private final RatEntity mob;
    private ItemEntity target;
    private int tryingTime;

    public PickupGoal(RatEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        List<ItemEntity> list = mob.getWorld()
                .getEntitiesByClass(ItemEntity.class, mob.getBoundingBox().expand(16.0, 16.0, 16.0), this::isValidTarget);
        if (list.isEmpty()) return false;
        target = list.getFirst();
        return mob.getNavigation().startMovingTo(target, 1F);
    }

    @Override
    public boolean shouldContinue() {
        return isValidTarget(target) && tryingTime < 120;
    }

    @Override
    public void tick() {
        tryingTime++;
    }

    private boolean isValidTarget(ItemEntity entity) {
        if (!entity.isAlive() || entity.cannotPickup()) return false;
        ItemStack stack = entity.getStack();
        ItemStack held = mob.getMainHandStack();
        return (held.isEmpty() && (stack.isIn(RatEntity.USABLE) || stack.isIn(RatEntity.VALUABLE))) || (held.isOf(stack.getItem()) && held.getCount() < held.getMaxCount());
    }
}
