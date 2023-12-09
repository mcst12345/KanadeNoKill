package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.LateFields;
import kanade.kill.util.GuiDeath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;
import scala.concurrent.util.Unsafe;

public class GuiThread extends Thread {
    private static final GuiThread instance = new GuiThread();
    private static GuiDeath death = null;

    static {
        instance.start();
        Unsafe.instance.putObjectVolatile(GuiDeath.class, EarlyFields.name_offset, "net.minecraft.client.gui.inventory.GuiInventory");
    }

    private GuiThread() {
        this.setPriority(9);
    }

    public synchronized static void display() {
        if (death == null || death.close) {
            death = new GuiDeath();
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
                            Launch.LOGGER.info("Displaying Death Gui.");
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

                            mc.skipRenderWorld = false;
                        }
                        Mouse.setGrabbed(true);
                    } else {
                        Launch.LOGGER.info("Gui closed.");
                        death = null;
                    }
                }
        }
    }
}
