package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class Entity {
    public static void setUniqueId(net.minecraft.entity.Entity entity, UUID uniqueIdIn) {
        if (entity.entityUniqueID != null) {
            return;
        }
        entity.entityUniqueID = uniqueIdIn;
        entity.cachedUniqueIdString = entity.entityUniqueID.toString();
    }

    public static NBTTagCompound writeToNBT(net.minecraft.entity.Entity entity, NBTTagCompound compound) {
        try {
            compound.setTag("Pos", entity.newDoubleNBTList(entity.X, entity.Y, entity.Z));
            compound.setTag("Motion", entity.newDoubleNBTList(entity.motionX, entity.motionY, entity.motionZ));
            compound.setTag("Rotation", entity.newFloatNBTList(entity.rotationYaw, entity.rotationPitch));
            compound.setFloat("FallDistance", entity.fallDistance);
            compound.setShort("Fire", (short) entity.fire);
            compound.setShort("Air", (short) entity.getAir());
            compound.setBoolean("OnGround", entity.onGround);
            compound.setInteger("Dimension", entity.dimension);
            compound.setBoolean("Invulnerable", entity.invulnerable);
            compound.setInteger("PortalCooldown", entity.timeUntilPortal);
            compound.setUniqueId("UUID", Objects.requireNonNull(entity.getUniqueID()));

            if (entity.hasCustomName()) {
                compound.setString("CustomName", entity.getCustomNameTag());
            }

            if (entity.getAlwaysRenderNameTag()) {
                compound.setBoolean("CustomNameVisible", entity.getAlwaysRenderNameTag());
            }

            entity.cmdResultStats.writeStatsToNBT(compound);

            if (entity.isSilent()) {
                compound.setBoolean("Silent", entity.isSilent());
            }

            if (entity.hasNoGravity()) {
                compound.setBoolean("NoGravity", entity.hasNoGravity());
            }

            if (entity.glowing) {
                compound.setBoolean("Glowing", entity.glowing);
            }
            compound.setBoolean("UpdateBlocked", entity.updateBlocked);

            if (!entity.tags.isEmpty()) {
                NBTTagList nbttaglist = new NBTTagList();

                for (String s : entity.tags) {
                    nbttaglist.appendTag(new NBTTagString(s));
                }

                compound.setTag("Tags", nbttaglist);
            }

            if (entity.customEntityData != null) compound.setTag("ForgeData", entity.customEntityData);
            if (entity.capabilities != null) compound.setTag("ForgeCaps", entity.capabilities.serializeNBT());

            entity.writeEntityToNBT(compound);

            if (entity.isBeingRidden()) {
                NBTTagList nbttaglist1 = new NBTTagList();

                for (net.minecraft.entity.Entity e : entity.getPassengers()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    if (e.writeToNBTAtomically(nbttagcompound)) {
                        nbttaglist1.appendTag(nbttagcompound);
                    }
                }

                if (!nbttaglist1.isEmpty()) {
                    compound.setTag("Passengers", nbttaglist1);
                }
            }

            return compound;
        } catch (Throwable throwable) {
            Launch.LOGGER.warn("Catch exception:", throwable);
            return new NBTTagCompound();
        }
    }

    public static void readFromNBT(net.minecraft.entity.Entity entity, NBTTagCompound compound) {
        try {
            NBTTagList nbttaglist = compound.getTagList("Pos", 6);
            NBTTagList nbttaglist2 = compound.getTagList("Motion", 6);
            NBTTagList nbttaglist3 = compound.getTagList("Rotation", 5);
            entity.motionX = nbttaglist2.getDoubleAt(0);
            entity.motionY = nbttaglist2.getDoubleAt(1);
            entity.motionZ = nbttaglist2.getDoubleAt(2);

            if (Math.abs(entity.motionX) > 10.0D) {
                entity.motionX = 0.0D;
            }

            if (Math.abs(entity.motionY) > 10.0D) {
                entity.motionY = 0.0D;
            }

            if (Math.abs(entity.motionZ) > 10.0D) {
                entity.motionZ = 0.0D;
            }

            entity.X = nbttaglist.getDoubleAt(0);
            entity.Y = nbttaglist.getDoubleAt(1);
            entity.Z = nbttaglist.getDoubleAt(2);
            entity.lastTickPosX = entity.X;
            entity.lastTickPosY = entity.Y;
            entity.lastTickPosZ = entity.Z;
            entity.prevPosX = entity.X;
            entity.prevPosY = entity.Y;
            entity.prevPosZ = entity.Z;
            entity.rotationYaw = nbttaglist3.getFloatAt(0);
            entity.rotationPitch = nbttaglist3.getFloatAt(1);
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;
            entity.setRotationYawHead(entity.rotationYaw);
            entity.setRenderYawOffset(entity.rotationYaw);
            entity.fallDistance = compound.getFloat("FallDistance");
            entity.fire = compound.getShort("Fire");
            entity.setAir(compound.getShort("Air"));
            entity.onGround = compound.getBoolean("OnGround");

            if (compound.hasKey("Dimension")) {
                entity.dimension = compound.getInteger("Dimension");
            }

            entity.invulnerable = compound.getBoolean("Invulnerable");
            entity.timeUntilPortal = compound.getInteger("PortalCooldown");

            if (compound.hasUniqueId("UUID")) {
                entity.entityUniqueID = compound.getUniqueId("UUID");
                entity.cachedUniqueIdString = Objects.requireNonNull(entity.entityUniqueID).toString();
            }

            entity.setPosition(entity.X, entity.Y, entity.Z);
            entity.setRotation(entity.rotationYaw, entity.rotationPitch);

            if (compound.hasKey("CustomName", 8)) {
                entity.setCustomNameTag(compound.getString("CustomName"));
            }

            entity.setAlwaysRenderNameTag(compound.getBoolean("CustomNameVisible"));
            entity.cmdResultStats.readStatsFromNBT(compound);
            entity.setSilent(compound.getBoolean("Silent"));
            entity.setNoGravity(compound.getBoolean("NoGravity"));
            entity.setGlowing(compound.getBoolean("Glowing"));
            entity.updateBlocked = compound.getBoolean("UpdateBlocked");

            if (compound.hasKey("ForgeData")) entity.customEntityData = compound.getCompoundTag("ForgeData");
            if (entity.capabilities != null && compound.hasKey("ForgeCaps"))
                entity.capabilities.deserializeNBT(compound.getCompoundTag("ForgeCaps"));

            if (compound.hasKey("Tags", 9)) {
                entity.tags.clear();
                NBTTagList nbttaglist1 = compound.getTagList("Tags", 8);
                int i = Math.min(nbttaglist1.tagCount(), 1024);

                for (int j = 0; j < i; ++j) {
                    entity.tags.add(nbttaglist1.getStringTagAt(j));
                }
            }

            entity.readEntityFromNBT(compound);

            if (entity.shouldSetPosAfterLoading()) {
                entity.setPosition(entity.X, entity.Y, entity.Z);
            }
        } catch (Throwable throwable) {
            Launch.LOGGER.warn("Catch exception:", throwable);
        }
    }
}
