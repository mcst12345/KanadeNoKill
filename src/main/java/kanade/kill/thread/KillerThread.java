package kanade.kill.thread;

import kanade.kill.Core;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.net.URL;

@SuppressWarnings("unused")
public class KillerThread extends Thread {
    public KillerThread(ThreadGroup group) {
        super(group, "KillerThread");
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("KillerThread");
    }

    @Override
    public void run() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            boolean good = true;
            if (thread.getClass() != KillerThread.class && thread.getClass() != SecurityManagerCheckThread.class && thread.getClass() != GuiThread.class && thread.getClass() != TransformersCheckThread.class && thread.getClass() != ClassLoaderCheckThread.class) {
                final URL res = Launch.classLoader.findResource(thread.getClass().getName().replace('.', '/').concat(".class"));
                if (res != null) {
                    String path = res.getPath();

                    if (path.contains("!")) {
                        path = path.substring(0, path.indexOf("!"));
                    }
                    if (path.contains("file:/")) {
                        path = path.replace("file:/", "");
                    }

                    if (path.startsWith("mods", path.lastIndexOf(File.separator) - 4)) {
                        good = false;
                    }
                }
            }

            if (!good) {
                Core.LOGGER.warn("Killing thread:" + thread.getName());
                try {
                    thread.stop();
                } catch (Throwable t) {
                    Core.LOGGER.warn("Failed to kill thread,", t);
                }
            }
        }
    }
}
