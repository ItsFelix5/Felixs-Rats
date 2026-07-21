package rats.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class RatRenderState extends LivingEntityRenderState {
    public final ItemStackRenderState itemState = new ItemStackRenderState();
}
