package rats;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import rats.entity.RatEntity;
import rats.item.ModItems;

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
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel world = server.overworld();
            if(world.getGameRules().get(GameRules.SPAWN_MOBS) && cooldown.decrementAndGet() <= 0) {
                cooldown.set(12000);
                Player player = world.getRandomPlayer();
                if (player == null || player.isSpectator()) return;

                RandomSource random = world.getRandom();
                a: for (int i = 0; i < 5; i++) {
                    BlockPos anchor = player.blockPosition().offset((8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1), 0, (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));
                    if (!world.hasChunksAt(anchor.getX() - 10, anchor.getZ() - 10, anchor.getX() + 10, anchor.getZ() + 10)) continue;
                    BlockPos.MutableBlockPos pos = anchor.mutable().move(Direction.DOWN, 16);
                    int tries = 24;
                    while (true) {
                        if(tries == 0) continue a;

                        if(!world.getBlockState(pos).isAir()) pos.move(Direction.UP);
                        else {
                            BlockState block = world.getBlockState(pos.offset(0, -1, 0));
                            if(block.isAir()) pos.move(Direction.DOWN);
                            else if(block.isValidSpawn(world, pos, RAT)) break;
                            else continue a;
                        }
                    }
                    if (!inStructure(world, pos)) continue;
                    int amount = world.getEntities(EntityTypeTest.forClass(RatEntity.class), new AABB(pos).inflate(64), rat -> !rat.isTame()).size();
                    if(amount > 1) return;

                    for (int k = 0; k < 3 - amount; k++) {
                        BlockPos spawnPos = pos.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
                        if (!SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(world, spawnPos, RAT)) spawnPos = pos;

                        RatEntity rat = RAT.create(world, EntitySpawnReason.NATURAL);
                        if (rat == null) return;
                        rat.snapTo(spawnPos, random.nextFloat() * 360f, 0f);
                        rat.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.NATURAL, null);
                        world.addFreshEntity(rat);
                    }
                    return;
                }
            }
        });
    }

    private static boolean inStructure(ServerLevel world, BlockPos pos) {
        StructureManager structures = world.structureManager();
        for (Structure structure : structures.getAllStructuresAt(pos).keySet())
            if (structures.getStructureAt(pos, structure).isValid()) return true;
        return false;
    }
}
