package kanade.kill;

import net.minecraft.launchwrapper.Launch;
import scala.concurrent.util.Unsafe;

public class CheckThread extends Thread {
    public CheckThread() {
        this.setDaemon(true);
        this.setName("CheckThread");
    }

    @Override
    public void run() {
        System.out.println("CheckThread started.");
        while (true) {
            Object obj = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            if (!(obj instanceof TransformerList)) {
                System.out.println("Warn:Someone changed transformers field. Reset it.");
                Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, Core.lists);
            }
        }
    }

    @Override
    public void interrupt() {
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }
}
