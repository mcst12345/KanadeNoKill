package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

@SuppressWarnings("unused")
public class DimensionManager {
    public static boolean checkWorld(World world) {
        Launch.LOGGER.info("Checking world:" + world);
        try {
            return world == null || (world.getClass() != WorldServer.class && (Launch.client && world.getClass() != WorldClient.class));
        } catch (Throwable t) {
            Launch.LOGGER.info("The fuck?", t);
            return true;
        }
    }
}
