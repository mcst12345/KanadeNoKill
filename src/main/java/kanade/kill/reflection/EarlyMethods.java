package kanade.kill.reflection;

import java.lang.reflect.Method;

public class EarlyMethods {
    public static final Method getDeclaredFields0;
    public static final Method getDeclaredMethods0;
    public static final Method forName0;
    public static final Method getThreads;
    public static final Method invoke0;

    static {
        try {
            getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            getDeclaredMethods0.setAccessible(true);
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            forName0 = Class.class.getDeclaredMethod("forName0", String.class, boolean.class, ClassLoader.class, Class.class);
            forName0.setAccessible(true);
            Class<?> clazz = Class.forName("sun.management.ThreadImpl");
            getThreads = clazz.getDeclaredMethod("getThreads");
            getThreads.setAccessible(true);
            clazz = Class.forName("sun.reflect.NativeMethodAccessorImpl");
            invoke0 = clazz.getDeclaredMethod("invoke0", Method.class, Object.class, Object[].class);
            invoke0.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
