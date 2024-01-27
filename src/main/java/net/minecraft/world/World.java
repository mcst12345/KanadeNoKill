package net.minecraft.world;


import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class World implements IBlockAccess {
    public List<Entity> loadedEntityList;
    public List<Entity> entities;
    public List<Entity> protects;
    public List<Entity> playerEntities;
    public List<Entity> players;
    public boolean isRemote;
    public List<IWorldAccess> worldAccesses;

    public Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_) {
        return null;
    }

    @Override
    public TileEntity getTileEntity(int p_147438_1_, int p_147438_2_, int p_147438_3_) {
        return null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return 0;
    }

    @Override
    public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {
        return 0;
    }

    @Override
    public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {
        return 0;
    }

    @Override
    public boolean isAirBlock(int p_147437_1_, int p_147437_2_, int p_147437_3_) {
        return false;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_) {
        return null;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return false;
    }

    public void spawnParticle(String p_72869_1_, double p_72869_2_, double p_72869_4_, double p_72869_6_, double p_72869_8_, double p_72869_10_, double p_72869_12_) {
    }
    public boolean addWeatherEffect(Entity p_72942_1_) {
        return true;
    }
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

    public WorldInfo worldInfo;
}
