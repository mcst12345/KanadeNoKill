package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class World {
    public List<Entity> loadedEntityList;
    public List<Entity> protects;
    public List<Entity> playerEntities;
    private Profiler profiler;

    public Chunk getChunk(int x, int z) {
        return null;
    }

    public void updateEntities() {
        if (loadedEntityList.getClass() != ArrayList.class) {
            loadedEntityList = new ArrayList<>(loadedEntityList);
        }
        for (Entity e : protects) {
            if (!loadedEntityList.contains(e)) {
                loadedEntityList.add(e);
            }
        }

        this.profiler.startSection("entities");
        this.profiler.startSection("global");
    }
}
