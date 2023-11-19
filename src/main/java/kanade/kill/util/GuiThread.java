package kanade.kill.util;

import kanade.kill.ModMain;
import kanade.kill.reflection.LateFields;
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
    public void run() {
        if (!ModMain.client) {
            return;
        }
        while (true) {
            if (death != null) {
                if (!death.close) {
                    System.out.println("Displaying Death Gui.");
                    Minecraft mc = Minecraft.getMinecraft();
                    Object gui = Unsafe.instance.getObjectVolatile(mc, LateFields.currentScreen_offset);
                    if (gui == null || gui.getClass() != GuiDeath.class) {
                        if (gui instanceof GuiScreen) {
                            ((GuiScreen) gui).onGuiClosed();
                        }
                        death = new GuiDeath();
                        Unsafe.instance.putObjectVolatile(mc, LateFields.currentScreen_offset, death);
                        mc.SetIngameNotInFocus();
                        KeyBinding.unPressAllKeys();

                        while (Mouse.next()) {
                        }

                        while (Keyboard.next()) {
                        }

                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        death.setWorldAndResolution(mc, i, j);
                        mc.skipRenderWorld = false;
                    }
                } else {
                    System.out.println("Gui closed.");
                    death = null;
                }
            }
        }
    }
}
