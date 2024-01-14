package kanade.kill.asm.hooks;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class MouseHelper {
    public static void grabMouseCursor(net.minecraft.util.MouseHelper helper) {
        if (Boolean.parseBoolean(System.getProperty("fml.noGrab", "false"))) return;
        Mouse.setGrabbed(true);
        helper.deltaX = 0;
        helper.deltaY = 0;
    }

    public static void ungrabMouseCursor() {
        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
        Mouse.setGrabbed(false);
    }

    public static void mouseXYChange(net.minecraft.util.MouseHelper helper) {
        helper.deltaX = Mouse.getDX();
        helper.deltaY = Mouse.getDY();
    }
}
