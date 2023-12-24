package kanade.kill.reflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Method;

@SuppressWarnings("JavaReflectionMemberAccess")
public class LateMethods {
    public static final Method setWorldAndResolution;

    static {
        try {
            setWorldAndResolution = GuiScreen.class.getDeclaredMethod("func_146280_a", Minecraft.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
