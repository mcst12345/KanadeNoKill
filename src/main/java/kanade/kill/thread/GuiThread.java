package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.LateFields;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;
import scala.concurrent.util.Unsafe;

public class GuiThread extends Thread {
    private static final GuiThread instance = new GuiThread();
    private static Object death = null;

    static {
        instance.start();
        Unsafe.instance.putObjectVolatile(ModMain.GUI, EarlyFields.name_offset, "net.minecraft.client.gui.inventory.GuiInventory");
    }

    private GuiThread() {
        this.setPriority(10);
    }

    public synchronized static void display() {
        if (death == null || Unsafe.instance.getBooleanVolatile(death, LateFields.close_offset)) {
            try {
                death = ModMain.GUI.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
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
                if (!Unsafe.instance.getBooleanVolatile(death, LateFields.close_offset)) {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (!Util.isKanadeDeathGui(mc)) {
                        synchronized (mc) {
                            mc.displayGuiScreen((GuiScreen) death);
                            mc.SetIngameNotInFocus();
                        }
                    }
                    KeyBinding.unPressAllKeys();
                    Mouse.setGrabbed(true);
                } else {
                    Launch.LOGGER.info("Gui closed.");
                    death = null;
                }
            }
        }
    }
}
