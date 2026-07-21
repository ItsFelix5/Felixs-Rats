package rats.entity.goal;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
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
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0) return false;
        cooldown = 200;

        ItemStack stack = mob.getMainHandItem();
        if (stack.is(Items.GOLDEN_AXE)) type = TaskType.FOREST;
        else if (stack.is(ItemTags.PICKAXES)) type = TaskType.MINE;
        else if (stack.isEmpty() || (stack.is(RatEntity.EATABLE) && stack.getCount() < stack.getMaxStackSize())) type = TaskType.FARM;
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
                        target = mob.blockPosition().offset(dx, y, dz);
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
    public boolean canContinueToUse() {
        return tryingTime <= 1200 && isValidTarget();
    }

    @Override
    public void tick() {
        Level world = mob.level();
        Vec3 mobPos = mob.position();

        if (target.distSqr(BlockPos.containing(mobPos)) > 9 || (type == TaskType.FOREST
                && target.getX() == mob.getBlockX() && target.getZ() == mob.getBlockZ()
                && Math.abs(target.getY() - mob.getBlockY()) < 10)) {
            if (tryingTime++ % 40 == 0)
                mob.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1);
        } else {
            BlockState state = world.getBlockState(target);

            ((ServerLevel) world).levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, target, Block.getId(state));
            Block.dropResources(state, world, target, null, mob, mob.getMainHandItem());
            if (state.getBlock() instanceof CropBlock crop) {
                BlockState replanted = crop.getStateForAge(0);
                world.setBlock(target, replanted, Block.UPDATE_ALL);

                world.gameEvent(GameEvent.BLOCK_CHANGE, target, GameEvent.Context.of(mob, replanted));
            } else {
                mob.getMainHandItem().hurtAndBreak(
                        1,
                        (ServerLevel) world,
                        null,
                        item -> mob.onEquippedItemBroken(item, EquipmentSlot.MAINHAND)
                );
                if (world.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL))
                    world.gameEvent(GameEvent.BLOCK_DESTROY, target, GameEvent.Context.of(mob, state));

                target = target.above();
            }

            cooldown = 0;
            if (isValidTarget() || canUse()) start();
            else tryingTime = 1200;
        }
    }

    private boolean isValidTarget() {
        BlockState state = mob.level().getBlockState(target);

        return switch (type) {
            case FARM -> state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
            case FOREST -> state.is(BlockTags.LOGS) && isAccessible(target);
            case MINE -> state.is(ConventionalBlockTags.ORES) && isAccessible(target);
        };
    }

    private boolean isAccessible(BlockPos pos) {
        return mob.getNavigation().createPath(pos, 3) != null;
    }
}
