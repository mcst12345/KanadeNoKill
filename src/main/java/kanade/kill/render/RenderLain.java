package kanade.kill.render;

import kanade.kill.entity.Lain;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderLain extends RenderLiving<Lain> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("kanade", "textures/entity/lain.png");
    private final ModelPlayer lain;

    public RenderLain(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
        this.lain = new ModelPlayer(1.0f, true);
        this.layerRenderers.add(new ArmorLayer(this));
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull Lain entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(@Nonnull Lain entity, double x, double y, double z, float entityYaw, float partialTicks) {
        mainModel = lain;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public boolean shouldRender(@Nonnull Lain livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected void renderLeash(@Nonnull Lain entityLivingIn, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    public void doRenderShadowAndFire(@Nonnull Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }
}