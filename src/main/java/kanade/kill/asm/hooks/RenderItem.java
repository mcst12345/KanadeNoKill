package kanade.kill.asm.hooks;

import kanade.kill.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static net.minecraft.client.renderer.RenderItem.RES_ITEM_GLINT;

@SuppressWarnings("unused")
public class RenderItem {
    private static final float[] color = new float[]{0, 0, 0};
    private static int Color = 0;

    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (color[0] < 1.0F) {
                    color[0] += 0.05F;
                } else if (color[1] < 1.0F) {
                    color[1] += 0.05F;
                } else if (color[2] < 1.0F) {
                    color[2] += 0.05F;
                } else {
                    color[0] = 0.0F;
                    color[1] = 0.0F;
                    color[2] = 0.0F;
                }
                if (Color > -9999999) {
                    Color--;
                } else {
                    Color = 0;
                }
            }
        }, 50L, 50L);
    }

    public static void renderItemModel(net.minecraft.client.renderer.RenderItem renderItem, ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        if (!stack.isEmpty()) {
            boolean flag = stack.ITEM == ModMain.kill_item;
            renderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            renderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

            if (!flag) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                GlStateManager.color(color[0], color[1], color[2], 1.0F);
            }
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            // TODO: check if negative scale is a thing
            bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, transform, leftHanded);

            if (!stack.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);

                if (bakedmodel.isBuiltInRenderer()) {
                    if (flag) {
                        GlStateManager.color(color[0], color[1], color[2], 1.0F);
                    }
                    GlStateManager.enableRescaleNormal();
                    stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                } else {
                    renderItem.renderModel(bakedmodel, stack);
                    if (flag) {
                        GlStateManager.depthMask(false);
                        GlStateManager.depthFunc(514);
                        GlStateManager.disableLighting();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                        renderItem.textureManager.bindTexture(RES_ITEM_GLINT);
                        GlStateManager.matrixMode(5890);
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(8.0F, 8.0F, 8.0F);
                        float f = (float) (net.minecraft.client.Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                        GlStateManager.translate(f, 0.0F, 0.0F);
                        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                        renderItem.renderModel(bakedmodel, Color);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(8.0F, 8.0F, 8.0F);
                        float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
                        GlStateManager.translate(-f1, 0.0F, 0.0F);
                        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
                        renderItem.renderModel(bakedmodel, -(new Random().nextInt(9999999)));
                        GlStateManager.popMatrix();
                        GlStateManager.matrixMode(5888);
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        GlStateManager.enableLighting();
                        GlStateManager.depthFunc(515);
                        GlStateManager.depthMask(true);
                        renderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    } else if (stack.hasEffect()) {
                        renderItem.renderEffect(bakedmodel);
                    }
                }

                GlStateManager.popMatrix();
            }
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            renderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            renderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
    }
}
