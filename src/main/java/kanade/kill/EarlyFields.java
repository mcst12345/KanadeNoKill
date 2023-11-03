package kanade.kill;

import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;

public class EarlyFields {
    public static final long transformers_offset;

    static {
        try {
            Field field = LaunchClassLoader.class.getDeclaredField("transformers");
            transformers_offset = Unsafe.instance.objectFieldOffset(field);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
