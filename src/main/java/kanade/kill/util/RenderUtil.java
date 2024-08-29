package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.entity.Lain;
import kanade.kill.render.RenderLain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RenderUtil {
    @SideOnly(Side.CLIENT)
    public static final RenderLain renderer;

    static {
        if (Launch.client) {
            renderer = new RenderLain(Minecraft.getMinecraft().getRenderManager(), new ModelPlayer(1, true), 0);
        } else {
            renderer = null;
        }
    }

    private static void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private static boolean setScoreTeamColor(Lain entityLivingBaseIn) {
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    private static float prepareScale(Lain entitylivingbaseIn, float partialTicks) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        float f = 0.0625F;
        GlStateManager.translate(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    private static void applyRotations(Lain entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);

        String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

        if (("Dinnerbone".equals(s) || "Grumm".equals(s))) {
            GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    private static void renderLivingAt(Lain entityLivingBaseIn, double x, double y, double z) {
        GlStateManager.translate((float) x, (float) y, (float) z);
    }

    private static int getTeamColor(Lain entityIn) {
        int i = 16777215;
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) entityIn.getTeam();

        if (scoreplayerteam != null) {
            String s = FontRenderer.getFormatFromString(scoreplayerteam.getPrefix());

            if (s.length() >= 2) {
                i = Minecraft.getMinecraft().FontRenderer.getColorCode(s.charAt(1));
            }
        }

        return i;
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

    private static float handleRotationFloat(Lain livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    @SideOnly(Side.CLIENT)
    public static void renderLain(Minecraft mc, Lain lain) {
        ModelPlayer model = new ModelPlayer(1.0f, true);
        float partialTicks = mc.timer.renderPartialTicks;
        double d0 = lain.lastTickPosX + (lain.posX - lain.lastTickPosX) * (double) partialTicks;
        double d1 = lain.lastTickPosY + (lain.posY - lain.lastTickPosY) * (double) partialTicks;
        double d2 = lain.lastTickPosZ + (lain.posZ - lain.lastTickPosZ) * (double) partialTicks;

        Entity e = mc.getRenderViewEntity();
        assert e != null;
        double d3 = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) partialTicks;
        double d4 = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) partialTicks;
        double d5 = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) partialTicks;


        double x = d0 - d3;
        double y = d1 - d4;
        double z = d2 - d5;

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        model.swingProgress = lain.getSwingProgress(partialTicks);
        boolean shouldSit = lain.isRiding() && (lain.getRidingEntity() != null && lain.getRidingEntity().shouldRiderSit());
        model.isRiding = shouldSit;
        model.isChild = true;

        try {
            float f = interpolateRotation(lain.prevRenderYawOffset, lain.renderYawOffset, partialTicks);
            float f1 = interpolateRotation(lain.prevRotationYawHead, lain.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (shouldSit && lain.getRidingEntity() instanceof EntityLivingBase) {
                EntityLivingBase entitylivingbase = (EntityLivingBase) lain.getRidingEntity();
                f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapDegrees(f2);

                if (f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F) {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F) {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f7 = lain.prevRotationPitch + (lain.rotationPitch - lain.prevRotationPitch) * partialTicks;
            renderLivingAt(lain, x, y, z);
            float f8 = handleRotationFloat(lain, partialTicks);
            applyRotations(lain, f8, f, partialTicks);
            float f4 = prepareScale(lain, partialTicks);
            float f5 = 0.0F;
            float f6 = 0.0F;

            if (!lain.isRiding()) {
                f5 = lain.prevLimbSwingAmount + (lain.limbSwingAmount - lain.prevLimbSwingAmount) * partialTicks;
                f6 = lain.limbSwing - lain.limbSwingAmount * (1.0F - partialTicks);

                if (f5 > 1.0F) {
                    f5 = 1.0F;
                }
                f2 = f1 - f; // Forge: Fix MC-1207
            }

            GlStateManager.enableAlpha();
            model.setLivingAnimations(lain, f6, f5, partialTicks);
            model.setRotationAngles(f6, f5, f8, f2, f7, f4, lain);

            boolean flag1 = setScoreTeamColor(lain);
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(lain));

            renderModel(model, lain, f6, f5, f8, f2, f7, f4);

            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();

            if (flag1) {
                unsetScoreTeamColor();
            }

            GlStateManager.disableRescaleNormal();
        } catch (Exception exception) {
            Launch.LOGGER.error("Couldn't render entity", (Throwable) exception);
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private static void unsetBrightness() {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture(0);
        GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private static boolean bindEntityTexture(Lain entity) {
        ResourceLocation resourcelocation = RenderLain.TEXTURE;

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourcelocation);
        return true;
    }

    private static void renderModel(ModelBase model, Lain entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        boolean flag = true;
        boolean flag1 = false;

        if (!bindEntityTexture(entitylivingbaseIn)) {
            return;
        }

        model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

    }
}
