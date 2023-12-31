package net.minecraft.world;


import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;

import java.util.List;

public class World {
    public List<Entity> loadedEntityList;
    public List<Entity> entities;
    public List<Entity> protects;
    public List<Entity> playerEntities;
    public List<Entity> players;
    public boolean isRemote;
    public List<IWorldAccess> worldAccesses;

    public Chunk getChunkFromChunkCoords(int x, int z) {
        return null;
    }

    public WorldInfo getWorldInfo() {
        return null;
    }

    public boolean setBlock(int p_147465_1_, int p_147465_2_, int p_147465_3_, Block p_147465_4_, int p_147465_5_, int p_147465_6_) {
        return true;
    }

    public MinecraftServer getMinecraftServer() {
        return null;
    }
}
