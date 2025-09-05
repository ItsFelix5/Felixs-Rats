package rats.entity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import rats.Client;
import rats.Main;

public class RatRenderer extends MobEntityRenderer<RatEntity, RatRenderState, RatModel> {
    public RatRenderer(EntityRendererFactory.Context context) {
        super(context, new RatModel(context.getPart(Client.RAT_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(RatRenderState state) {
        return Main.id("textures/entity/rat/rat.png");
    }

    @Override
    public RatRenderState createRenderState() {
        return new RatRenderState();
    }

    @Override
    public void updateRenderState(RatEntity entity, RatRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        itemModelResolver.updateForLivingEntity(state.itemState, entity.getMainHandStack(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
    }

    @Override
    public void render(RatRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (state.baby) matrices.scale(0.6f, 0.6f, 0.6f);
        if (!state.itemState.isEmpty()) {
            matrices.push();
            matrices.scale(0.7F, 0.7F, 0.7F);
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(state.bodyYaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrices.translate(0F, 0.2F, 0F);
            state.itemState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        super.render(state, matrices, vertexConsumers, light);
    }
}