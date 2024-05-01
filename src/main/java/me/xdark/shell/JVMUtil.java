package me.xdark.shell;

import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.util.ObjectUtil;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JVMUtil {

    public static final Unsafe UNSAFE;
    public static final MethodHandles.Lookup LOOKUP;
    private static final NativeLibraryLoader NATIVE_LIBRARY_LOADER;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = UNSAFE = (Unsafe) field.get(null);
            field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            LOOKUP = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(field),
                    unsafe.staticFieldOffset(field));
            NATIVE_LIBRARY_LOADER = Float.parseFloat(System.getProperty("java.class.version")) - 44 > 8 ? new Java9LibraryLoader() : new Java8LibraryLoader();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private JVMUtil() {
    }

    public static NativeLibrary findJvm() throws Throwable {
        Path jvmDir = Paths.get(System.getProperty("java.home"));
        Path maybeJre = jvmDir.resolve("jre");
        if (Files.isDirectory(maybeJre)) {
            jvmDir = maybeJre;
        }
        jvmDir = jvmDir.resolve("bin");
        String os = System.getProperty("os.name").toLowerCase();
        Path pathToJvm;
        if (os.contains("win")) {
            pathToJvm = findFirstFile(jvmDir, "server/jvm.dll", "client/jvm.dll");
        } else if (os.contains("nix") || os.contains("nux")) {
            pathToJvm = findFirstFile(jvmDir.getParent(), "lib/amd64/server/libjvm.so", "lib/i386/server/libjvm.so");
        } else {
            throw new RuntimeException("Unsupported OS (probably MacOS X): " + os);
        }
        return NATIVE_LIBRARY_LOADER.loadLibrary(pathToJvm.normalize().toString());
    }

    private static Path findFirstFile(Path directory, String... files) {
        for (String file : files) {
            Path path = directory.resolve(file);
            if (Files.exists(path)) return path;
        }
        throw new RuntimeException("Failed to find one of the required paths!: ");
    }

    private static abstract class NativeLibraryLoader {

        protected static final Class<?> CL_NATIVE_LIBRARY;
        protected static final Constructor<?> CNSTR_NATIVE_LIBRARY;

        static {
            try {
                CL_NATIVE_LIBRARY = Class.forName("java.lang.ClassLoader$NativeLibrary", true, null);
                //CNSTR_NATIVE_LIBRARY = LOOKUP.findConstructor(CL_NATIVE_LIBRARY, MethodType.methodType(Void.TYPE, Class.class, String.class, Boolean.TYPE))
                CNSTR_NATIVE_LIBRARY = ReflectionUtil.getConstructor(CL_NATIVE_LIBRARY, Class.class, String.class, boolean.class);
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }
        }

        abstract NativeLibrary loadLibrary(String path) throws Throwable;
    }

    private static class Java8LibraryLoader extends NativeLibraryLoader {

        private static final Method MH_NATIVE_LOAD;
        private static final Method MH_NATIVE_FIND;
        private static final Field MH_NATIVE_LOADED_RFIElD;

        static {
            try {
                Class<?> cl = Class.forName("java.lang.ClassLoader$NativeLibrary", true, null);
                Method tmp;
                try {
                    tmp = ReflectionUtil.getMethod(cl, "load", String.class, boolean.class, boolean.class);
                } catch (NoSuchMethodError e) {
                    tmp = ReflectionUtil.getMethod(cl, "load", String.class, boolean.class);
                }
                MH_NATIVE_LOAD = tmp;
                MH_NATIVE_LOAD.setAccessible(true);
                MH_NATIVE_FIND = ReflectionUtil.getMethod(cl, "find", String.class);
                MH_NATIVE_FIND.setAccessible(true);
                MH_NATIVE_LOADED_RFIElD = ReflectionUtil.getField(cl, "loaded");
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }

        }

        @Override
        NativeLibrary loadLibrary(String path) throws Throwable {
            Object library = CNSTR_NATIVE_LIBRARY.newInstance(JVMUtil.class, path, false);
            if (MH_NATIVE_LOAD.getParameterCount() == 3) {
                MH_NATIVE_LOAD.invoke(library, path, false, false);
            } else {
                MH_NATIVE_LOAD.invoke(library, path, false);
            }
            ObjectUtil.fillValue(MH_NATIVE_LOADED_RFIElD, library, true);
            return entry -> {
                try {
                    return (long) MH_NATIVE_FIND.invoke(library, entry);
                } catch (Throwable t) {
                    throw new InternalError(t);
                }
            };
        }
    }

    private static class Java9LibraryLoader extends NativeLibraryLoader {

        private static final MethodHandle MH_NATIVE_LOAD;
        private static final MethodHandle MH_NATIVE_FIND;

        static {
            MethodHandles.Lookup lookup = LOOKUP;
            Class<?> cl = CL_NATIVE_LIBRARY;
            try {
                MH_NATIVE_LOAD = lookup.findVirtual(cl, "load0", MethodType.methodType(Boolean.TYPE, String.class, Boolean.TYPE));
                MH_NATIVE_FIND = lookup.findVirtual(cl, "findEntry", MethodType.methodType(Long.TYPE, String.class));
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }

        }

        @Override
        NativeLibrary loadLibrary(String path) throws Throwable {
            Object library = CNSTR_NATIVE_LIBRARY.newInstance(JVMUtil.class, path, false);
            MH_NATIVE_LOAD.invoke(library, path, false);
            return entry -> {
                try {
                    return (long) MH_NATIVE_FIND.invoke(library, entry);
                } catch (Throwable t) {
                    throw new InternalError(t);
                }
            };
        }
    }
}
