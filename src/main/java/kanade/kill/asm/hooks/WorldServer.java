package kanade.kill.asm.hooks;

import kanade.kill.item.KillItem;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntityStatus;

public class WorldServer {
    public static void setEntityState(net.minecraft.world.WorldServer world, Entity entityIn, byte state) {
        if (KillItem.inList(entityIn)) {
            if (state == 3 || state == 36 || state == 37 || state == 2) {
                return;
            }
        }
        world.getEntityTracker().sendToTrackingAndSelf(entityIn, new SPacketEntityStatus(entityIn, state));
    }
}
