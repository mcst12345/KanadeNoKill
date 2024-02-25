package kanade.kill.util;

import kanade.kill.render.RenderBeaconBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class KanadeFontRender extends FontRenderer {
    public static KanadeFontRender INSTANCE = null;

    private KanadeFontRender(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
        super(gameSettingsIn, location, textureManagerIn, unicode);
    }

    public static KanadeFontRender Get() {
        if (INSTANCE == null) {
            INSTANCE = new KanadeFontRender(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().RenderEngine, true);
        }
        return INSTANCE;
    }

    @Override
    protected float renderUnicodeChar(char ch, boolean italic) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor(RenderBeaconBeam.color[0], RenderBeaconBeam.color[1], RenderBeaconBeam.color[2], 1.0f);
        return super.renderUnicodeChar(ch, italic);
    }

    @Override
    protected float renderDefaultChar(int ch, boolean italic) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor(RenderBeaconBeam.color[0], RenderBeaconBeam.color[1], RenderBeaconBeam.color[2], 1.0f);
        return super.renderDefaultChar(ch, italic);
    }

    @Override
    protected void doDraw(float f) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor(RenderBeaconBeam.color[0], RenderBeaconBeam.color[1], RenderBeaconBeam.color[2], 1.0f);
        super.doDraw(f);
    }
}
