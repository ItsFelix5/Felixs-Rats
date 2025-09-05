package rats.entity;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import rats.Main;
import rats.entity.goal.*;
import rats.entity.goal.TemptGoal;
import rats.item.ModItems;
import rats.item.RatData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RatEntity extends TameableEntity implements Angerable {
    public static final TagKey<Item> EATABLE = TagKey.of(RegistryKeys.ITEM, Main.id("rat_eatable"));
    public static final TagKey<Item> USABLE = TagKey.of(RegistryKeys.ITEM, Main.id("rat_usable"));
    public static final TagKey<Item> VALUABLE = TagKey.of(RegistryKeys.ITEM, Main.id("valuable"));
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(RatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    @Nullable
    private UUID angryAt;

    public RatEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return AnimalEntity.createAnimalAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.4F)
                .add(EntityAttributes.MAX_HEALTH, 8.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.ATTACK_KNOCKBACK, 0.2);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new RunFromTntGoal(this));
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new TameableEntity.TameableEscapeDangerGoal(1.2, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(2, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(3, new HealGoal(this));
        this.goalSelector.add(4, new WorkGoal(this));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.0, 32.0F, 32.0F));
        this.goalSelector.add(6, new TemptGoal(this, stack -> stack.isIn(EATABLE)));
        this.goalSelector.add(7, new BreedGoal(this));
        this.goalSelector.add(8, new PickupGoal(this));
        this.goalSelector.add(9, new ReturnItemGoal(this));
        this.goalSelector.add(10, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(11, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(EATABLE);
    }

    @Override
    protected void tickCramming() {
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANGER_TIME, 0);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack playerStack = player.getStackInHand(hand);
        if(getWorld().isClient || playerStack.isOf(ModItems.RAT_BUNDLE) || playerStack.isOf(ModItems.CREATIVE_RAT_BUNDLE)) return ActionResult.PASS;
        if (!isOwner(player)) {
            if (playerStack.isIn(EATABLE)) {
                playerStack.decrementUnlessCreative(1, player);
                lovePlayer(player);
                setTamedBy(player);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        if (playerStack.isOf(Items.BUNDLE)) {
            if (!playerStack.get(DataComponentTypes.BUNDLE_CONTENTS).isEmpty()) return ActionResult.PASS;
            ItemStack stack = new ItemStack(ModItems.RAT_BUNDLE);
            List<RatData> data = new ArrayList<>();
            data.add(serialize());

            dropInventory((ServerWorld) getWorld());
            discard();

            if(player.isSneaking())
                getWorld().getEntitiesByClass(RatEntity.class, this.getBoundingBox().expand(16.0, 8.0, 16.0), rat->rat.isOwner(player)).forEach(rat->{
                        data.add(rat.serialize());
                        rat.dropInventory((ServerWorld) getWorld());
                        rat.discard();
                });

            stack.set(ModItems.RATS_COMPONENT, List.copyOf(data));
            player.setStackInHand(hand, stack);
        } else if (hand == Hand.MAIN_HAND) {
            ItemStack ratStack = getMainHandStack();
            this.setStackInHand(Hand.MAIN_HAND, playerStack);
            player.setStackInHand(Hand.MAIN_HAND, ratStack);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld) this.getWorld(), true);

            ItemStack stack = getMainHandStack();
            if (getOwner() != null && distanceTo(getOwner()) < 5 && stack.isIn(VALUABLE)) {
                dropStack((ServerWorld) getWorld(), stack).addVelocity(getOwner().getPos().subtract(getPos()).multiply(0.1));
                setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (super.tryAttack(world, target)) {
            ItemStack stack = getMainHandStack();
            if (stack.isOf(Items.TNT)) {
                stack.decrement(1);
                TntEntity tntEntity = new TntEntity(world, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, this);
                world.spawnEntity(tntEntity);
                world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(this, GameEvent.PRIME_FUSE, target.getPos());
            }
            if (target instanceof LivingEntity livingEntity) {
                getStatusEffects().forEach(effect -> livingEntity.addStatusEffect(effect, this));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldTryTeleportToOwner() {
        return isOnGround() && this.getOwner() != null && this.squaredDistanceTo(this.getOwner()) >= 2400;
    }

    public RatData serialize() {
        return new RatData(getHealth(), getBreedingAge(), Optional.ofNullable(getCustomName()),
                Optional.ofNullable(getOwnerReference()), Optional.ofNullable(getOwner()).map(Entity::getCustomName),
                getStatusEffects().stream().toList(), getAttributes().pack());
    }

    public void deserialize(RatData data) {
        setHealth(data.health());
        setBreedingAge(data.age());
        data.name().ifPresent(this::setCustomName);
        data.owner().ifPresent(owner->{
            this.setTamed(true, true);
            this.setOwner(owner);
        });
        data.effects().forEach(this::addStatusEffect);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.writeAngerToData(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.readAngerFromData(this.getWorld(), view);
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        System.out.println(source.getType().msgId());
        return source.isIn(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(world, source);
    }

    @Override
    public void dropInventory(ServerWorld world) {
        ItemStack itemStack = getMainHandStack();
        if (!itemStack.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            this.dropStack(world, itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME, angerTime);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Nullable
    public RatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        RatEntity other = (RatEntity) passiveEntity;
        RatEntity baby = Main.RAT.create(serverWorld, SpawnReason.BREEDING);
        if (baby == null) return null;
        if (this.isTamed()) baby.setTamedBy((PlayerEntity) getOwner());
        else if (other.isTamed()) baby.setTamedBy((PlayerEntity) other.getOwner());
        baby.getAttributes().setFrom(this.getAttributes());

        return baby;
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof RatEntity || target instanceof GhastEntity || target instanceof ArmorStandEntity) return false;
        else if (target instanceof Tameable tameable) return tameable.getOwner() != owner;
        return true;
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return stack.isIn(VALUABLE) || stack.isIn(USABLE);
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getStack();
        ItemStack held = getMainHandStack();
        if (held.isEmpty()) {
            setStackInHand(Hand.MAIN_HAND, stack);
            itemEntity.discard();
            sendPickup(itemEntity, stack.getCount());
        } else if (held.isOf(stack.getItem()) && held.getCount() < held.getMaxCount()) {
            int amount = Math.min(stack.getCount(), held.getMaxCount() - held.getCount());
            stack.decrement(amount);
            held.increment(amount);
            sendPickup(itemEntity, amount);
            if (stack.isEmpty()) itemEntity.discard();
        }
    }

    @Override
    public void playEatSound() {
        this.getWorld()
                .playSoundFromEntity(null, this, SoundEvents.ENTITY_GENERIC_EAT.value(), SoundCategory.NEUTRAL, 1.0F, MathHelper.nextBetween(this.getWorld().random, 0.8F, 1.2F));
    }
}
