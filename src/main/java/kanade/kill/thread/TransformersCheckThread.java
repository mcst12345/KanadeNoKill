package kanade.kill.thread;

import kanade.kill.Core;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.TransformerList;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import scala.concurrent.util.Unsafe;

import java.util.List;

@SuppressWarnings("unused")
public class TransformersCheckThread extends Thread {
    public TransformersCheckThread(ThreadGroup group) {
        super(group, "TransformersCheckThread");
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("TransformersCheckThread");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Core.LOGGER.info("CheckThread started.");
        while (true) {
            Object obj = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            if (obj != Core.lists) {
                Core.LOGGER.warn("Someone has changed the transformers field. Resetting it.");
                List<IClassTransformer> New = new TransformerList<>((List<IClassTransformer>) obj);
                Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, New);
                Core.lists = New;
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
