package rats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import rats.entity.RatModel;
import rats.entity.RatRenderer;

public class Client implements ClientModInitializer {
    public static final ModelLayerLocation RAT_LAYER = new ModelLayerLocation(Main.id("rat"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Main.RAT, RatRenderer::new);

        ModelLayerRegistry.registerModelLayer(RAT_LAYER, RatModel::getTexturedModelData);
    }
}
