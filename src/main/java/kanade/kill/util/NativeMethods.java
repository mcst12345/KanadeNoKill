package kanade.kill.util;

import java.lang.instrument.Instrumentation;

@SuppressWarnings("unused")
public class NativeMethods {
    public native static void UnregisterNatives(Class<?> clazz);

    public native static void Test(String s);

    public native static void Kill(Object o);

    public native static void DeadAdd(int s);

    public native static boolean DeadContain(int s);

    public native static void DeadRemove(int s);

    public native static void ProtectAdd(int s);

    public native static boolean ProtectContain(int s);

    public native static void ProtectRemove(int s);

    public native static void Reset();
    public native static boolean HaveDeadTag(Object o);

    public native static void SetTag(Object o, long tag);

    public native static boolean HaveTag(Object o, long tag);

    public native static void FuckObjects();

    public native static Instrumentation ConstructInst();
}
