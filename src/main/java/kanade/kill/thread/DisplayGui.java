package kanade.kill.thread;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.*;

public class DisplayGui {
    private static boolean display;

    private DisplayGui() {
    }

    public synchronized static void display() {
        display = true;
    }

    public static void run() { //Wll be called from he Minecraft client thread.
        {
            if (display) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.timer.updateTimer();
                if (mc.rightClickDelayTimer > 0) {
                    --mc.rightClickDelayTimer;
                }
                mc.SetIngameNotInFocus();
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                for (int j = 0; j < Math.min(10, mc.timer.elapsedTicks); ++j) {
                    mc.leftClickCounter = 10000;
                    mc.runTickMouse();

                    if (mc.leftClickCounter > 0) {
                        --mc.leftClickCounter;
                    }

                    mc.runTickKeyboard();
                }

                GlStateManager.pushMatrix();
                GlStateManager.clear(16640);
                mc.framebuffer.bindFramebuffer(true);
                GlStateManager.enableTexture2D();

                GlStateManager.clear(256);
                mc.framebuffer.unbindFramebuffer();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                mc.framebuffer.framebufferRender(mc.displayWidth, mc.displayHeight);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();

                GlStateManager.clear(256);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);

                GlStateManager.popMatrix();

                Display.update();
                mc.checkWindowResize();
                Thread.yield();

                if (mc.isFramerateLimitBelowMax()) {
                    Display.sync(mc.getLimitFramerate());
                }
                Window window = javax.swing.FocusManager.getCurrentManager().getActiveWindow();

                JOptionPane.showMessageDialog(window, "You are dead!");

                display = false;
                if (mc.PLAYER != null) {
                    mc.PLAYER.connection.getNetworkManager().closeChannel(new TextComponentString("You die!"));
                }
                mc.loadWorld(null);

                mc.displayGuiScreen(new GuiMainMenu());
            }
        }
    }
}
