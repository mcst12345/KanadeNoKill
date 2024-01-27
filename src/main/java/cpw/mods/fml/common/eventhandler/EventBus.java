package cpw.mods.fml.common.eventhandler;

import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;

public class EventBus {
    private static int maxID = 0;
    public final int busID = maxID++;

    public boolean post(MinecartCollisionEvent minecartCollisionEvent) {
        return false;
    }
}
