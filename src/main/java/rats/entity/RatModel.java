package rats.entity;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;

public class RatModel extends EntityModel<RatRenderState> {
    public final ModelPart body;
    public final ModelPart backrightleg;
    public final ModelPart frontleftleg;
    public final ModelPart frontrightleg;
    public final ModelPart backleftleg;
    public final ModelPart head;
    public final ModelPart leftear;
    public final ModelPart rightear;
    public final ModelPart tail;

    public RatModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.backrightleg = this.body.getChild("backrightleg");
        this.frontleftleg = this.body.getChild("frontleftleg");
        this.frontrightleg = this.body.getChild("frontrightleg");
        this.backleftleg = this.body.getChild("backleftleg");
        this.head = this.body.getChild("head");
        this.leftear = this.head.getChild("leftear");
        this.rightear = this.head.getChild("rightear");
        this.tail = this.body.getChild("tail");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(1, 1).cuboid(-9.0F, -2.0F, -5.0F, 10.0F, 5.0F, 8.0F, new Dilation(-0.5F)), ModelTransform.of(-1.0F, 20.0F, 6.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData backrightleg = body.addChild("backrightleg", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 4.0F, 3.0F));

        backrightleg.addChild("cube_r1", ModelPartBuilder.create().uv(0, 58).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new Dilation(-0.5F)), ModelTransform.of(-1.0F, 0.0F, -1.0F, 0.0F, -1.2217F, 0.0F));

        ModelPartData frontleftleg = body.addChild("frontleftleg", ModelPartBuilder.create(), ModelTransform.origin(-9.0F, 4.0F, -6.0F));

        frontleftleg.addChild("cube_r2", ModelPartBuilder.create().uv(0, 52).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new Dilation(-0.5F)), ModelTransform.of(0.0F, 0.0F, 1.0F, 0.0F, 1.309F, 0.0F));

        ModelPartData frontrightleg = body.addChild("frontrightleg", ModelPartBuilder.create(), ModelTransform.origin(-7.9763F, 3.0F, 3.2164F));

        frontrightleg.addChild("cube_r3", ModelPartBuilder.create().uv(0, 58).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new Dilation(-0.5F)), ModelTransform.of(0.9763F, 1.0F, -1.2164F, 0.0F, -1.309F, 0.0F));

        ModelPartData backleftleg = body.addChild("backleftleg", ModelPartBuilder.create(), ModelTransform.origin(-1.0603F, 3.0F, -5.658F));

        backleftleg.addChild("cube_r4", ModelPartBuilder.create().uv(0, 52).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new Dilation(-0.5F)), ModelTransform.of(-1.9397F, 1.0F, 0.658F, 0.0F, 1.2217F, 0.0F));

        ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(1, 17).cuboid(-14.0F, -1.0F, -4.0F, 6.0F, 4.0F, 6.0F, new Dilation(-0.5F))
                .uv(19, 20).cuboid(-14.4F, 0.9F, -2.0F, 1.0F, 1.0F, 2.0F, new Dilation(-0.1F)), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData leftear = head.addChild("leftear", ModelPartBuilder.create(), ModelTransform.origin(2.0F, 2.0F, -7.0F));

        leftear.addChild("cube_r5", ModelPartBuilder.create().uv(0, 0).cuboid(1.0F, -2.0F, -1.3F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-13.0F, -2.0F, 2.8F, -0.0608F, -0.4323F, 0.1443F));

        ModelPartData rightear = head.addChild("rightear", ModelPartBuilder.create(), ModelTransform.origin(2.0F, 2.0F, -1.0F));

        rightear.addChild("cube_r6", ModelPartBuilder.create().uv(0, -2).cuboid(1.0F, -2.0F, -1.3F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-13.0F, -2.0F, 3.8F, 0.0608F, 0.4323F, 0.1443F));

        body.addChild("tail", ModelPartBuilder.create().uv(46, 0).cuboid(-2.25F, -2.0F, -0.75F, 7.0F, 2.0F, 2.0F, new Dilation(-0.5F)), ModelTransform.origin(2.0F, 2.0F, -1.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }
}