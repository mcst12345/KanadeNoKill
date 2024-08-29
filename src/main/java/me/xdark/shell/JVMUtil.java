


package me.xdark.shell;

import miku.lib.utils.InternalUtils;
import miku.lib.utils.ObjectUtils;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JVMUtil {
    public static final MethodHandles.Lookup LOOKUP;
    private static final Method getDeclaredFields0;
    private static final Method getDeclaredConstructors0;
    private static final Method getDeclaredMethods0;
    private static final NativeLibraryLoader NATIVE_LIBRARY_LOADER;
    private static final Unsafe UNSAFE = InternalUtils.getUnsafe();
    public static Path LIBJVM;

    public static byte[] getBytes(long addr, int size) {
        byte[] data = new byte[size];

        for (int i = 0; i < size; i++) {
            data[i] = UNSAFE.getByte(addr + i);
        }

        return data;
    }

    static {
        try {
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            getDeclaredConstructors0 = Class.class.getDeclaredMethod("getDeclaredConstructors0", boolean.class);
            getDeclaredConstructors0.setAccessible(true);
            getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            getDeclaredMethods0.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            Field field = getField(MethodHandles.Lookup.class, "IMPL_LOOKUP");
            LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(field),
                    UNSAFE.staticFieldOffset(field));
            NATIVE_LIBRARY_LOADER = Float.parseFloat(System.getProperty("java.class.version")) - 44 > 8 ? new Java9LibraryLoader() : new Java8LibraryLoader();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private JVMUtil() {
    }

    private static Field[] getFields(Class<?> clazz) {
        if (clazz == null) {
            return new Field[0];
        }
        try {
            return (Field[]) getDeclaredFields0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            return new Field[0];
        }
    }

    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new NoSuchFieldException(name);
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
        LIBJVM = pathToJvm;
        return NATIVE_LIBRARY_LOADER.loadLibrary(pathToJvm.normalize().toString());
    }

    private static Path findFirstFile(Path directory, String... files) {
        for (String file : files) {
            Path path = directory.resolve(file);
            if (Files.exists(path)) return path;
        }
        throw new RuntimeException("Failed to find one of the required paths!: ");
    }

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Constructor<?> constructor : getConstructors(clazz)) {
            if (arrayContentsEq(parameterTypes, constructor.getParameterTypes())) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new NoSuchMethodError();
    }

    private static Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            return (Constructor<?>[]) getDeclaredConstructors0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Constructor[0];
        }
    }

    private static Method[] getMethods(Class<?> clazz) {
        try {
            return (Method[]) getDeclaredMethods0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Method[0];
        }
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Method method : getMethods(clazz)) {
            if (method.getName().equals(name) && arrayContentsEq(parameterTypes, method.getParameterTypes())) {
                return method;
            }
        }
        throw new NoSuchMethodError(name);
    }

    public static String getProcessId() {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static abstract class NativeLibraryLoader {

        protected static final Class<?> CL_NATIVE_LIBRARY;
        protected static final Constructor<?> CNSTR_NATIVE_LIBRARY;

        static {
            try {
                CL_NATIVE_LIBRARY = Class.forName("java.lang.ClassLoader$NativeLibrary", true, null);
                //CNSTR_NATIVE_LIBRARY = LOOKUP.findConstructor(CL_NATIVE_LIBRARY, MethodType.methodType(Void.TYPE, Class.class, String.class, Boolean.TYPE))
                CNSTR_NATIVE_LIBRARY = getConstructor(CL_NATIVE_LIBRARY, Class.class, String.class, boolean.class);
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
                    tmp = getMethod(cl, "load", String.class, boolean.class, boolean.class);
                } catch (NoSuchMethodError e) {
                    tmp = getMethod(cl, "load", String.class, boolean.class);
                }
                MH_NATIVE_LOAD = tmp;
                MH_NATIVE_LOAD.setAccessible(true);
                MH_NATIVE_FIND = getMethod(cl, "find", String.class);
                MH_NATIVE_FIND.setAccessible(true);
                MH_NATIVE_LOADED_RFIElD = getField(cl, "loaded");
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
            ObjectUtils.fillValue(MH_NATIVE_LOADED_RFIElD, library, true);
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
