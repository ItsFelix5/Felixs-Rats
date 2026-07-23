package rats.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rats.entity.RatEntity;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Redirect(
            method = "doSweepAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean rats$preventSweepingRats(Player player, Entity target) {
        return target instanceof RatEntity || player.isAlliedTo(target);
    }
}
