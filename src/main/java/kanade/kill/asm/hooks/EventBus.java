package kanade.kill.asm.hooks;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.util.Util;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;

public class EventBus {
    public static boolean post(net.minecraftforge.fml.common.eventhandler.EventBus bus, Event event) {
        if (Config.disableEvent || !Util.shouldPostEvent(event)) {
            return false;
        }
        IEventListener[] listeners = event.getListenerList().getListeners(bus.busID);
        int index = 0;
        try {
            for (; index < listeners.length; index++) {
                listeners[index].invoke(event);
            }
        } catch (NullPointerException ignored) {
        } catch (Throwable throwable) {
            Launch.LOGGER.warn(throwable);
        }
        return event.isCancelable() && event.isCanceled();
    }
}
