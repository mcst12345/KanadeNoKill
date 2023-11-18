package kanade.kill;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;

public class Empty {
    public static void test(MinecraftServer server) {
        ArrayList<WorldServer> tmp = new ArrayList<WorldServer>();
        server.worlds = tmp.toArray(new WorldServer[0]);
        server.backup = tmp.toArray(new WorldServer[0]);
    }
}
