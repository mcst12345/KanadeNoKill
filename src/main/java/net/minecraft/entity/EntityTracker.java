package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

public class EntityTracker {
    private static final Logger LOGGER = LogManager.getLogger();
    public final WorldServer world;
    public final Set<EntityTrackerEntry> entries = Sets.newHashSet();
    private final IntHashMap<EntityTrackerEntry> trackedEntityHashTable = new IntHashMap<>();
    private int maxTrackingDistanceThreshold;

    public EntityTracker(WorldServer theWorldIn) {
        this.world = theWorldIn;
        this.maxTrackingDistanceThreshold = theWorldIn.getMinecraftServer().getPlayerList().getEntityViewDistance();
    }

    public static long getPositionLong(double value) {
        return MathHelper.lfloor(value * 4096.0D);
    }

    @SideOnly(Side.CLIENT)
    public static void updateServerPosition(Entity entityIn, double x, double y, double z) {
        entityIn.serverPosX = getPositionLong(x);
        entityIn.serverPosY = getPositionLong(y);
        entityIn.serverPosZ = getPositionLong(z);
    }

    public void track(Entity entityIn) {
        if (net.minecraftforge.fml.common.registry.EntityRegistry.instance().tryTrackingEntity(this, entityIn)) return;

        if (entityIn instanceof EntityPlayerMP) {
            this.track(entityIn, 512, 2);
            EntityPlayerMP entityplayermp = (EntityPlayerMP) entityIn;

            for (EntityTrackerEntry entitytrackerentry : this.entries) {
                if (entitytrackerentry.getTrackedEntity() != entityplayermp) {
                    entitytrackerentry.updatePlayerEntity(entityplayermp);
                }
            }
        } else if (entityIn instanceof EntityFishHook) {
            this.track(entityIn, 64, 5, true);
        } else if (entityIn instanceof EntityArrow) {
            this.track(entityIn, 64, 20, false);
        } else if (entityIn instanceof EntitySmallFireball) {
            this.track(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntityFireball) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntitySnowball) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityLlamaSpit) {
            this.track(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntityEnderPearl) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityEnderEye) {
            this.track(entityIn, 64, 4, true);
        } else if (entityIn instanceof EntityEgg) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityPotion) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityExpBottle) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityFireworkRocket) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityItem) {
            this.track(entityIn, 64, 20, true);
        } else if (entityIn instanceof EntityMinecart) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityBoat) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntitySquid) {
            this.track(entityIn, 64, 3, true);
        } else if (entityIn instanceof EntityWither) {
            this.track(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityShulkerBullet) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityBat) {
            this.track(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityDragon) {
            this.track(entityIn, 160, 3, true);
        } else if (entityIn instanceof IAnimals) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityTNTPrimed) {
            this.track(entityIn, 160, 10, true);
        } else if (entityIn instanceof EntityFallingBlock) {
            this.track(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityHanging) {
            this.track(entityIn, 160, Integer.MAX_VALUE, false);
        } else if (entityIn instanceof EntityArmorStand) {
            this.track(entityIn, 160, 3, true);
        } else if (entityIn instanceof EntityXPOrb) {
            this.track(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityAreaEffectCloud) {
            this.track(entityIn, 160, Integer.MAX_VALUE, true);
        } else if (entityIn instanceof EntityEnderCrystal) {
            this.track(entityIn, 256, Integer.MAX_VALUE, false);
        } else if (entityIn instanceof EntityEvokerFangs) {
            this.track(entityIn, 160, 2, false);
        }
    }

    public void track(Entity entityIn, int trackingRange, int updateFrequency) {
        this.track(entityIn, trackingRange, updateFrequency, false);
    }

    public void track(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates) {
        try {
            if (this.trackedEntityHashTable.containsItem(entityIn.getEntityId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }

            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entityIn, trackingRange, this.maxTrackingDistanceThreshold, updateFrequency, sendVelocityUpdates);
            this.entries.add(entitytrackerentry);
            this.trackedEntityHashTable.addKey(entityIn.getEntityId(), entitytrackerentry);
            entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding entity to track");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
            crashreportcategory.addCrashSection("Tracking range", trackingRange + " blocks");
            crashreportcategory.addDetail("Update interval", () -> {
                String s = "Once per " + updateFrequency + " ticks";

                if (updateFrequency == Integer.MAX_VALUE) {
                    s = "Maximum (" + s + ")";
                }

                return s;
            });
            entityIn.addEntityCrashInfo(crashreportcategory);
            this.trackedEntityHashTable.lookup(entityIn.getEntityId()).getTrackedEntity().addEntityCrashInfo(crashreport.makeCategory("Entity That Is Already Tracked"));

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                LOGGER.error("\"Silently\" catching entity tracking error.", reportedexception);
            }
        }
    }

    public void untrack(Entity entityIn) {
        if (entityIn instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) entityIn;

            for (EntityTrackerEntry entitytrackerentry : this.entries) {
                entitytrackerentry.removeFromTrackedPlayers(entityplayermp);
            }
        }

        EntityTrackerEntry entitytrackerentry1 = this.trackedEntityHashTable.removeObject(entityIn.getEntityId());

        if (entitytrackerentry1 != null) {
            this.entries.remove(entitytrackerentry1);
            entitytrackerentry1.sendDestroyEntityPacketToTrackedPlayers();
        }
    }

    public void tick() {
        List<EntityPlayerMP> list = Lists.newArrayList();

        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            entitytrackerentry.updatePlayerList(this.world.playerEntities);

            if (entitytrackerentry.playerEntitiesUpdated) {
                Entity entity = entitytrackerentry.getTrackedEntity();

                if (entity instanceof EntityPlayerMP) {
                    list.add((EntityPlayerMP) entity);
                }
            }
        }

        for (EntityPlayerMP entityplayermp : list) {
            for (EntityTrackerEntry entitytrackerentry1 : this.entries) {
                if (entitytrackerentry1.getTrackedEntity() != entityplayermp) {
                    entitytrackerentry1.updatePlayerEntity(entityplayermp);
                }
            }
        }
    }

    public void updateVisibility(EntityPlayerMP player) {
        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            if (entitytrackerentry.getTrackedEntity() == player) {
                entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
            } else {
                entitytrackerentry.updatePlayerEntity(player);
            }
        }
    }

    public void sendToTracking(Entity entityIn, Packet<?> packetIn) {
        EntityTrackerEntry entitytrackerentry = this.trackedEntityHashTable.lookup(entityIn.getEntityId());

        if (entitytrackerentry != null) {
            entitytrackerentry.sendPacketToTrackedPlayers(packetIn);
        }
    }

    /* ======================================== FORGE START =====================================*/

    // don't expose the EntityTrackerEntry directly so mods can't mess with the data in there as easily

    /**
     * Get all players tracking the given Entity. The Entity must be part of the World that this Tracker belongs to.
     *
     * @param entity the Entity
     * @return all players tracking the Entity
     */
    public Set<? extends net.minecraft.entity.player.EntityPlayer> getTrackingPlayers(Entity entity) {
        EntityTrackerEntry entry = trackedEntityHashTable.lookup(entity.getEntityId());
        if (entry == null)
            return java.util.Collections.emptySet();
        else
            return java.util.Collections.unmodifiableSet(entry.trackingPlayers);
    }

    /* ======================================== FORGE END   =====================================*/

    public void sendToTrackingAndSelf(Entity entityIn, Packet<?> packetIn) {
        EntityTrackerEntry entitytrackerentry = this.trackedEntityHashTable.lookup(entityIn.getEntityId());

        if (entitytrackerentry != null) {
            entitytrackerentry.sendToTrackingAndSelf(packetIn);
        }
    }

    public void removePlayerFromTrackers(EntityPlayerMP player) {
        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            entitytrackerentry.removeTrackedPlayerSymmetric(player);
        }
    }

    public void sendLeashedEntitiesInChunk(EntityPlayerMP player, Chunk chunkIn) {
        List<Entity> list = Lists.newArrayList();
        List<Entity> list1 = Lists.newArrayList();

        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            Entity entity = entitytrackerentry.getTrackedEntity();

            if (entity != player && entity.chunkCoordX == chunkIn.x && entity.chunkCoordZ == chunkIn.z) {
                entitytrackerentry.updatePlayerEntity(player);

                if (entity instanceof EntityLiving && ((EntityLiving) entity).getLeashHolder() != null) {
                    list.add(entity);
                }

                if (!entity.getPassengers().isEmpty()) {
                    list1.add(entity);
                }
            }
        }

        if (!list.isEmpty()) {
            for (Entity entity1 : list) {
                player.connection.sendPacket(new SPacketEntityAttach(entity1, ((EntityLiving) entity1).getLeashHolder()));
            }
        }

        if (!list1.isEmpty()) {
            for (Entity entity2 : list1) {
                player.connection.sendPacket(new SPacketSetPassengers(entity2));
            }
        }
    }

    public void setViewDistance(int distance) {
        this.maxTrackingDistanceThreshold = (distance - 1) * 16;

        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            entitytrackerentry.setMaxRange(this.maxTrackingDistanceThreshold);
        }
    }
}
