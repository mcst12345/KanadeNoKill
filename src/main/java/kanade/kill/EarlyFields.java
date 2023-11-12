package kanade.kill;

import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;

public class EarlyFields {
    public static final long transformers_offset;
    public static final long reflectionData_offset;

    static {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("transformers");
            transformers_offset = Unsafe.instance.objectFieldOffset(field);
            field = Class.class.getDeclaredField("reflectionData");
            reflectionData_offset = Unsafe.instance.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
