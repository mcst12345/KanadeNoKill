package net.minecraftforge.fml.common.eventhandler;

public class EventBus {
    private static int maxID = 0;
    public final int busID = maxID++;

    public void register(Object o) {

    }

    public void unregister(Object object) {
    }

    public boolean post(Event event) {
        return kanade.kill.asm.hooks.EventBus.post(this, event);
    }
}
