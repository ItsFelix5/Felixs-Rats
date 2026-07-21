package rats.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import rats.Client;
import rats.Main;

public class RatRenderer extends MobRenderer<RatEntity, RatRenderState, RatModel> {
    public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel(context.bakeLayer(Client.RAT_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTextureLocation(RatRenderState state) {
        return Main.id("textures/entity/rat/rat.png");
    }

    @Override
    public RatRenderState createRenderState() {
        return new RatRenderState();
    }

    @Override
    public void extractRenderState(RatEntity entity, RatRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        itemModelResolver.updateForLiving(state.itemState, entity.getMainHandItem(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
    }

    @Override
    public void submit(RatRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.isBaby) matrices.scale(0.6f, 0.6f, 0.6f);
        if (!state.itemState.isEmpty()) {
            matrices.pushPose();
            matrices.scale(0.7F, 0.7F, 0.7F);
            matrices.mulPose(Axis.YN.rotationDegrees(state.bodyRot));
            matrices.mulPose(Axis.XP.rotationDegrees(90.0F));
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
            matrices.translate(0F, 0.2F, 0F);
            state.itemState.submit(matrices, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            matrices.popPose();
        }
        super.submit(state, matrices, collector, cameraState);
    }
}
