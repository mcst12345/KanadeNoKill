package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.reflection.EarlyFields;
import scala.concurrent.util.Unsafe;

import java.lang.ref.SoftReference;

@SuppressWarnings("unused")
public class ClassUtil {
    public static void setClassLoader(Class<?> clazz, ClassLoader loader) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.classLoader_offset, loader);
    }

    public static void setName(Class<?> clazz, String name) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.name_offset, name);
    }

    public static void setRedefinedCount(Class<?> clazz, int count) {
        Unsafe.instance.putIntVolatile(clazz, EarlyFields.classRedefinedCount_offset, count);
    }

    public static SoftReference getReflectionData(Class<?> clazz) {
        return (SoftReference) Unsafe.instance.getObjectVolatile(clazz, EarlyFields.reflectionData_offset);
    }

    public static void setReflectionData(Class<?> clazz, SoftReference sr) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.reflectionData_offset, sr);
    }

    public static void setClass(Object obj, Class<?> clazz) {
        try {
            int tmp = Unsafe.instance.getIntVolatile(Unsafe.instance.allocateInstance(clazz), 8L);
            Unsafe.instance.putIntVolatile(obj, 8L, tmp);
        } catch (InstantiationException e) {
            Launch.LOGGER.error("Failed to set class of object!", e);
        }
    }
}
