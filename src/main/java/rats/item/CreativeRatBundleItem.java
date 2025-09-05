package rats.item;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rats.Main;

public class CreativeRatBundleItem extends Item {
    public CreativeRatBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

        Main.RAT.create(serverWorld, rat -> {
            if (hand == Hand.MAIN_HAND) rat.setTamedBy(user);
            serverWorld.spawnEntity(rat);
            rat.setVelocity(user.getRotationVec(1));
        }, BlockPos.ofFloored(user.getEyePos()), SpawnReason.BUCKET, true, false);

        return ActionResult.SUCCESS;
    }
}
