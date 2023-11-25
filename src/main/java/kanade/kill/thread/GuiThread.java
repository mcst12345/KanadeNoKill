package kanade.kill.thread;

import kanade.kill.Core;
import kanade.kill.ModMain;
import kanade.kill.reflection.LateFields;
import kanade.kill.util.GuiDeath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import scala.concurrent.util.Unsafe;

public class GuiThread extends Thread {
    private static final GuiThread instance = new GuiThread();

    static {
        instance.start();
    }

    private GuiDeath death = null;

    private GuiThread() {
        this.setPriority(9);
    }

    public static void display() {
        if (instance.death == null || instance.death.close) {
            instance.death = new GuiDeath();
        }
    }

    @Override
    public synchronized void run() {
        if (!ModMain.client) {
            return;
        }
        while (true) {
            if (death != null) {
                if (!death.close) {
                    Minecraft mc = Minecraft.getMinecraft();
                    Object gui = Unsafe.instance.getObjectVolatile(mc, LateFields.currentScreen_offset);
                    if (gui == null || gui.getClass() != GuiDeath.class) {
                        Core.LOGGER.info("Displaying Death Gui.");
                        if (gui instanceof GuiScreen) {
                            ((GuiScreen) gui).onGuiClosed();
                        }
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        death.setWorldAndResolution(mc, i, j);
                        Unsafe.instance.putObjectVolatile(mc, LateFields.currentScreen_offset, death);
                        mc.SetIngameNotInFocus();
                        KeyBinding.unPressAllKeys();

                        while (Mouse.next()) {
                        }

                        while (Keyboard.next()) {
                        }

                        mc.skipRenderWorld = false;
                    }
                } else {
                    Core.LOGGER.info("Gui closed.");
                    death = null;
                }
            }
        }
    }
}