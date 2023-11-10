package net.minecraft.entity;

import net.minecraft.world.World;

import java.util.UUID;

public class Entity {

    public boolean isDead;
    public boolean addedToChunk;
    public int dimension;
    public World world;
    public int chunkCoordX;
    public int chunkCoordZ;

    public UUID getUniqueID() {
        return null;
    }
}
