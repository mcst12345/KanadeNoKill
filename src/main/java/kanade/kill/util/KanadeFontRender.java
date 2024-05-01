package kanade.kill.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Random;

import static kanade.kill.util.Util.drawRandomString;

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

    private static int color = 0xbb6588;
    private static int count = 0;
    private static boolean flag = true;

    @Override
    protected float renderUnicodeChar(char ch, boolean italic) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0f);
        return super.renderUnicodeChar(ch, italic);
    }

    @Override
    protected float renderDefaultChar(int ch, boolean italic) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0f);
        return super.renderDefaultChar(ch, italic);
    }

    @Override
    protected void doDraw(float f) {
        Random r = new Random();
        this.posX += 5 - r.nextInt(11);
        this.posY += 10 - r.nextInt(21);
        setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0f);
        super.doDraw(f);
        if (flag) {
            color++;
        } else {
            color--;
        }
        if (count == 10000) {
            count = 0;
            flag = !flag;
        }
    }

    @Override
    public void renderStringAtPos(@Nonnull String text, boolean shadow) {
        super.renderStringAtPos(text, shadow);
        drawRandomString();
    }

}
