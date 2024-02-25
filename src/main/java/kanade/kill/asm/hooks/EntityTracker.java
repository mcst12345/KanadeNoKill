package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import kanade.kill.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

public class EntityTracker {
    public static void tck(net.minecraft.entity.EntityTracker tracker) {
        if (Util.killing) {
            return;
        }
        List<EntityPlayerMP> list = new ArrayList<>();

        try {
            synchronized (tracker.entries) {
                for (EntityTrackerEntry entitytrackerentry : tracker.entries) {
                    entitytrackerentry.updatePlayerList(tracker.world.playerEntities);

                    if (entitytrackerentry.playerEntitiesUpdated) {
                        Entity entity = entitytrackerentry.getTrackedEntity();

                        if (entity instanceof EntityPlayerMP) {
                            list.add((EntityPlayerMP) entity);
                        }
                    }
                }

                for (EntityPlayerMP entityplayermp : list) {
                    for (EntityTrackerEntry entitytrackerentry1 : tracker.entries) {
                        if (entitytrackerentry1.getTrackedEntity() != entityplayermp) {
                            entitytrackerentry1.updatePlayerEntity(entityplayermp);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Launch.LOGGER.warn("Catch exception:", t);
        }
    }
}
