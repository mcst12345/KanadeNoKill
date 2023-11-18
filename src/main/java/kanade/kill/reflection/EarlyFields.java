package kanade.kill.reflection;

import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EarlyFields {
    public static final long transformers_offset;
    public static final long reflectionData_offset;
    public static final long modifiers_offset;

    static {
        try {
            Field field = Field.class.getDeclaredField("modifiers");
            modifiers_offset = Unsafe.instance.objectFieldOffset(field);
            field = LaunchClassLoader.class.getDeclaredField("transformers");
            transformers_offset = Unsafe.instance.objectFieldOffset(field);
            Unsafe.instance.putObjectVolatile(field, modifiers_offset, Modifier.FINAL | Modifier.PRIVATE);
            field = Class.class.getDeclaredField("reflectionData");
            reflectionData_offset = Unsafe.instance.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
