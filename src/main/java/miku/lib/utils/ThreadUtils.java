package miku.lib.utils;

import kanade.kill.Launch;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ThreadUtils {
    public static void killThreads() {
        while (true) {
            Launch.LOGGER.info("Kill threads entered.");
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (ObjectUtils.FromModClass(thread)) {
                    Thread.defaultUncaughtExceptionHandler = null;
                    thread.uncaughtExceptionHandler = null;
                    thread.setPriority0(1);
                    thread.suspend0();
                } else if (thread.getName().startsWith("Timer-") || (thread.getName().startsWith("pool"))) {
                    Thread.defaultUncaughtExceptionHandler = null;
                    thread.uncaughtExceptionHandler = null;
                    thread.setPriority0(1);
                    thread.suspend0();
                } else if (thread.getName().startsWith("Thread-") && NumberUtils.isCreatable(thread.getName().substring(7))) {
                    Thread.defaultUncaughtExceptionHandler = null;
                    thread.uncaughtExceptionHandler = null;
                    thread.setPriority0(1);
                    thread.suspend0();
                }
            }
            Launch.LOGGER.info("Kill threads exited.");
            break;
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public static void printAllThreads() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            LOGGER.info(thread.getName());
        }
    }

    public static void forEach(Consumer<Thread> consumer) {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            consumer.accept(thread);
        }
    }
}
