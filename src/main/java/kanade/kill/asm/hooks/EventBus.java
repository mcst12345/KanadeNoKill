package kanade.kill.asm.hooks;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.util.Util;

public class EventBus {
    public static boolean post(cpw.mods.fml.common.eventhandler.EventBus bus, Event event) {
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
        return (event.isCancelable() && event.isCanceled());
    }
}
