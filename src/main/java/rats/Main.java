package rats;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import rats.entity.RatEntity;
import rats.item.ModItems;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main implements ModInitializer {
    public static final EntityType<RatEntity> RAT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id("rat"),
            EntityType.Builder.of(RatEntity::new, MobCategory.MISC).sized(0.75f, 0.4f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("rat")))
    );

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("rats", path);
    }

    @Override
    public void onInitialize() {
        ModItems.init();
        FabricDefaultAttributeRegistry.register(RAT, RatEntity.createMobAttributes());
        AtomicInteger cooldown = new AtomicInteger();
        ServerTickEvents.END_LEVEL_TICK.register(world->{
            if (world.getGameRules().get(GameRules.SPAWN_MOBS) && cooldown.decrementAndGet() <= 0) {
                cooldown.set(1200);
                Player player = world.getRandomPlayer();
                if (player != null) {
                    RandomSource random = world.getRandom();
                    int i = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                    int j = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                    BlockPos blockPos = player.blockPosition().offset(i, 0, j);
                    if (world.hasChunksAt(blockPos.getX() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getZ() + 10)) {
                        if (SpawnPlacements.isSpawnPositionOk(RAT, world, blockPos)) {
                            if (world.structureManager().hasAnyStructureAt(blockPos)) {
                                List<RatEntity> list = world.getEntities(EntityTypeTest.forClass(RatEntity.class), new AABB(blockPos).inflate(20), rat -> true);
                                for (int k = 0; k < 3 - list.size(); k++) {
                                    RatEntity ratEntity = RAT.create(world, EntitySpawnReason.NATURAL);
                                    if (ratEntity != null) {
                                        ratEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(blockPos), EntitySpawnReason.NATURAL, null);

                                        ratEntity.snapTo(blockPos, 0.0F, 0.0F);
                                        world.addFreshEntity(ratEntity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
