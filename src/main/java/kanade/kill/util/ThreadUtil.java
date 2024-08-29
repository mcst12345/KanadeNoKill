package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.util.memory.MemoryHelper;

public class ThreadUtil {
    public static void StopThread(Thread t) {
        Launch.LOGGER.info("Stopping thread:{}", t.getName());
        t.stillborn = false;
        t.threadStatus = 2;
        t.suspend0();
    }

    public static void printThreads() {
        Launch.LOGGER.info("Printing threads");
        Launch.LOGGER.info("------------------------------------------------------------------------");
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            Launch.LOGGER.info("Thread:{}", t.getName());
            Launch.LOGGER.info("class:{}", MemoryHelper.getClassName(t.getClass()));
            Launch.LOGGER.info("Group:{}", t.getThreadGroup().getName());
            Launch.LOGGER.info("HaveTag:{}", NativeMethods.HaveTag(t, 25L));
            Launch.LOGGER.info("------------------------------------------------------------------------");
        }
    }

    public static int FuckThreads() {
        int i = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (!ObjectUtil.ModClass(MemoryHelper.getClassName(t.getClass())) || NativeMethods.HaveTag(t, 9)) {
                continue;
            }
            Launch.LOGGER.info("Killing thread:{}", t.getName());
            StopThread(t);
            i++;
        }
        return i;
    }
}
