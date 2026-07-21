package rats.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import rats.Main;

public class CreativeRatBundleItem extends Item {
    public CreativeRatBundleItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!(world instanceof ServerLevel serverWorld)) return InteractionResult.PASS;

        Main.RAT.create(serverWorld, rat -> {
            if (hand == InteractionHand.MAIN_HAND) rat.tame(user);
            serverWorld.addFreshEntity(rat);
            rat.setDeltaMovement(user.getViewVector(1));
        }, BlockPos.containing(user.getEyePosition()), EntitySpawnReason.BUCKET, true, false);

        return InteractionResult.SUCCESS;
    }
}
