package kanade.kill.reflection;

import kanade.kill.util.KanadeSecurityManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import paulscode.sound.SoundSystem;
import scala.concurrent.util.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureClassLoader;

public class EarlyFields {
    public static final long transformers_offset;
    public static final long reflectionData_offset;
    public static final long modifiers_offset;
    public static final Object security_base;
    public static final long security_offset;
    public static final long uncaughtExceptionHandler_offset;
    public static final long renameTransformer_offset;
    public static final long mNativeAgent_offset;
    public static final long invalidClasses_offset;
    public static final long transformerExceptions_offset;
    public static final long packageManifests_offset;
    public static final long cachedClasses_offset;
    public static final long classLoaderExceptions_offset;
    public static final long parent_offset;
    public static final long sources_offset;
    public static final long resourceCache_offset;
    public static final long negativeResourceCache_offset;
    public static final long loadBuffer_offset;
    public static final long contextClassLoader_offset;
    public static final long name_offset;
    public static final Object classLoader_base;
    public static final long classLoader_offset;
    public static final long pdcache_offset;
    public static final long soundLibrary_offset;
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
            field = ReflectionUtil.getField(Class.class, "name");
            name_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(System.class, "security");
            security_base = Unsafe.instance.staticFieldBase(field);
            security_offset = Unsafe.instance.staticFieldOffset(field);
            Unsafe.instance.putObjectVolatile(security_base, security_offset, KanadeSecurityManager.INSTANCE);
            field = ReflectionUtil.getField(Thread.class, "uncaughtExceptionHandler");
            uncaughtExceptionHandler_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Class.forName("sun.instrument.InstrumentationImpl"), "mNativeAgent");
            mNativeAgent_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "invalidClasses");
            invalidClasses_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "transformerExceptions");
            transformerExceptions_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "packageManifests");
            packageManifests_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "cachedClasses");
            cachedClasses_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "classLoaderExceptions");
            classLoaderExceptions_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "parent");
            parent_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "sources");
            sources_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "resourceCache");
            resourceCache_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "negativeResourceCache");
            negativeResourceCache_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(LaunchClassLoader.class, "loadBuffer");
            loadBuffer_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Thread.class, "contextClassLoader");
            contextClassLoader_offset = Unsafe.instance.objectFieldOffset(field);
            field = ReflectionUtil.getField(Launch.class, "classLoader");
            classLoader_base = Unsafe.instance.staticFieldBase(field);
            classLoader_offset = Unsafe.instance.staticFieldOffset(field);
            field = ReflectionUtil.getField(SecureClassLoader.class, "pdcache");
            pdcache_offset = Unsafe.instance.objectFieldOffset(field);
            if (kanade.kill.Launch.client) {
                field = ReflectionUtil.getField(SoundSystem.class, "soundLibrary");
                soundLibrary_offset = Unsafe.instance.objectFieldOffset(field);
            } else {
                soundLibrary_offset = 0;
            }
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
