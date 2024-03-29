package kanade.kill.reflection;

import org.lwjgl.opengl.GLContext;

import java.lang.reflect.Method;

public class EarlyMethods {
    public static final Method getDeclaredFields0;
    public static final Method getDeclaredMethods0;
    public static final Method getDeclaredConstructors0;
    public static final Method forName0;
    public static final Method getThreads;
    public static final Method invoke0;
    public static final Method getName0;
    public static final Method stop0;
    public static final Method getFunctionAddress;

    static {
        try {
            boolean client = System.getProperty("minecraft.client.jar") != null;
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
            getName0 = Class.class.getDeclaredMethod("getName0");
            getName0.setAccessible(true);
            stop0 = Thread.class.getDeclaredMethod("stop0", Object.class);
            getDeclaredConstructors0 = Class.class.getDeclaredMethod("getDeclaredConstructors0", boolean.class);
            getDeclaredConstructors0.setAccessible(true);
            getDeclaredMethods0.setAccessible(true);
            if (client) {
                getFunctionAddress = ReflectionUtil.getMethod(GLContext.class, "ngetFunctionAddress", Long.TYPE);
            } else {
                getFunctionAddress = null;
            }
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
