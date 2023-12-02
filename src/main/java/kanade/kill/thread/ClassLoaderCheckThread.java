package kanade.kill.thread;

import kanade.kill.Core;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import scala.concurrent.util.Unsafe;

@SuppressWarnings("unused")
public class ClassLoaderCheckThread extends Thread {
    public ClassLoaderCheckThread(ThreadGroup group) {
        super(group, "ClassLoaderCheckThread");
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("ClassLoaderCheckThread");
    }

    @Override
    public void run() {
        Core.LOGGER.info("ClassLoaderCheckThread started.");
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            ClassLoader old = (ClassLoader) Unsafe.instance.getObjectVolatile(thread, EarlyFields.contextClassLoader_offset);
            if (old == null || old.getClass() != KanadeClassLoader.class) {
                Unsafe.instance.putObjectVolatile(thread, EarlyFields.contextClassLoader_offset, KanadeClassLoader.INSTANCE);
            }
        }
        while (true) {
            Object loader = Unsafe.instance.getObjectVolatile(EarlyFields.classLoader_base, EarlyFields.classLoader_offset);
            if (loader.getClass() != KanadeClassLoader.class) {
                Unsafe.instance.putObjectVolatile(EarlyFields.classLoader_base, EarlyFields.classLoader_offset, KanadeClassLoader.INSTANCE);
            }
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                ClassLoader old = (ClassLoader) Unsafe.instance.getObjectVolatile(thread, EarlyFields.contextClassLoader_offset);
                if (old == null || old.getClass() != KanadeClassLoader.class) {
                    Core.LOGGER.warn("Someone has changed the classloader of " + thread.getName() + ". Resetting it,");
                    Unsafe.instance.putObjectVolatile(thread, EarlyFields.contextClassLoader_offset, KanadeClassLoader.INSTANCE);
                }
            }
        }
    }
}
