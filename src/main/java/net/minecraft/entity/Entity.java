package net.minecraft.entity;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class Entity implements ICommandSender, net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {

    public boolean isDead;
    public boolean addedToChunk;
    public boolean updateBlocked;
    public boolean isAddedToWorld;
    public boolean forceSpawn;
    public World world;
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
    public float prevRotationYaw;
    public float rotationYaw;
    public float prevRotationPitch;
    public float rotationPitch;
    public int ticksExisted;

    @Nullable
    public UUID getUniqueID() {
        return null;
    }

    public boolean isSneaking() {
        return false;
    }

    public Entity(World worldIn) {
    }

    protected abstract void entityInit();

    protected abstract void readEntityFromNBT(NBTTagCompound compound);

    protected abstract void writeEntityToNBT(NBTTagCompound compound);

    public World getEntityWorld() {
        return this.world;
    }

    @Nullable
    public MinecraftServer getServer() {
        return this.world.getMinecraftServer();
    }

    public boolean canUseCommand(int permLevel, String commandName) {
        return true;
    }

    public String getName() {
        return "";
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        return false;
    }

    @Override
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        return null;
    }

    public NBTTagCompound serializeNBT() {
        return null;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
    }

    public boolean isRiding() {
        return false;
    }

    public void updateRidden() {

    }

    public void onUpdate() {

    }
}
