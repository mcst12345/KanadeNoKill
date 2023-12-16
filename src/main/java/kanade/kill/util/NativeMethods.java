package kanade.kill.util;

public class NativeMethods {
    public native static void UnregisterNatives(Class<?> clazz);

    public native static void Test(String s);

    public native static void Kill(Object o);

    public native static void DeadAdd(int s);

    public native static boolean DeadContain(int s);

    public native static void ProtectAdd(int s);

    public native static boolean ProtectContain(int s);

    public native static boolean HaveDeadTag(Object o);
}
