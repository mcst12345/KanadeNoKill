package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.InternalUtils;
import kanade.kill.util.KanadeSecurityManager;
import sun.misc.Unsafe;

public class SecurityManagerThread extends Thread {
    private final Unsafe unsafe = InternalUtils.getUnsafe();

    public SecurityManagerThread(ThreadGroup group) {
        super(group, "SecurityManagerThread");
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("SecurityManagerThread");
    }

    @Override
    public void run() {
        Launch.LOGGER.info("SecurityManagerThread started.");
        while (true) {
            Object current = unsafe.getObjectVolatile(EarlyFields.security_base, EarlyFields.security_offset);
            if (current != KanadeSecurityManager.INSTANCE) {
                unsafe.putObjectVolatile(EarlyFields.security_base, EarlyFields.security_offset, KanadeSecurityManager.INSTANCE);
            }
        }
    }
}
