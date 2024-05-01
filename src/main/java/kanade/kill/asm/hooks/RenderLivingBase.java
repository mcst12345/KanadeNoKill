package kanade.kill.asm.hooks;

import kanade.kill.Config;
import kanade.kill.item.KillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SuppressWarnings("unused")
public class RenderLivingBase {
    private static float handleRotationFloat(EntityLivingBase livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }
    private static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }

    public static void doRender(net.minecraft.client.renderer.entity.RenderLivingBase<?> renderer, EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity instanceof EntityPlayer) {
            /*if(KillItem.inList(entity)){
                float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                float f1 = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                float f2 = f1 - f;
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float f8 = handleRotationFloat(entity, partialTicks);
                float f4 = 0.0625F;
                float f5 = 0.0F;
                float f6 = 0.0F;

                if (!entity.isRiding()) {
                    f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                    f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                    if (entity.isChild()) {
                        f6 *= 3.0F;
                    }

                    if (f5 > 1.0F) {
                        f5 = 1.0F;
                    }
                    f2 = f1 - f; // Forge: Fix MC-1207
                }
                renderSwing(renderer,entity, f6, f5, partialTicks, f8, f2, f7, f4);
            }*/
            return;
        }
        WorldClient world = Minecraft.getMinecraft().WORLD;
        if (Config.CrystalBeam) {
            for (EntityPlayer player : world.playerEntities) {
                if (KillItem.inList(player)) {
                    renderer.bindTexture(RenderDragon.ENDERCRYSTAL_BEAM_TEXTURES);
                    float f2 = (float) player.X + 0.5F;
                    float f3 = (float) player.Y + 0.5F;
                    float f4 = (float) player.Z + 0.5F;
                    double d0 = f2 - entity.X;
                    double d1 = f3 - entity.Y;
                    double d2 = f4 - entity.Z;
                    renderCrystalBeams(x + d0, y - 1.0D + d1, z + d2, partialTicks, f2, f3, f4, entity.ticksExisted, entity.X, entity.Y, entity.Z);
                }
            }
            EntityPlayer player = Minecraft.getMinecraft().PLAYER;
            if (KillItem.inList(player)) {
                renderer.bindTexture(RenderDragon.ENDERCRYSTAL_BEAM_TEXTURES);
                float f2 = (float) player.X + 0.5F;
                float f3 = (float) player.Y + 0.5F;
                float f4 = (float) player.Z + 0.5F;
                double d0 = f2 - entity.X;
                double d1 = f3 - entity.Y;
                double d2 = f4 - entity.Z;
                renderCrystalBeams(x + d0, y - 1.0D + d1, z + d2, partialTicks, f2, f3, f4, entity.ticksExisted, entity.X, entity.Y, entity.Z);
            }

            if (Config.Annihilation) {
                for (int i = 0; i < 10; i++) {
                    String str = RandomStringUtils.random(10);
                    drawNameplate(str, (float) (x + new Random().nextInt(5)), (float) (y + new Random().nextInt(5)), (float) (z + new Random().nextInt(5)), new Random().nextInt(360), new Random().nextInt(5), new Random().nextInt(5), false, false);
                }
            }
        }
    }

    private static void renderCrystalBeams(double p_188325_0_, double p_188325_2_, double p_188325_4_, float p_188325_6_, double p_188325_7_, double p_188325_9_, double p_188325_11_, int p_188325_13_, double p_188325_14_, double p_188325_16_, double p_188325_18_) {
        float f = (float) (p_188325_14_ - p_188325_7_);
        float f1 = (float) (p_188325_16_ - 1.0D - p_188325_9_);
        float f2 = (float) (p_188325_18_ - p_188325_11_);
        float f3 = MathHelper.sqrt(f * f + f2 * f2);
        float tmp = f * f + f1 * f1 + f2 * f2;
        float f4 = MathHelper.sqrt(tmp);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) p_188325_0_, (float) p_188325_2_ + 2.0F, (float) p_188325_4_);
        GlStateManager.rotate((float) (-Math.atan2(f2, f)) * (180F / (float) Math.PI) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (-Math.atan2(f3, f1)) * (180F / (float) Math.PI) - 90.0F, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(7425);
        float f5 = 0.0F - ((float) p_188325_13_ + p_188325_6_) * 0.01F;
        float f6 = MathHelper.sqrt(tmp) / 32.0F - ((float) p_188325_13_ + p_188325_6_) * 0.01F;
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);
        int i = 8;

        for (int j = 0; j <= 8; ++j) {
            float f7 = MathHelper.sin((float) (j % 8) * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float f8 = MathHelper.cos((float) (j % 8) * ((float) Math.PI * 2F) / 8.0F) * 0.75F;
            float f9 = (float) (j % 8) / 8.0F;
            bufferbuilder.pos(f7 * 0.2F, f8 * 0.2F, 0.0D).tex(f9, f5).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(f7, f8, f4).tex(f9, f6).color(255, 255, 255, 255).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(7424);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static void drawNameplate(String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking) {
        FontRenderer fontRendererIn = Minecraft.getMinecraft().FontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!isSneaking) {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int i = fontRendererIn.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(-i - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(i + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos(i + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (!isSneaking) {
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);

        Random random = new Random();
        GL11.glColor4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void renderSwing(Render<?> render, EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        if (entitylivingbaseIn instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entitylivingbaseIn;

            if (abstractclientplayer.isPlayerInfoSet() && abstractclientplayer.getLocationElytra() != null)
            {
                render.bindTexture(abstractclientplayer.getLocationElytra());
            }
            else if (abstractclientplayer.hasPlayerInfo() && abstractclientplayer.getLocationCape() != null && abstractclientplayer.isWearing(EnumPlayerModelParts.CAPE))
            {
                render.bindTexture(abstractclientplayer.getLocationCape());
            }
            else
            {
                render.bindTexture(TEXTURE_ELYTRA);
            }
        }
        else
        {
            render.bindTexture(TEXTURE_ELYTRA);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.125F);
        modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
        modelElytra.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static final ModelElytra modelElytra = new ModelElytra();
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
}
