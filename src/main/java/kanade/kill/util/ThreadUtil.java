package kanade.kill.util;

import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolImpl;
import kanade.kill.Launch;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.EarlyMethods;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.util.memory.MemoryHelper;
import scala.concurrent.util.Unsafe;

import java.util.List;
import java.util.Timer;

public class ThreadUtil {
    public static void StopThread(Thread t) {
        Launch.LOGGER.info("Stopping thread:{}", t.getName());
        try {
            ReflectionUtil.invoke(EarlyMethods.stop0, t, Unsafe.instance.allocateInstance(ThreadDeath.class));
        } catch (Throwable e) {
            Launch.LOGGER.warn(t);
            try {
                t.stop();
            } catch (Throwable th) {
                Launch.LOGGER.error(th);
            }
        }
    }

    public static void StopTimer(Timer timer) {
        Launch.LOGGER.info("Stopping timer:{}", timer.toString());
        Thread t = (Thread) Unsafe.instance.getObjectVolatile(timer, EarlyFields.TimerThread_offset);
        try {
            StopThread(t);
        } catch (Throwable ignored) {
        }
    }

    public static void StopThreadPool(ThreadPoolImpl pool) {
        Launch.LOGGER.info("Stopping threadPool:{}", pool.getName());
        List<?> workers = (List<?>) Unsafe.instance.getObjectVolatile(pool, EarlyFields.ThreadPoolWorkers_offset);
        for (Object object : workers) {
            ThreadUtil.StopThread((Thread) object);
        }
        ThreadGroup threadGroup = (ThreadGroup) Unsafe.instance.getObjectVolatile(pool, EarlyFields.ThreadPoolThreadGroup_offset);
        StopThreadGroup(threadGroup);
    }

    public static void StopThreadGroup(ThreadGroup group) {
        if (group.getName().equals("main")) {
            return;
        }
        Launch.LOGGER.info("Stopping threadGroup:{}", group.getName());
        /*ThreadGroup[] additional = (ThreadGroup[]) Unsafe.instance.getObjectVolatile(group,EarlyFields.ThreadGroupGroups_offset);
        if(additional != null){
            for(ThreadGroup g : additional){
                if(g == null){
                    continue;
                }
                StopThreadGroup(g);
            }
        }*/
        Unsafe.instance.putObjectVolatile(group, EarlyFields.ThreadGroupGroups_offset, null);
        Unsafe.instance.putIntVolatile(group, EarlyFields.ThreadGroupGroupsN_offset, 0);
        Unsafe.instance.putIntVolatile(group, EarlyFields.ThreadGroupThreadsN_offset, 0);
        Thread[] thread = (Thread[]) Unsafe.instance.getObjectVolatile(group, EarlyFields.ThreadGroupThreads_offset);
        if (thread != null) {
            for (Thread t : thread) {
                if (t == null) {
                    continue;
                }
                StopThread(t);
            }
        }
        Unsafe.instance.putObjectVolatile(group, EarlyFields.ThreadGroupThreads_offset, null);
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
            //if(t.getName().equals("Snooper Timer") || t.getName().startsWith("Netty Local Client IO") || t.getName().equals("File IO Thread") || t.getName().equals("Signal Dispatcher") || t.getName().startsWith("Netty Server IO") || t.getClass().getName().equals("paulscode.sound.StreamThread") || t.getName().equals("Server thread") || t.getName().equals("Reference Handler") || t.getName().equals("Narrator") || t.getClass().getName().equals("paulscode.sound.CommandThread") || t.getName().equals("AWT-XAWT") || t.getName().startsWith("Chunk Batcher") || t.getName().startsWith("Finalizer") || t.getName().equals("Timer hack thread") || t.getName().equals("Client thread") || t.getName().equals("Java2D Disposer")){
            //    continue;
            //}
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
