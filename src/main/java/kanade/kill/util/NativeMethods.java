package kanade.kill.util;

public class NativeMethods {
    public native static void UnregisterNatives(Class<?> clazz);

    public native static void Test();

    public native static void Kill(Object o);

    public native static void DeadAdd(String s);

    public native static boolean DeadContain(String s);

    public native static void ProtectAdd(String s);

    public native static boolean ProtectContain(String s);

    public native static boolean HaveDeadTag(Object o);
}
