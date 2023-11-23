package kanade.kill.reflection;

import kanade.kill.util.KanadeSecurityManager;
import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class EarlyFields {
    public static final long transformers_offset;
    public static final long reflectionData_offset;
    public static final long modifiers_offset;
    public static final Object security_base;
    public static final long security_offset;
    public static final long uncaughtExceptionHandler_offset;
    public static final long renameTransformer_offset;

    static {
        try {
            Field field = Field.class.getDeclaredField("modifiers");
            modifiers_offset = Unsafe.instance.objectFieldOffset(field);
            field = LaunchClassLoader.class.getDeclaredField("transformers");
            transformers_offset = Unsafe.instance.objectFieldOffset(field);
            Unsafe.instance.putObjectVolatile(field, modifiers_offset, Modifier.FINAL | Modifier.PRIVATE);
            field = Class.class.getDeclaredField("reflectionData");
            reflectionData_offset = Unsafe.instance.objectFieldOffset(field);
            for (Field field1 : (Field[]) EarlyMethods.getDeclaredFields0.invoke(System.class, false)) {
                if (field1.getName().equals("security")) {
                    field = field1;
                    break;
                }
            }
            if (field.getName().equals("security")) {
                security_base = Unsafe.instance.staticFieldBase(field);
                security_offset = Unsafe.instance.staticFieldOffset(field);
                Unsafe.instance.putObjectVolatile(security_base, security_offset, KanadeSecurityManager.INSTANCE);
            } else {
                security_base = null;
                security_offset = 0;
            }
            for (Field field1 : (Field[]) EarlyMethods.getDeclaredFields0.invoke(Thread.class, false)) {
                if (field1.getName().equals("uncaughtExceptionHandler")) {
                    field = field1;
                    break;
                }
            }
            if (field.getName().equals("uncaughtExceptionHandler")) {
                uncaughtExceptionHandler_offset = Unsafe.instance.objectFieldOffset(field);
            } else {
                uncaughtExceptionHandler_offset = 0;
            }
            field = LaunchClassLoader.class.getDeclaredField("renameTransformer");
            renameTransformer_offset = Unsafe.instance.objectFieldOffset(field);
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
