package kanade.kill.util;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.entity.EntityBeaconBeam;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.CoreDump;
import kanade.kill.network.packets.KillCurrentPlayer;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.reflection.LateFields;
import kanade.kill.reflection.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class EntityUtil {
    public static final Set<UUID> blackHolePlayers = new HashSet<>();
    public static final Set<UUID> Dead = new HashSet<>();
    private static final Set<Class<?>> redefined = new HashSet<>();

    public static boolean holdKillItem(EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        return stack.getITEM() == ModMain.kill_item;
    }

    public static void summonThunder(Entity entity, int count) {
        World world = entity.WORLD;
        for (int i = 0; i < count; i++) {
            entity.WORLD.addWeatherEffect(new EntityLightningBolt(world, entity.X, entity.Y, entity.Z, true));
        }
    }

    public static synchronized void Kill(List<Entity> list) {
        Util.killing = true;
        for (Entity e : list) {
            if (KillItem.inList(e)) {
                continue;
            }
            Kill(e, false);
            if (e instanceof EntityLivingBase) {
                summonThunder(e, 4);
                Entity beacon_beam = new EntityBeaconBeam(e.WORLD);
                beacon_beam.X = e.X;
                beacon_beam.Y = e.Y;
                beacon_beam.Z = e.Z;
                beacon_beam.forceSpawn = true;
                e.WORLD.spawnEntity(beacon_beam);
            }
        }
        if (Config.fieldReset) {
            MinecraftForge.Event_bus.listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) ModMain.listeners;
            MinecraftForge.Event_bus.listenerOwners = (Map<Object, ModContainer>) ModMain.listenerOwners;
            ObjectUtil.ResetStatic();
            if (Config.SuperAttack) {
                if (Launch.Debug) {
                    Util.printStackTrace();
                }
                Launch.LOGGER.info("Fucking threads...");
                ThreadUtil.FuckThreads();
                Launch.LOGGER.info("Fucking objects...");
                NativeMethods.FuckObjects();
            }
        }
        Util.killing = false;
    }

    //Thread safe :)
    public static synchronized void SafeKill(Object entity, boolean reset) {
        if (!(entity instanceof Entity)) {
            return;
        }
        synchronized (kanade.kill.asm.hooks.MinecraftServer.futureTaskQueue) {
            kanade.kill.asm.hooks.MinecraftServer.AddTask(() -> Kill((Entity) entity, reset));
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity, boolean reset) {
        if (KillItem.inList(entity) || entity == null) return;
        try {
            MinecraftForge.Event_bus.unregister(entity);
        } catch (Throwable ignored) {
        }
        if (Launch.client) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.scheduledTasks.clear();
        }
        try {
            if (reset) {
                Util.killing = true;
            }
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                for (WorldServer world : server.Worlds) {
                    if (world.customTeleporters != null) {
                        world.customTeleporters.clear();
                    }
                }
            }
            UUID uuid = entity.getUniqueID();
            if (uuid != null) {
                Dead.add(uuid);
                NativeMethods.DeadAdd(uuid.hashCode());
            }
            World world = entity.WORLD;
            if (world.entities.getClass() != KanadeArrayList.class) {
                Unsafe.instance.putObjectVolatile(world, LateFields.loadedEntityList_offset, new KanadeArrayList<>(world.entities));
            }
            world.entities.remove(entity);
            Chunk chunk = world.getChunk(entity.chunkCoordX, entity.chunkCoordZ);
            ClassInheritanceMultiMap<Entity>[] entityLists = (ClassInheritanceMultiMap<Entity>[]) Unsafe.instance.getObjectVolatile(chunk, LateFields.entities_offset);
            for (ClassInheritanceMultiMap<Entity> map : entityLists) {
                Map<Class<?>, List> MAP = (Map<Class<?>, List>) Unsafe.instance.getObjectVolatile(map, LateFields.map_offset);
                List list = MAP.get(entity.getClass());
                if (list != null) {
                    list.remove(entity);
                }
            }
            chunk.markDirty();

            entity.isDead = true;
            Unsafe.instance.putObjectVolatile(entity, LateFields.HatedByLife_offset, true);
            entity.addedToChunk = false;
            if (entity instanceof EntityLivingBase) {
                DataParameter<Float> HEALTH = (DataParameter<Float>) Unsafe.instance.getObjectVolatile(LateFields.HEALTH_base, LateFields.HEALTH_offset);
                ((EntityDataManager) Unsafe.instance.getObjectVolatile(entity, LateFields.dataManager_offset)).set(HEALTH, 0.0f);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.Inventory = new InventoryPlayer(player);
                    player.enderChest = new InventoryEnderChest();
                    if (world.players.getClass() != KanadeArrayList.class) {
                        Unsafe.instance.putObjectVolatile(world, LateFields.playerEntities_offset, new KanadeArrayList<>(world.players));
                    }
                    world.players.remove(player);
                    if (player instanceof EntityPlayerMP) {
                        NetworkHandler.INSTANCE.sendMessageToPlayer(new KillCurrentPlayer(), (EntityPlayerMP) player);
                        if (Config.coreDumpAttack) {
                            NetworkHandler.INSTANCE.sendMessageToPlayer(new CoreDump(), (EntityPlayerMP) player);
                        }
                    }
                }
            }
            for (IWorldEventListener listener : entity.WORLD.eventListeners) {
                if (entity instanceof EntityLiving && listener instanceof PathWorldListener) {
                    ((PathWorldListener) listener).navigations.remove(((EntityLiving) entity).getNavigator());
                }
                if (world instanceof WorldServer && listener instanceof ServerWorldEventHandler) {
                    EntityTracker tracker = ((WorldServer) entity.WORLD).getEntityTracker();
                    tracker.untrack(entity);
                }
            }
            if (!entity.WORLD.isRemote) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity(entity.entityId, reset));
            }

            WorldInfo info = world.worldInfo;

            if (info != null) {
                if (info.playerTag != null) {
                    info.playerTag.tagMap.clear();
                }
                if (info.additionalProperties != null) {
                    info.additionalProperties.clear();
                }
                if (info.dimensionData != null) {
                    info.dimensionData.clear();
                }
            }
            if (reset) {
                if (Config.fieldReset) {
                    MinecraftForge.Event_bus.listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) ModMain.listeners;
                    MinecraftForge.Event_bus.listenerOwners = (Map<Object, ModContainer>) ModMain.listenerOwners;
                    ObjectUtil.ResetStatic();
                }
                if (Config.SuperAttack) {
                    if (Launch.Debug) {
                        Util.printStackTrace();
                    }
                    Launch.LOGGER.info("Fucking threads...");
                    ThreadUtil.FuckThreads();
                    Launch.LOGGER.info("Fucking objects...");
                    NativeMethods.FuckObjects();
                }
                Util.killing = false;
            }
            if (Config.redefineAttack) {
                ClassUtil.redefineClass(entity.getClass(), ClassUtil.generateClassBytes(((KanadeClassLoader) Launch.classLoader).untransformName(ReflectionUtil.getName(entity.getClass()))));
            }
        } catch (Throwable t) {
            Launch.LOGGER.fatal("The fuck?", t);
            if (reset) {
                Util.killing = false;
            }
        }
    }

    public static boolean isDead(Object obj) {
        if (obj == null) {
            return false;
        }
        if (NativeMethods.HaveDeadTag(obj)) {
            return true;
        }
        if (obj instanceof Entity) {
            Entity entity = (Entity) obj;
            return Dead.contains(entity.getUniqueID()) || entity.getUniqueID() != null && NativeMethods.DeadContain(entity.getUniqueID().hashCode()) || NativeMethods.HaveDeadTag(entity);
        } else if (obj instanceof UUID) {
            return Dead.contains(obj) || NativeMethods.HaveDeadTag(obj.hashCode());
        }
        return false;
    }

    public static boolean invHaveKillItem(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.Inventory;
        for (ItemStack stack : inventoryPlayer.armorInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        for (ItemStack stack : inventoryPlayer.mainInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        for (ItemStack stack : inventoryPlayer.offHandInventory) {
            if (stack.getITEM() == ModMain.kill_item) {
                return true;
            }
        }
        return false;
    }

    public static void clearNBT(Object nbt) {
        if (nbt instanceof NBTTagCompound) {
            ((NBTTagCompound) nbt).tagMap.clear();
        }
    }

    public static void updatePlayer(@Nonnull
                                    EntityPlayer player) {
        boolean blackhole = blackHolePlayers.contains(player.getUniqueID());
        player.getActivePotionEffects().clear();
        player.hurtTime = 0;
        player.addedToChunk = true;
        player.capabilities.allowEdit = true;
        player.capabilities.allowFlying = true;
        player.setScore(Integer.MAX_VALUE);
        player.updateBlocked = false;
        player.isAddedToWorld = true;
        player.forceSpawn = true;
        World world = player.WORLD;
        //player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(5);
        player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).setBaseValue(1024.D);
        if (world.players.getClass() != KanadeArrayList.class) {
            world.players = new KanadeArrayList<>(world.players);
        }
        if (!world.players.contains(player)) {
            world.players.add(player);
        }
        if (world.entities.getClass() != KanadeArrayList.class) {
            world.entities = new KanadeArrayList<>(world.entities);
        }
        if (!world.entities.contains(player)) {
            world.entities.add(player);
        }
        if (Config.particleEffect) {
            EnumParticleTypes type = blackhole ? EnumParticleTypes.PORTAL : EnumParticleTypes.ENCHANTMENT_TABLE;
            for (double x = player.X - 3d; x <= player.X + 3d; x += 0.1d) {
                world.spawnParticle(type, x, player.Y + 1d, player.Z + 4d, 0, 0, 0);
            }
            for (double x = player.X - 3d; x <= player.X + 3d; x += 0.1d) {
                world.spawnParticle(type, x, player.Y + 1d, player.Z - 4d, 0, 0, 0);
            }
            for (double z = player.Z - 3d; z <= player.Z + 3d; z += 0.1d) {
                world.spawnParticle(type, player.X + 4d, player.Y + 1d, z, 0, 0, 0);
            }
            for (double z = player.Z - 3d; z <= player.Z + 3d; z += 0.1d) {
                world.spawnParticle(type, player.X - 4d, player.Y + 1d, z, 0, 0, 0);
            }
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X + 4d, player.Y + 4d, player.Z + 4d, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X + 4d, player.Y + 4d, player.Z - 4d, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X - 4d, player.Y + 4d, player.Z + 4d, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, player.X - 4d, player.Y + 4d, player.Z - 4d, 0, 0, 0);
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X + 1.5d, y, player.Z, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X - 1.5d, y, player.Z, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X + 1d, y, player.Z - 1d, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X - 1d, y, player.Z + 1d, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X + 1d, y, player.Z + 1d, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X - 1d, y, player.Z - 1d, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X, y, player.Z + 1.5d, 0, 0, 0);
            }
            for (double y = player.Y - 1d; y <= player.Y + 3d; y += 0.5d) {
                world.spawnParticle(type, player.X, y, player.Z - 1.5d, 0, 0, 0);
            }
        }
        if (blackhole) {
            List<Entity> list = new ArrayList<>(world.entities);
            for (Entity e : list) {
                if (e != player) {
                    double dx = player.X - e.X;
                    double dy = player.Y - e.Y;
                    double dz = player.Z - e.Z;

                    double lensquared = dx * dx + dy * dy + dz * dz;
                    double len = Math.sqrt(lensquared);
                    double suckRange = Double.MAX_VALUE;
                    double lenn = len / suckRange;

                    if (len <= suckRange) {
                        double strength = (1 - lenn) * (1 - lenn);
                        double power = 0.5;

                        e.mX += (dx / len) * strength * power;
                        e.mY += (dy / len) * strength * power;
                        e.mZ += (dz / len) * strength * power;
                    }
                }
            }
        }
    }
}
