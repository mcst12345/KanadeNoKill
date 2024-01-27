package kanade.kill.util;

import sun.misc.Unsafe;

@SuppressWarnings("unused")
public class UnsafeAccessor {
    public static final Unsafe UNSAFE;

    static {
        ClassUtil.setClassLoader(UnsafeAccessor.class, null);
        UNSAFE = Unsafe.getUnsafe();
    }
}
