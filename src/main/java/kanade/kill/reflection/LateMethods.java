package kanade.kill.reflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Method;

public class LateMethods {
    public static final Method setWorldAndResolution;

    static {
        try {
            setWorldAndResolution = GuiScreen.class.getDeclaredMethod("setWorldAndResolution", Minecraft.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
