package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import net.minecraftforge.fml.common.event.FMLEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FMLModContainer {
    public static void handleModStateEvent(net.minecraftforge.fml.common.FMLModContainer container, FMLEvent event) {
        if (!container.eventMethods.containsKey(event.getClass())) {
            return;
        }
        for (Method m : container.eventMethods.get(event.getClass())) {
            try {
                m.invoke(container.modInstance, event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                Launch.LOGGER.error("Catch exception:", e.getCause() != null ? e.getCause() : e);
            } catch (Throwable t) {
                Launch.LOGGER.error("The fuck?", t.getCause() != null ? t.getCause() : t);
            }
        }
    }
}
