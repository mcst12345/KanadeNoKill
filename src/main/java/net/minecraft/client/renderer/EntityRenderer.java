package net.minecraft.client.renderer;

import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;

public class EntityRenderer {
    public static boolean anaglyphEnable;
    public int[] lightmapColors;
    public ItemRenderer itemRenderer;
    public ShaderGroup shaderGroup;

    public void updateCameraAndRender(float v, long i) {

    }

    public void renderStreamIndicator(float renderPartialTicks) {

    }

    public void loadShader(ResourceLocation resourceLocationIn) {
    }

    public void stopUseShader() {
    }

    public boolean isShaderActive() {
        return OpenGlHelper.shadersSupported && this.shaderGroup != null;
    }

    public void enableLightmap() {

    }

    public void disableLightmap() {
    }
}
