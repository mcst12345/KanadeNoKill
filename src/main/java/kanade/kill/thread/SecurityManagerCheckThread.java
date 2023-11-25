package kanade.kill.thread;

import kanade.kill.Core;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.KanadeSecurityManager;
import scala.concurrent.util.Unsafe;

public class SecurityManagerCheckThread extends Thread {
    public SecurityManagerCheckThread(ThreadGroup group) {
        super(group, "SecurityManagerCheckThread");
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("SecurityManagerCheckThread");
    }

    @Override
    public void run() {
        Core.LOGGER.info("SecurityManagerCheckThread started.");
        while (true) {
            Object old = Unsafe.instance.getObjectVolatile(EarlyFields.security_base, EarlyFields.security_offset);
            if (old.getClass() != KanadeSecurityManager.class) {
                Core.LOGGER.warn("Someone has changed the SecurityManager. Resetting it.");
                Unsafe.instance.putObjectVolatile(EarlyFields.security_base, EarlyFields.security_offset, KanadeSecurityManager.INSTANCE);
            }
        }
    }
}
