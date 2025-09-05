package rats.entity.goal;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import rats.entity.RatEntity;

import java.util.EnumSet;

public class WorkGoal extends Goal {
    public enum TaskType { FARM, FOREST, MINE }

    private final RatEntity mob;
    private TaskType type;
    private int cooldown;
    private BlockPos target;
    private int tryingTime;

    public WorkGoal(RatEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (--cooldown > 0) return false;
        cooldown = 200;

        ItemStack stack = mob.getMainHandStack();
        if (stack.isOf(Items.GOLDEN_AXE)) type = TaskType.FOREST;
        else if (stack.isIn(ItemTags.PICKAXES)) type = TaskType.MINE;
        else if (stack.isEmpty() || (stack.isIn(RatEntity.EATABLE) && stack.getCount() < stack.getMaxCount())) type = TaskType.FARM;
        else return false;

        int maxRadius = type == TaskType.MINE ? 16 : 8;

        int minY = switch (type) {
            case FARM -> 0;
            case FOREST -> -1;
            case MINE -> -2;
        };
        int maxY = switch (type) {
            case FARM -> 1;
            case FOREST -> 5;
            case MINE -> 3;
        };
        for (int r = 1; r <= maxRadius; r++) {
            for (int y = minY; y <= maxY; y++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                        target = mob.getBlockPos().add(dx, y, dz);
                        if (isValidTarget()) return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        tryingTime = -1;
    }

    @Override
    public boolean shouldContinue() {
        return tryingTime <= 1200 && isValidTarget();
    }

    @Override
    public void tick() {
        World world = mob.getWorld();
        Vec3d mobPos = mob.getPos();

        if (target.getSquaredDistance(mobPos) > 9 || (type == TaskType.FOREST
                && target.getX() == mob.getBlockX() && target.getZ() == mob.getBlockZ()
                && Math.abs(target.getY() - mob.getBlockY()) < 10)) {
            if (tryingTime++ % 40 == 0)
                mob.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1);
        } else {
            BlockState state = world.getBlockState(target);

            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, target, Block.getRawIdFromState(state));
            Block.dropStacks(state, world, target, null, mob, mob.getMainHandStack());
            if (type == TaskType.FARM) {
                BlockState replanted = ((CropBlock) state.getBlock()).withAge(0);
                world.setBlockState(target, replanted, Block.NOTIFY_ALL);

                world.emitGameEvent(GameEvent.BLOCK_CHANGE, target, GameEvent.Emitter.of(mob, replanted));
            } else {
                mob.getMainHandStack().damage(
                        1,
                        (ServerWorld) world,
                        null,
                        item -> mob.sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND)
                );
                if (world.setBlockState(target, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL))
                    world.emitGameEvent(GameEvent.BLOCK_DESTROY, target, GameEvent.Emitter.of(mob, state));

                target = target.up();
            }

            cooldown = 0;
            if (isValidTarget() || canStart()) start();
            else tryingTime = 1200;
        }
    }

    private boolean isValidTarget() {
        BlockState state = mob.getWorld().getBlockState(target);

        return switch (type) {
            case FARM -> state.getBlock() instanceof CropBlock crop && crop.isMature(state);
            case FOREST -> state.isIn(BlockTags.LOGS) && isAccessible(target);
            case MINE -> state.isIn(ConventionalBlockTags.ORES) && isAccessible(target);
        };
    }

    private boolean isAccessible(BlockPos pos) {
        return mob.getNavigation().findPathTo(pos, 3) != null;
    }
}
