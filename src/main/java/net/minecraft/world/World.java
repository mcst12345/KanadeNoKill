package net.minecraft.world;


import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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

    public WorldInfo worldInfo;

    public WorldInfo getWorldInfo() {
        return null;
    }

    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return false;
    }

    public MinecraftServer getMinecraftServer() {
        return null;
    }

    public boolean addWeatherEffect(Entity entityLightningBolt) {
        return false;
    }
}
