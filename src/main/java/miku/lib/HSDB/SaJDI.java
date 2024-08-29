package miku.lib.HSDB;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaJDI {
    static final File sa_jdi;

    static {
        Path jvmDir = Paths.get(System.getProperty("java.home"));
        if (jvmDir.endsWith("jre")) {
            jvmDir = jvmDir.getParent();
        }
        Path pathToJdi = jvmDir.resolve("lib/sa-jdi.jar");
        sa_jdi = pathToJdi.toFile();
        appendJar();
    }

    public static void appendJar() {
        if (!sa_jdi.exists()) {
            throw new IllegalStateException("sa-jdi.jar doesn't exist! Use JDK instead of JRE!");
        }
        System.out.println("append sa-jdi.jar.");
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(appClassLoader, sa_jdi.toURI().toURL());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
