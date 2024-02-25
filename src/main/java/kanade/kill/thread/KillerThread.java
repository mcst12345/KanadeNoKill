package kanade.kill.thread;


import kanade.kill.util.ObjectUtil;
import kanade.kill.util.ThreadUtil;

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
            if (thread != this && thread.getClass() != ClassLoaderCheckThread.class) {
                good = !ObjectUtil.FromModClass(thread);
            }

            if (!good) {
                kanade.kill.Launch.LOGGER.warn("Killing thread:" + thread.getName());
                try {
                    ThreadUtil.StopThread(thread);
                } catch (Throwable t) {
                    kanade.kill.Launch.LOGGER.warn("Failed to kill thread,", t);
                }
            }
        }
    }
}
