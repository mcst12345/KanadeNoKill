package miku.lib.HSDB;

import kanade.kill.Empty;
import miku.lib.utils.InternalUtils;
import one.helfy.Type;
import sun.jvm.hotspot.debugger.MachineDescriptionAMD64;
import sun.jvm.hotspot.debugger.linux.LinuxDebuggerLocal;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HSDB {

    public synchronized static void getMultiply(String file, Type... args) {
        final boolean win = System.getProperty("os.name").startsWith("Windows");
        String jar = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
        if (win) {
            jar = jar.substring(1);
        }
        try {
            jar = URLDecoder.decode(jar, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
        }
        StringBuilder LAUNCH = new StringBuilder();
        String JAVA = System.getProperty("java.home");
        if (JAVA.endsWith("jre")) {
            String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
            if (win) {
                JavaHome = JavaHome + ".exe";
            }
            JavaHome = "\"" + JavaHome + "\" ";
            LAUNCH.insert(0, JavaHome);
        } else {
            String tmp = JAVA + File.separator + "bin" + File.separator + "java";
            if (win) {
                tmp = tmp + ".exe";
            }
            tmp = "\"" + tmp + "\" ";
            LAUNCH.insert(0, tmp);
        }

        LAUNCH.append("-cp \"").append(jar).append(win ? ";" : ":").append(SaJDI.sa_jdi.getAbsolutePath()).append("\" ").append("miku.lib.HSDB.HSDB ").append(getProcessId("Nope.")).append(" MULTIPLY ").append(file).append(" ");

        for (Type type : args) {
            LAUNCH.append(type.name).append(":").append(type.name.length()).append(" ");
        }

        System.out.println(LAUNCH);

        try {
            attach = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.valueOf(LAUNCH)}, null, (File) null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        InputStream is = attach.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                String tmp = reader.readLine();
                System.out.println("[attach]" + tmp);
                if (tmp == null) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        attach.destroy();
    }

    private static Process attach;

    public synchronized static long getSymbol(String s) {
        System.out.println("target:" + s);
        final boolean win = System.getProperty("os.name").startsWith("Windows");
        String jar = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
        if (win) {
            jar = jar.substring(1);
        }
        try {
            jar = URLDecoder.decode(jar, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
        }
        StringBuilder LAUNCH = new StringBuilder();
        String JAVA = System.getProperty("java.home");
        if (JAVA.endsWith("jre")) {
            String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
            if (win) {
                JavaHome = JavaHome + ".exe";
            }
            JavaHome = "\"" + JavaHome + "\" ";
            LAUNCH.insert(0, JavaHome);
        } else {
            String tmp = JAVA + File.separator + "bin" + File.separator + "java";
            if (win) {
                tmp = tmp + ".exe";
            }
            tmp = "\"" + tmp + "\" ";
            LAUNCH.insert(0, tmp);
        }

        LAUNCH.append("-cp \"").append(jar).append(win ? ";" : ":").append(SaJDI.sa_jdi.getAbsolutePath()).append("\" ").append("miku.lib.HSDB.HSDB ").append(getProcessId("Nope.")).append(" ").append(s).append(" raw");

        System.out.println(LAUNCH);

        try {
            attach = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.valueOf(LAUNCH)}, null, (File) null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        InputStream is = attach.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while (true) {
            try {
                String tmp = reader.readLine();
                System.out.println("[attach]" + tmp);
                if (tmp == null) {
                    break;
                }
                line = tmp;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(line);
        attach.destroy();
        return line == null ? 0 : Long.parseLong(line);
    }

    public synchronized static long getSymbol(Type type) {
        String s = type.name + ":" + type.name.length();
        final boolean win = System.getProperty("os.name").startsWith("Windows");
        String jar = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/mcst/shiyuan/Empty.class", "").replace("file:", "");
        if (win) {
            jar = jar.substring(1);
        }
        try {
            jar = URLDecoder.decode(jar, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
        }
        StringBuilder LAUNCH = new StringBuilder();
        String JAVA = System.getProperty("java.home");
        if (JAVA.endsWith("jre")) {
            String JavaHome = JAVA.substring(0, JAVA.length() - 3) + "bin" + File.separator + "java";
            if (win) {
                JavaHome = JavaHome + ".exe";
            }
            JavaHome = "\"" + JavaHome + "\" ";
            LAUNCH.insert(0, JavaHome);
        } else {
            String tmp = JAVA + File.separator + "bin" + File.separator + "java";
            if (win) {
                tmp = tmp + ".exe";
            }
            tmp = "\"" + tmp + "\" ";
            LAUNCH.insert(0, tmp);
        }

        LAUNCH.append("-cp \"").append(jar).append(win ? ";" : ":").append(SaJDI.sa_jdi.getAbsolutePath()).append("\" ").append("miku.lib.HSDB.HSDB ").append(getProcessId("Nope.")).append(" ").append(s);

        System.out.println(LAUNCH);

        try {
            attach = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", String.valueOf(LAUNCH)}, null, (File) null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        InputStream is = attach.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while (true) {
            try {
                String tmp = reader.readLine();
                System.out.println("[attach]" + tmp);
                if (tmp == null) {
                    break;
                }
                line = tmp;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        attach.destroy();
        try {
            return Long.parseLong(line);
        } catch (Throwable t) {
            System.out.println(line);
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static void main(String[] args) throws Throwable {
        String pid = args[0];
        Class<LinuxDebuggerLocal> clazz = LinuxDebuggerLocal.class;
        unsafe.ensureClassInitialized(clazz);
        Method lookup = getMethod(clazz, "lookupByName0", String.class, String.class);
        Method attach0 = getMethod(clazz, "attach0", int.class);
        Method detach0 = getMethod(clazz, "detach0");
        LinuxDebuggerLocal debugger = new LinuxDebuggerLocal(new MachineDescriptionAMD64(), false);
        attach0.setAccessible(true);
        debugger.attach(Integer.parseInt(pid));
        lookup.setAccessible(true);
        long l = (long) lookup.invoke(debugger, "libjvm.so", "__vt_10JavaThread");
        if (l == 0) {
            vt = "_ZTV";
        } else {
            vt = "__vt_";
        }
        if (!args[1].endsWith("MULTIPLY")) {
            String symbol = args[1];
            long result = (long) lookup.invoke(debugger, "libjvm.so", args.length == 2 ? vtblSymbolForType(symbol) : symbol);
            System.out.println(result);
        } else {
            File file = new File(args[2]);
            if (!file.exists()) {
                Files.createFile(file.toPath());
            } else {
                Files.delete(file.toPath());
                Files.createFile(file.toPath());
            }
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                for (int i = 3; i < args.length; i++) {
                    System.out.println(vtblSymbolForType(args[i]));
                    long s = (long) lookup.invoke(debugger, "libjvm.so", vtblSymbolForType(args[i]));
                    System.out.println(s);
                    randomAccessFile.writeLong(s);
                }
            }
        }
        detach0.invoke(debugger);
    }

    public static String vtblSymbolForType(String desc) {

        return vt + Integer.parseInt(desc.substring(desc.indexOf(':') + 1)) + desc.substring(0, desc.indexOf(':'));
    }

    private static String vt;

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

    private static final Method getDeclaredFields0;
    private static final Method getDeclaredConstructors0;
    private static final Method getDeclaredMethods0;

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

    private static String getProcessId(final String fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            // ignore
        }
        return fallback;
    }
}
