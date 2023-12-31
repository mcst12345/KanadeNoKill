package net.minecraft.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class Entity {

    public boolean isDead;
    public boolean addedToChunk;
    public boolean forceSpawn;
    public World worldObj;
    public int chunkCoordX;
    public int chunkCoordZ;
    public int entityId;
    public int dimension;
    public int chunkCoordY;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;

    public Entity(World worldIn) {
    }

    @Nullable
    public UUID getUniqueID() {
        return null;
    }

    public boolean isSneaking() {
        return false;
    }

    protected abstract void entityInit();

    protected abstract void readEntityFromNBT(NBTTagCompound compound);

    protected abstract void writeEntityToNBT(NBTTagCompound compound);

    public World getEntityWorld() {
        return this.worldObj;
    }

    @Nullable
    public MinecraftServer getServer() {
        return this.worldObj.getMinecraftServer();
    }

    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }

    public String getName() {
        return "";
    }

    public NBTTagCompound serializeNBT() {
        return null;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
    }
}
