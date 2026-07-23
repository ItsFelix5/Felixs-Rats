package rats.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import rats.Main;
import rats.entity.goal.*;
import rats.entity.goal.BreedGoal;
import rats.entity.goal.TemptGoal;
import rats.item.ModItems;
import rats.item.RatData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RatEntity extends TamableAnimal implements NeutralMob {
    public static final TagKey<Item> EATABLE = TagKey.create(Registries.ITEM, Main.id("rat_eatable"));
    public static final TagKey<Item> USABLE = TagKey.create(Registries.ITEM, Main.id("rat_usable"));
    public static final TagKey<Item> VALUABLE = TagKey.create(Registries.ITEM, Main.id("valuable"));
    private static final EntityDataAccessor<Long> DATA_ANGER_END_TIME = SynchedEntityData.defineId(RatEntity.class, EntityDataSerializers.LONG);
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private EntityReference<LivingEntity> persistentAngerTarget;

    public RatEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 0.2F)
                .add(Attributes.ATTACK_KNOCKBACK, 0.2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RunFromTntGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.2, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new RunFromAttackerGoal(this));
        this.goalSelector.addGoal(2, new HighLeapAtTargetGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new HealGoal(this));
        this.goalSelector.addGoal(4, new WorkGoal(this));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 32.0F, 32.0F));
        this.goalSelector.addGoal(6, new TemptGoal(this, stack -> stack.is(EATABLE)));
        this.goalSelector.addGoal(7, new BreedGoal(this));
        this.goalSelector.addGoal(8, new PickupGoal(this));
        this.goalSelector.addGoal(9, new ReturnItemGoal(this));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(EATABLE);
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANGER_END_TIME, 0L);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack playerStack = player.getItemInHand(hand);
        if(level().isClientSide() || playerStack.is(ModItems.RAT_BUNDLE) || playerStack.is(ModItems.CREATIVE_RAT_BUNDLE)) return InteractionResult.PASS;
        if (!isOwnedBy(player)) {
            if (playerStack.is(EATABLE)) {
                playerStack.consume(1, player);
                setInLove(player);
                tame(player);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (playerStack.is(Items.BUNDLE)) {
            if (!playerStack.get(DataComponents.BUNDLE_CONTENTS).isEmpty()) return InteractionResult.PASS;
            ItemStack stack = new ItemStack(ModItems.RAT_BUNDLE);
            List<RatData> data = new ArrayList<>();
            data.add(serialize());

            dropEquipment((ServerLevel) level());
            remove(Entity.RemovalReason.DISCARDED);

            if(player.isShiftKeyDown())
                level().getEntitiesOfClass(RatEntity.class, this.getBoundingBox().inflate(16.0, 8.0, 16.0), rat->rat.isOwnedBy(player)).forEach(rat->{
                        data.add(rat.serialize());
                        rat.dropEquipment((ServerLevel) level());
                        rat.remove(Entity.RemovalReason.DISCARDED);
                });

            stack.set(ModItems.RATS_COMPONENT, List.copyOf(data));
            player.setItemInHand(hand, stack);
        } else if (hand == InteractionHand.MAIN_HAND) {
            ItemStack ratStack = getMainHandItem();
            this.setItemInHand(InteractionHand.MAIN_HAND, playerStack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ratStack);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);

            ItemStack stack = getMainHandItem();
            if (getOwner() != null && distanceTo(getOwner()) < 5 && stack.is(VALUABLE)) {
                spawnAtLocation((ServerLevel) level(), stack).addDeltaMovement(getOwner().position().subtract(position()).scale(0.1));
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel world, Entity target) {
        if (super.doHurtTarget(world, target)) {
            target.invulnerableTime = 5;
            ItemStack stack = getMainHandItem();
            if (stack.is(Items.TNT)) {
                stack.shrink(1);
                PrimedTnt tntEntity = new PrimedTnt(world, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, this);
                tntEntity.setFuse(40);
                world.addFreshEntity(tntEntity);
                world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(this, GameEvent.PRIME_FUSE, target.position());
            }
            if (target instanceof LivingEntity livingEntity) {
                getActiveEffects().forEach(effect -> livingEntity.addEffect(effect, this));
            }
            return true;
        }
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!(damageSource.getEntity() instanceof LivingEntity target)) return;

        for (RatEntity rat : level().getEntitiesOfClass(RatEntity.class,
                new AABB(position(), position()).inflate(getAttributeValue(Attributes.FOLLOW_RANGE)),
                rat -> rat.getTarget() == null && rat.wantsToAttack(target, rat.getOwner())))
            rat.setTarget(target);
    }

    @Override
    public boolean shouldTryTeleportToOwner() {
        return onGround() && this.getOwner() != null && this.distanceToSqr(this.getOwner()) >= 2400;
    }

    public RatData serialize() {
        return new RatData(getHealth(), getAge(), Optional.ofNullable(getCustomName()),
                Optional.ofNullable(getOwnerReference()), Optional.ofNullable(getOwner()).map(Entity::getCustomName),
                getActiveEffects().stream().toList(), getAttributes().pack());
    }

    public void deserialize(RatData data) {
        setHealth(data.health());
        setAge(data.age());
        data.name().ifPresent(this::setCustomName);
        data.owner().ifPresent(owner->{
            this.setTame(true, true);
            this.setOwnerReference(owner);
        });
        data.effects().forEach(this::addEffect);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        this.addPersistentAngerSaveData(view);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        this.readPersistentAngerSaveData(this.level(), view);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public void dropEquipment(ServerLevel world) {
        ItemStack itemStack = getMainHandItem();
        if (!itemStack.isEmpty() && !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            this.spawnAtLocation(world, itemStack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.entityData.get(DATA_ANGER_END_TIME);
    }

    @Override
    public void setPersistentAngerEndTime(long angerEndTime) {
        this.entityData.set(DATA_ANGER_END_TIME, angerEndTime);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(ANGER_TIME_RANGE.sample(this.random));
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> angerTarget) {
        this.persistentAngerTarget = angerTarget;
    }

    @Nullable
    @Override
    public RatEntity getBreedOffspring(ServerLevel serverWorld, AgeableMob passiveEntity) {
        RatEntity other = (RatEntity) passiveEntity;
        RatEntity baby = Main.RAT.create(serverWorld, EntitySpawnReason.BREEDING);
        if (baby == null) return null;
        if (this.isTame()) baby.tame((Player) getOwner());
        else if (other.isTame()) baby.tame((Player) other.getOwner());
        baby.getAttributes().assignAllValues(this.getAttributes());

        return baby;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof RatEntity || target instanceof Ghast || target instanceof ArmorStand) return false;
        else if (target instanceof OwnableEntity tameable) return tameable.getOwner() != owner;
        return true;
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    public boolean wantsToPickUp(ServerLevel world, ItemStack stack) {
        return stack.is(VALUABLE) || stack.is(USABLE);
    }

    @Override
    protected void pickUpItem(ServerLevel world, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack held = getMainHandItem();
        if (held.isEmpty()) {
            setItemInHand(InteractionHand.MAIN_HAND, stack);
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
            take(itemEntity, stack.getCount());
        } else if (held.is(stack.getItem()) && held.getCount() < held.getMaxStackSize()) {
            int amount = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
            stack.shrink(amount);
            held.grow(amount);
            take(itemEntity, amount);
            if (stack.isEmpty()) itemEntity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public void playEatingSound() {
        this.level()
                .playSound(null, this, SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 1.0F, Mth.nextFloat(this.level().getRandom(), 0.8F, 1.2F));
    }
}
