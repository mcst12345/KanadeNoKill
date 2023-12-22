package net.minecraft.world;

import net.minecraft.entity.Entity;
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
    public List<IWorldEventListener> eventListeners;
    ;

    public Chunk getChunk(int x, int z) {
        return null;
    }

    public WorldInfo getWorldInfo() {
        return null;
    }
}
