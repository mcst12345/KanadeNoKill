package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.util.NativeMethods;

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
            if (net.minecraft.launchwrapper.Launch.classLoader != Launch.classLoader) {
                net.minecraft.launchwrapper.Launch.classLoader = Launch.classLoader;
            }
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                ClassLoader old = thread.contextClassLoader;
                if (old == null || old.getClass() != KanadeClassLoader.class) {
                    thread.contextClassLoader = Launch.classLoader;
                }
            }
        }
    }
}
