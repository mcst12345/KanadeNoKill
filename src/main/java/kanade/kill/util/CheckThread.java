package kanade.kill.util;

import kanade.kill.Core;
import kanade.kill.reflection.EarlyFields;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import scala.concurrent.util.Unsafe;

import java.util.List;

@SuppressWarnings("unused")
public class CheckThread extends Thread {
    public CheckThread() {
        this.setPriority(9);
        this.setDaemon(true);
        this.setName("CheckThread");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        System.out.println("CheckThread started.");
        while (true) {
            Object obj = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            if (obj != Core.lists) {
                System.out.println("Warn:Someone changed transformers field. Reset it.");
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
