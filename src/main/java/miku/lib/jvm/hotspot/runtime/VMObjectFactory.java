package miku.lib.jvm.hotspot.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VMObjectFactory {
    public static VMObject newObject(Class<? extends VMObject> clazz, long addr) {
        try {
            if (addr == 0L) {
                return null;
            } else {
                Constructor<? extends VMObject> c = clazz.getDeclaredConstructor(long.class);
                return c.newInstance(addr);
            }
        } catch (InvocationTargetException var3) {
            if (var3.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) var3.getTargetException();
            } else {
                throw new RuntimeException(var3);
            }
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }
}
