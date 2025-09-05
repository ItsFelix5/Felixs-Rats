package rats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import rats.entity.RatModel;
import rats.entity.RatRenderer;

public class Client implements ClientModInitializer {
    public static final EntityModelLayer RAT_LAYER = new EntityModelLayer(Main.id("rat"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Main.RAT, RatRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(RAT_LAYER, RatModel::getTexturedModelData);
    }
}
