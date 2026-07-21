package rats.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rats.Main;
import rats.entity.RatEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RatBundleItem extends Item {
    public RatBundleItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.literal("Use on a rat to pick up (shift for all)").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        textConsumer.accept(Component.literal("Shift + click on a block to release").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        textConsumer.accept(Component.literal("Click air with bundle in offhand to throw (shift for all)").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        HashMap<MutableComponent, Integer> rats = new HashMap<>();
        stack.get(ModItems.RATS_COMPONENT).forEach(rat->{
            MutableComponent text = rat.name().orElse(Component.literal("Rat")).copy();
            rat.ownerName().ifPresent(name->text.append(Component.literal(" (").append(name).append(")")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))));
            rats.compute(text, (k, v) -> v == null ? 1 : v + 1);
        });
        rats.forEach((text, count) -> {
            if(count > 1) text.append(" x" + count);
            textConsumer.accept(text);
        });
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof RatEntity ratEntity) || !(entity.level() instanceof ServerLevel serverWorld)) return InteractionResult.PASS;
        List<RatEntity> rats;
        if(user.isShiftKeyDown()) rats = serverWorld.getEntitiesOfClass(RatEntity.class, entity.getBoundingBox().inflate(16.0, 8.0, 16.0), rat->rat.isOwnedBy(user));
        else if (ratEntity.isOwnedBy(user)) rats = List.of(ratEntity);
        else return InteractionResult.PASS;

        List<RatData> data = new ArrayList<>(stack.get(ModItems.RATS_COMPONENT));
        rats.forEach(rat->{
            if (data.size() > 1000) return;
            rat.dropEquipment(serverWorld);
            rat.remove(Entity.RemovalReason.DISCARDED);

            data.add(rat.serialize());
        });

        stack.set(ModItems.RATS_COMPONENT, List.copyOf(data));
        user.setItemInHand(hand, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown() || !(context.getLevel() instanceof ServerLevel serverWorld)) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        List<RatData> rats = stack.get(ModItems.RATS_COMPONENT);
        stack.set(ModItems.RATS_COMPONENT, List.copyOf(new ArrayList<>(rats) {{removeLast();}}));

        Main.RAT.create(serverWorld, rat -> {
            rat.deserialize(rats.getLast());
            serverWorld.addFreshEntity(rat);
        }, BlockPos.containing(context.getClickLocation()), EntitySpawnReason.BUCKET, true, false);

        if(rats.size() <= 1) context.getPlayer().setItemInHand(context.getHand(), new ItemStack(Items.BUNDLE));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!(world instanceof ServerLevel serverWorld) || hand == InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack stack = user.getItemInHand(hand);
        List<RatData> rats = new ArrayList<>(stack.get(ModItems.RATS_COMPONENT));

        Vec3 velocity = user.getDeltaMovement().add(user.getViewVector(1)).scale(3);
        if (user.isShiftKeyDown()) {
            rats.forEach(data -> Main.RAT.create(serverWorld, rat -> {
                rat.deserialize(data);
                serverWorld.addFreshEntity(rat);
                rat.setDeltaMovement(velocity);
            }, BlockPos.containing(user.getEyePosition()), EntitySpawnReason.BUCKET, true, false));

            user.setItemInHand(hand, new ItemStack(Items.BUNDLE));
        } else {
            if(rats.size() <= 1) user.setItemInHand(hand, new ItemStack(Items.BUNDLE));
            else {
                rats.removeLast();
                stack.set(ModItems.RATS_COMPONENT, rats);
            }

            Main.RAT.create(serverWorld, rat -> {
                rat.deserialize(rats.getLast());
                serverWorld.addFreshEntity(rat);
                rat.setDeltaMovement(velocity);
            }, BlockPos.containing(user.getEyePosition()), EntitySpawnReason.BUCKET, true, false);
        }

        return InteractionResult.SUCCESS;
    }
}
