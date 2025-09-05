package rats;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import rats.entity.RatEntity;
import rats.item.ModItems;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main implements ModInitializer {
    public static final EntityType<RatEntity> RAT = Registry.register(
            Registries.ENTITY_TYPE,
            id("rat"),
            EntityType.Builder.create(RatEntity::new, SpawnGroup.MISC).dimensions(0.75f, 0.4f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("rat")))
    );

    public static Identifier id(String path) {
        return Identifier.of("rats", path);
    }

    @Override
    public void onInitialize() {
        ModItems.init();
        FabricDefaultAttributeRegistry.register(RAT, RatEntity.createMobAttributes());
        AtomicInteger cooldown = new AtomicInteger();
        ServerTickEvents.END_WORLD_TICK.register(world->{
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && cooldown.decrementAndGet() <= 0) {
                cooldown.set(1200);
                PlayerEntity playerEntity = world.getRandomAlivePlayer();
                if (playerEntity != null) {
                    Random random = world.random;
                    int i = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                    int j = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                    BlockPos blockPos = playerEntity.getBlockPos().add(i, 0, j);
                    if (world.isRegionLoaded(blockPos.getX() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getZ() + 10)) {
                        if (SpawnRestriction.isSpawnPosAllowed(RAT, world, blockPos)) {
                            if (world.getStructureAccessor().getStructureContaining(blockPos, s->true).hasChildren()) {
                                List<RatEntity> list = world.getNonSpectatingEntities(RatEntity.class, new Box(blockPos).expand(20));
                                for (int k = 0; k < 3 - list.size(); k++) {
                                    RatEntity ratEntity = RAT.create(world, SpawnReason.NATURAL);
                                    if (ratEntity != null) {
                                        ratEntity.initialize(world, world.getLocalDifficulty(blockPos), SpawnReason.NATURAL, null);

                                        ratEntity.refreshPositionAndAngles(blockPos, 0.0F, 0.0F);
                                        world.spawnEntity(ratEntity);
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
