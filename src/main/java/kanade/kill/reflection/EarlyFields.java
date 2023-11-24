package kanade.kill.reflection;

import kanade.kill.util.KanadeSecurityManager;
import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;
import sun.instrument.InstrumentationImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EarlyFields {
    public static final long transformers_offset;
    public static final long reflectionData_offset;
    public static final long modifiers_offset;
    public static final Object security_base;
    public static final long security_offset;
    public static final long uncaughtExceptionHandler_offset;
    public static final long renameTransformer_offset;
    public static final long mNativeAgent_offset;

    static {
        try {
            Field field = ReflectionUtil.getField(Field.class, "modifiers");
            modifiers_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "transformers");
            transformers_offset = Unsafe.instance.objectFieldOffset(field);
            field = LaunchClassLoader.class.getDeclaredField("renameTransformer");
            renameTransformer_offset = Unsafe.instance.objectFieldOffset(field);
            Unsafe.instance.putObjectVolatile(field, modifiers_offset, Modifier.FINAL | Modifier.PRIVATE);
            field = ReflectionUtil.getField(Class.class, "reflectionData");
            reflectionData_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(System.class, "security");
            security_base = Unsafe.instance.staticFieldBase(field);
            security_offset = Unsafe.instance.staticFieldOffset(field);
            Unsafe.instance.putObjectVolatile(security_base, security_offset, KanadeSecurityManager.INSTANCE);
            field = ReflectionUtil.getField(Thread.class, "uncaughtExceptionHandler");
            uncaughtExceptionHandler_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(InstrumentationImpl.class, "mNativeAgent");
            mNativeAgent_offset = Unsafe.instance.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
