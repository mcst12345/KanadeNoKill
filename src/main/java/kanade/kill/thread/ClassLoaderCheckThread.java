package kanade.kill.thread;

import kanade.kill.Core;

public class ClassLoaderCheckThread extends Thread {
    public ClassLoaderCheckThread() {
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("ClassLoaderCheckThread");
    }

    @Override
    public void run() {
        Core.LOGGER.info("ClassLoaderCheckThread started.");
    }
}
