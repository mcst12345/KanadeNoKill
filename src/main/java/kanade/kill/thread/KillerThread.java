package kanade.kill.thread;


import kanade.kill.util.Util;

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
            if (thread.getClass() != KillerThread.class && thread.getClass() != SecurityManagerCheckThread.class && thread.getClass() != TransformersCheckThread.class && thread.getClass() != ClassLoaderCheckThread.class) {
                good = !Util.FromModClass(thread);
            }

            if (!good) {
                kanade.kill.Launch.LOGGER.warn("Killing thread:" + thread.getName());
                try {
                    thread.stop();
                } catch (Throwable t) {
                    kanade.kill.Launch.LOGGER.warn("Failed to kill thread,", t);
                }
            }
        }
    }
}
