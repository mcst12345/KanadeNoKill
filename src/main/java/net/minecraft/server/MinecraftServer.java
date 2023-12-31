package net.minecraft.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;

public abstract class MinecraftServer implements ICommandSender, Runnable {
    public WorldServer[] worlds = new WorldServer[0];
    public WorldServer[] backup = new WorldServer[0];
    public ServerConfigurationManager serverConfigManager;

    public static long getCurrentTimeMillis() {
        return 0;
    }
}
