package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.util.InternalUtils;
import kanade.kill.util.KanadeSecurityManager;
import kanade.kill.util.NativeMethods;
import sun.misc.Unsafe;

public class SecurityManagerThread extends Thread {
    private final Unsafe unsafe = InternalUtils.getUnsafe();

    public SecurityManagerThread(ThreadGroup group) {
        super(group, "SecurityManagerThread");
        NativeMethods.SetTag(this, 9);
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("SecurityManagerThread");
    }

    @Override
    public void run() {
        Launch.LOGGER.info("SecurityManagerThread started.");
        while (true) {
            Object current = System.security;
            if (current != KanadeSecurityManager.INSTANCE) {
                System.security = KanadeSecurityManager.INSTANCE;
            }
        }
    }
}
