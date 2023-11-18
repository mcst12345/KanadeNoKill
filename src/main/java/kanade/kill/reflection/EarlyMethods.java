package kanade.kill.reflection;

import java.lang.reflect.Method;

public class EarlyMethods {
    public static final Method getDeclaredFields0;
    public static final Method getDeclaredMethods0;

    static {
        try {
            getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            getDeclaredMethods0.setAccessible(true);
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
