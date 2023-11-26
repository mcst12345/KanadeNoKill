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
    private static final GuiThread[] instance = new GuiThread[4];
    private static GuiDeath death = null;

    static {
        for (int i = 0; i < 4; i++) {
            instance[i] = new GuiThread();
            instance[i].start();
        }
    }

    private GuiThread() {
        this.setPriority(9);
    }

    public synchronized static void display() {
        synchronized (death) {
            if (death == null || death.close) {
                death = new GuiDeath();
            }
        }
    }

    @Override
    public synchronized void run() {
        if (!ModMain.client) {
            return;
        }
        while (true) {
            if (death != null) {
                synchronized (death) {
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
}
