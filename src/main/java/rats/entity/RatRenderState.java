package rats.entity;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

public class RatRenderState extends LivingEntityRenderState {
    public final ItemRenderState itemState = new ItemRenderState();
}
