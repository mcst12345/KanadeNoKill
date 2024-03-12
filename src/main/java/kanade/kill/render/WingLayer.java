package kanade.kill.render;

import kanade.kill.item.KillItem;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class WingLayer implements LayerRenderer<EntityLivingBase> {
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/colormap/foliage.png");
    protected final RenderLivingBase<?> renderPlayer;
    private final ModelBase model = new ModelEnderCrystal(0,false);

    private static final ResourceLocation ENDER_CRYSTAL_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");

    public WingLayer(RenderLivingBase<?> p_i47185_1_)
    {
        this.renderPlayer = p_i47185_1_;
    }

    public void doRenderLayer(@Nonnull EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {

        if (KillItem.inList(entitylivingbaseIn))
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            renderPlayer.bindTexture(ENDER_CRYSTAL_TEXTURES);

            /*if (entitylivingbaseIn instanceof AbstractClientPlayer)
            {
                AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entitylivingbaseIn;

                if (abstractclientplayer.isPlayerInfoSet() && abstractclientplayer.getLocationElytra() != null)
                {
                    this.renderPlayer.bindTexture(abstractclientplayer.getLocationElytra());
                }
                else if (abstractclientplayer.hasPlayerInfo() && abstractclientplayer.getLocationCape() != null && abstractclientplayer.isWearing(EnumPlayerModelParts.CAPE))
                {
                    this.renderPlayer.bindTexture(abstractclientplayer.getLocationCape());
                }
                else
                {
                    this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
                }
            }
            else
            {
                this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
            }*/


            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            this.model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
            this.model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
