package kanade.kill.render;

import kanade.kill.entity.Lain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.Random;

public class ArmorLayer implements LayerRenderer<Lain> {
    private static final ResourceLocation WITHER_ARMOR = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final RenderLivingBase renderer;
    private final ModelPlayer model;

    public ArmorLayer(RenderLivingBase renderer) {
        this.renderer = renderer;
        this.model = (ModelPlayer) renderer.getMainModel();
    }


    @Override
    public void doRenderLayer(@Nonnull Lain entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.depthMask(true);
        this.renderer.bindTexture(WITHER_ARMOR);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float f = (float) entity.ticksExisted + partialTicks;
        float f1 = MathHelper.cos(f * 0.02F) * 3.0F;
        float f2 = f * 0.01F;
        GlStateManager.translate(f1, f2, 0.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.enableBlend();
        Random random = new Random();
        GlStateManager.color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setModelAttributes(this.renderer.getMainModel());
        Minecraft.getMinecraft().EntityRenderer.setupFogColor(true);
        this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        Minecraft.getMinecraft().EntityRenderer.setupFogColor(false);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
