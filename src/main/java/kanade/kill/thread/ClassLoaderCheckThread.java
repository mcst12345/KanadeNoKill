package kanade.kill.thread;

import kanade.kill.Launch;
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
        Launch.LOGGER.info("ClassLoaderCheckThread started.");
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            ClassLoader old = (ClassLoader) Unsafe.instance.getObjectVolatile(thread, EarlyFields.contextClassLoader_offset);
            if (old == null || old.getClass() != KanadeClassLoader.class) {
                Unsafe.instance.putObjectVolatile(thread, EarlyFields.contextClassLoader_offset, Launch.classLoader);
            }
        }
        while (true) {
            Object loader = Unsafe.instance.getObjectVolatile(EarlyFields.LaunchClassLoader_base, EarlyFields.LaunchClassLoader_offset);
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                ClassLoader old = (ClassLoader) Unsafe.instance.getObjectVolatile(thread, EarlyFields.contextClassLoader_offset);
                if (old == null || old.getClass() != KanadeClassLoader.class) {
                    Launch.LOGGER.warn("Someone has changed the classloader of " + thread.getName() + ". Resetting it,");
                    Unsafe.instance.putObjectVolatile(thread, EarlyFields.contextClassLoader_offset, Launch.classLoader);
                }
            }
        }
    }
}
