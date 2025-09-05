package rats.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rats.Main;
import rats.entity.RatEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RatBundleItem extends Item {
    public RatBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("Use on a rat to pick up (shift for all)").setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        textConsumer.accept(Text.literal("Shift + click on a block to release").setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        textConsumer.accept(Text.literal("Click air with bundle in offhand to throw (shift for all)").setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        HashMap<MutableText, Integer> rats = new HashMap<>();
        stack.get(ModItems.RATS_COMPONENT).forEach(rat->{
            MutableText text = rat.name().orElse(Text.of("Rat")).copy();
            rat.ownerName().ifPresent(name->text.append(Text.literal(" (").append(name).append(")")
                    .setStyle(Style.EMPTY.withColor(Colors.DARK_GRAY))));
            rats.compute(text, (k, v) -> v == null ? 1 : v + 1);
        });
        rats.forEach((text, count) -> {
            if(count > 1) text.append(" x" + count);
            textConsumer.accept(text);
        });
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof RatEntity ratEntity) || !(entity.getWorld() instanceof ServerWorld serverWorld)) return ActionResult.PASS;
        List<RatEntity> rats;
        if(user.isSneaking()) rats = serverWorld.getEntitiesByClass(RatEntity.class, entity.getBoundingBox().expand(16.0, 8.0, 16.0), rat->rat.isOwner(user));
        else if (ratEntity.isOwner(user)) rats = List.of(ratEntity);
        else return ActionResult.PASS;

        List<RatData> data = new ArrayList<>(stack.get(ModItems.RATS_COMPONENT));
        rats.forEach(rat->{
            if (data.size() > 1000) return;
            rat.dropInventory(serverWorld);
            rat.discard();

            data.add(rat.serialize());
        });

        stack.set(ModItems.RATS_COMPONENT, List.copyOf(data));
        user.setStackInHand(hand, stack);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getPlayer().isSneaking() || !(context.getWorld() instanceof ServerWorld serverWorld)) return ActionResult.PASS;
        ItemStack stack = context.getStack();
        List<RatData> rats = stack.get(ModItems.RATS_COMPONENT);
        stack.set(ModItems.RATS_COMPONENT, List.copyOf(new ArrayList<>(rats) {{removeLast();}}));

        Main.RAT.create(serverWorld, rat -> {
            rat.deserialize(rats.getLast());
            serverWorld.spawnEntity(rat);
        }, BlockPos.ofFloored(context.getHitPos()), SpawnReason.BUCKET, true, false);

        if(rats.size() <= 1) context.getPlayer().setStackInHand(context.getHand(), new ItemStack(Items.BUNDLE));
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld) || hand == Hand.MAIN_HAND) return ActionResult.PASS;
        ItemStack stack = user.getStackInHand(hand);
        List<RatData> rats = new ArrayList<>(stack.get(ModItems.RATS_COMPONENT));

        Vec3d velocity = user.getVelocity().add(user.getRotationVec(1)).multiply(3);
        if (user.isSneaking()) {
            rats.forEach(data -> Main.RAT.create(serverWorld, rat -> {
                rat.deserialize(data);
                serverWorld.spawnEntity(rat);
                rat.setVelocity(velocity);
            }, BlockPos.ofFloored(user.getEyePos()), SpawnReason.BUCKET, true, false));

            user.setStackInHand(hand, new ItemStack(Items.BUNDLE));
        } else {
            if(rats.size() <= 1) user.setStackInHand(hand, new ItemStack(Items.BUNDLE));
            else {
                rats.removeLast();
                stack.set(ModItems.RATS_COMPONENT, rats);
            }

            Main.RAT.create(serverWorld, rat -> {
                rat.deserialize(rats.getLast());
                serverWorld.spawnEntity(rat);
                rat.setVelocity(velocity);
            }, BlockPos.ofFloored(user.getEyePos()), SpawnReason.BUCKET, true, false);
        }

        return ActionResult.SUCCESS;
    }
}
