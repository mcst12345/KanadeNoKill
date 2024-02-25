package kanade.kill.asm.hooks;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.gui.Gui.drawRect;
import static net.minecraft.client.gui.GuiMainMenu.MINECRAFT_TITLE_TEXTURES;
import static net.minecraft.client.gui.GuiMainMenu.field_194400_H;

@SuppressWarnings("unused")
public class GuiMainMenu {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("kanade:textures/misc/card.png");

    public static void initGui(net.minecraft.client.gui.GuiMainMenu gui) {
        DynamicTexture viewportTexture = new DynamicTexture(256, 256);
        gui.backgroundTexture = gui.mc.getTextureManager().getDynamicTextureLocation("background", viewportTexture);
        gui.widthCopyright = gui.fontRenderer.getStringWidth("Copyright Mojang AB. Do not distribute!");
        gui.widthCopyrightRest = gui.width - gui.widthCopyright - 2;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int j = gui.height / 4 + 48;

        gui.addSingleplayerMultiplayerButtons(j, 24);

        gui.buttonList.add(new GuiButton(0, gui.width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options")));
        gui.buttonList.add(new GuiButton(4, gui.width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit")));
        gui.buttonList.add(new GuiButtonLanguage(5, gui.width / 2 - 124, j + 72 + 12));

        synchronized (gui.threadLock) {
            int openGLWarning1Width = gui.fontRenderer.getStringWidth(gui.openGLWarning1);
            gui.openGLWarning2Width = gui.fontRenderer.getStringWidth(gui.openGLWarning2);
            int k = Math.max(openGLWarning1Width, gui.openGLWarning2Width);
            gui.openGLWarningX1 = (gui.width - k) / 2;
            gui.openGLWarningY1 = (gui.buttonList.get(0)).y - 24;
            gui.openGLWarningX2 = gui.openGLWarningX1 + k;
            gui.openGLWarningY2 = gui.openGLWarningY1 + 24;
        }

        gui.mc.setConnectedToRealms(false);

        if (Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS) && !gui.hasCheckedForRealmsNotification) {
            RealmsBridge realmsbridge = new RealmsBridge();
            gui.realmsNotification = realmsbridge.getNotificationScreen(gui);
            gui.hasCheckedForRealmsNotification = true;
        }

        if (gui.areRealmsNotificationsEnabled()) {
            gui.realmsNotification.setGuiSize(gui.width, gui.height);
            gui.realmsNotification.initGui();
        }
    }

    public static void drawScreen(net.minecraft.client.gui.GuiMainMenu gui, int mouseX, int mouseY, float partialTicks) {
        gui.panoramaTimer += partialTicks;

        gui.mc.getTextureManager().bindTexture(BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int width = gui.width;
        int height = gui.height;
        int cx = width / 2;
        int cy = height / 2;
        double proportion;
        proportion = (double) height / 1440 * 1.1;
        double x = (2520 * proportion / 2);
        double y = (1440 * proportion / 2);
        float zLevel = gui.zLevel;
        bufferbuilder.pos(cx - x, cy + y, zLevel).tex(0, 1).endVertex();
        bufferbuilder.pos(cx + x, cy + y, zLevel).tex(1, 1).endVertex();
        bufferbuilder.pos(cx + x, cy - y, zLevel).tex(1, 0).endVertex();
        bufferbuilder.pos(cx - x, cy - y, zLevel).tex(0, 0).endVertex();
        tessellator.draw();

        int j = gui.width / 2 - 137;
        gui.drawGradientRect(0, 0, gui.width, gui.height, -2130706433, 16777215);
        gui.drawGradientRect(0, 0, gui.width, gui.height, 0, Integer.MIN_VALUE);
        gui.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if ((double) gui.minceraftRoll < 1.0E-4D) {
            gui.drawTexturedModalRect(j, 30, 0, 0, 99, 44);
            gui.drawTexturedModalRect(j + 99, 30, 129, 0, 27, 44);
            gui.drawTexturedModalRect(j + 99 + 26, 30, 126, 0, 3, 44);
            gui.drawTexturedModalRect(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
            gui.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        } else {
            gui.drawTexturedModalRect(j, 30, 0, 0, 155, 44);
            gui.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        }

        gui.mc.getTextureManager().bindTexture(field_194400_H);
        drawModalRectWithCustomSizedTexture(j + 88, 67, 0.0F, 0.0F, 98, 14, 128.0F, 16.0F);

        gui.splashText = "僕らは命に嫌われている。";

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (gui.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float) (Minecraft.getSystemTime() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
        f = f * 100.0F / (float) (gui.fontRenderer.getStringWidth(gui.splashText) + 32);
        GlStateManager.scale(f, f, f);
        gui.drawCenteredString(gui.fontRenderer, gui.splashText, 0, -8, 0xbb6588);
        GlStateManager.popMatrix();

        List<String> brandings = Lists.reverse(net.minecraftforge.fml.common.FMLCommonHandler.instance().getBrandings(true));
        for (int brdline = 0; brdline < brandings.size(); brdline++) {
            String brd = brandings.get(brdline);
            if (!com.google.common.base.Strings.isNullOrEmpty(brd)) {
                gui.drawString(gui.fontRenderer, brd, 2, gui.height - (10 + brdline * (gui.fontRenderer.FONT_HEIGHT + 1)), 16777215);
            }
        }

        gui.drawString(gui.fontRenderer, "Copyright Mojang AB. Do not distribute!", gui.widthCopyrightRest, gui.height - 10, -1);

        if (mouseX > gui.widthCopyrightRest && mouseX < gui.widthCopyrightRest + gui.widthCopyright && mouseY > gui.height - 10 && mouseY < gui.height && Mouse.isInsideWindow()) {
            drawRect(gui.widthCopyrightRest, gui.height - 1, gui.widthCopyrightRest + gui.widthCopyright, gui.height, -1);
        }

        if (gui.openGLWarning1 != null && !gui.openGLWarning1.isEmpty()) {
            drawRect(gui.openGLWarningX1 - 2, gui.openGLWarningY1 - 2, gui.openGLWarningX2 + 2, gui.openGLWarningY2 - 1, 1428160512);
            gui.drawString(gui.fontRenderer, gui.openGLWarning1, gui.openGLWarningX1, gui.openGLWarningY1, -1);
            gui.drawString(gui.fontRenderer, gui.openGLWarning2, (gui.width - gui.openGLWarning2Width) / 2, (gui.buttonList.get(0)).y - 12, -1);
        }

        for (int i = 0; i < gui.buttonList.size(); ++i) {
            gui.buttonList.get(i).drawButton(gui.mc, mouseX, mouseY, partialTicks);
        }

        for (int ij = 0; ij < gui.labelList.size(); ++ij) {
            gui.labelList.get(ij).drawLabel(gui.mc, mouseX, mouseY);
        }
    }
}
