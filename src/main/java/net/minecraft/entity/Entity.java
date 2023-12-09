package net.minecraft.entity;

import net.minecraft.world.World;

import java.util.UUID;

public class Entity {

    public boolean isDead;
    public boolean addedToChunk;
    public boolean updateBlocked;
    public boolean isAddedToWorld;
    public boolean forceSpawn;
    public World world;
    public int chunkCoordX;
    public int chunkCoordZ;

    public UUID getUniqueID() {
        return null;
    }

    public boolean isSneaking() {
        return false;
    }

}
