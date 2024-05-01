package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.NativeMethods;
import scala.concurrent.util.Unsafe;

@SuppressWarnings("unused")
public class ClassLoaderCheckThread extends Thread {
    public ClassLoaderCheckThread(ThreadGroup group) {
        super(group, "ClassLoaderCheckThread");
        NativeMethods.SetTag(this, 9);
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("ClassLoaderCheckThread");
    }

    @Override
    public void run() {
        Launch.LOGGER.info("ClassLoaderCheckThread started.");
        while (true) {
            Object loader = Unsafe.instance.getObjectVolatile(EarlyFields.Launch_classLoader_base, EarlyFields.Launch_classLoader_offset);
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                ClassLoader old = (ClassLoader) Unsafe.instance.getObjectVolatile(thread, EarlyFields.contextClassLoader_offset);
                if (old == null || old.getClass() != KanadeClassLoader.class) {
                    Unsafe.instance.putObjectVolatile(thread, EarlyFields.contextClassLoader_offset, Launch.classLoader);
                }
            }
        }
    }
}
