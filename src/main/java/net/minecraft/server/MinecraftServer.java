package net.minecraft.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

public abstract class MinecraftServer implements ICommandSender, Runnable, IThreadListener, ISnooperInfo {
    public WorldServer[] worlds = new WorldServer[0];
    public WorldServer[] backup = new WorldServer[0];

    public static long getCurrentTimeMillis() {
        return 0;
    }

    public PlayerList getPlayerList() {
        return null;
    }

    public String folderName;
    public String worldName;

    public void saveAllWorlds(boolean b) {
    }

    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
    }
    ;
}
