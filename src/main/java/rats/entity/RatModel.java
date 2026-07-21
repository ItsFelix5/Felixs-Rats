package rats.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

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

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition body = modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-9.0F, -2.0F, -5.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-1.0F, 20.0F, 6.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition backrightleg = body.addOrReplaceChild("backrightleg", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 3.0F));

        backrightleg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 58).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -1.0F, 0.0F, -1.2217F, 0.0F));

        PartDefinition frontleftleg = body.addOrReplaceChild("frontleftleg", CubeListBuilder.create(), PartPose.offset(-9.0F, 4.0F, -6.0F));

        frontleftleg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 52).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 1.309F, 0.0F));

        PartDefinition frontrightleg = body.addOrReplaceChild("frontrightleg", CubeListBuilder.create(), PartPose.offset(-7.9763F, 3.0F, 3.2164F));

        frontrightleg.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 58).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.9763F, 1.0F, -1.2164F, 0.0F, -1.309F, 0.0F));

        PartDefinition backleftleg = body.addOrReplaceChild("backleftleg", CubeListBuilder.create(), PartPose.offset(-1.0603F, 3.0F, -5.658F));

        backleftleg.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 52).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-1.9397F, 1.0F, 0.658F, 0.0F, 1.2217F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 17).addBox(-14.0F, -1.0F, -4.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                .texOffs(19, 20).addBox(-14.4F, 0.9F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftear = head.addOrReplaceChild("leftear", CubeListBuilder.create(), PartPose.offset(2.0F, 2.0F, -7.0F));

        leftear.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -2.0F, -1.3F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -2.0F, 2.8F, -0.0608F, -0.4323F, 0.1443F));

        PartDefinition rightear = head.addOrReplaceChild("rightear", CubeListBuilder.create(), PartPose.offset(2.0F, 2.0F, -1.0F));

        rightear.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, -2).addBox(1.0F, -2.0F, -1.3F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -2.0F, 3.8F, 0.0608F, 0.4323F, 0.1443F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(46, 0).addBox(-2.25F, -2.0F, -0.75F, 7.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)), PartPose.offset(2.0F, 2.0F, -1.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }
}
