package kanade.kill.util;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.CoreDump;
import kanade.kill.network.packets.KillCurrentPlayer;
import kanade.kill.network.packets.KillEntity;
import kanade.kill.reflection.LateFields;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import scala.concurrent.util.Unsafe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityUtil {
    public static void summonThunder(Entity entity, int count) {
        World world = entity.worldObj;
        for (int i = 0; i < count; i++) {
            entity.worldObj.addWeatherEffect(new EntityLightningBolt(world, entity.posX, entity.posY, entity.posZ));
        }

    }

    public static synchronized void Kill(List<Entity> list) {
        Util.killing = true;
        for (Entity e : list) {
            Kill(e, false);
            if (e instanceof EntityLivingBase) {
                summonThunder(e, 5);
            }
        }
        if (Config.fieldReset) {
            Util.reset();
        }
        Util.killing = false;
    }

    @SuppressWarnings("unchecked")
    public static synchronized void Kill(Entity entity, boolean reset) {
        if (KillItem.inList(entity) || entity == null) return;
        try {
            if (reset) {
                Util.killing = true;
            }
            UUID uuid = entity.getUniqueID();
            if (uuid != null) {
                Util.Dead.add(uuid);
                NativeMethods.DeadAdd(uuid.hashCode());
            }
            World world = entity.worldObj;
            if (world.entities.getClass() != ArrayList.class) {
                Unsafe.instance.putObjectVolatile(world, LateFields.loadedEntityList_offset, new ArrayList<>(world.entities));
            }
            world.entities.remove(entity);
            Chunk chunk = world.getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ);
            List[] entities = (List[]) Unsafe.instance.getObjectVolatile(chunk, LateFields.entities_offset);

            int index = entity.chunkCoordY;

            if (index < 0) {
                index = 0;
            }

            if (index >= entities.length) {
                index = entities.length - 1;
            }

            if (entities[index].getClass() != ArrayList.class) {
                entities[index] = new ArrayList<>(entities[index]);
            }

            entities[index].remove(entity);

            entity.isDead = true;
            Unsafe.instance.putObjectVolatile(entity, LateFields.HatedByLife_offset, true);
            entity.addedToChunk = false;
            if (entity instanceof EntityLivingBase) {
                ((DataWatcher) Unsafe.instance.getObjectVolatile(entity, LateFields.dataManager_offset)).updateObject(6, 0.0f);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.Inventory = new InventoryPlayer(player);
                    player.theInventoryEnderChest = new InventoryEnderChest();
                    if (world.players.getClass() != ArrayList.class) {
                        Unsafe.instance.putObjectVolatile(world, LateFields.playerEntities_offset, new ArrayList<>(world.players));
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
            for (IWorldAccess listener : entity.worldObj.worldAccesses) {
                if (world instanceof WorldServer && listener instanceof WorldManager) {
                    EntityTracker tracker = ((WorldServer) entity.worldObj).getEntityTracker();
                    tracker.removeEntityFromAllTrackingPlayers(entity);
                }
            }
            if (!entity.worldObj.isRemote) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity(entity.entityId));
            }

            if (world.worldInfo != null) {
                WorldInfo info = world.worldInfo;
                if (info.additionalProperties != null) {
                    info.additionalProperties.clear();
                }
                if (info.playerTag != null) {
                    info.playerTag.tagMap.clear();
                }
            }


            if (reset) {
                if (Config.fieldReset) {
                    Util.reset();
                }
                Util.killing = false;
            }
        } catch (Throwable t) {
            Launch.LOGGER.fatal(t);
            if (reset) {
                Util.killing = false;
            }
        }
    }

    public static boolean isDead(Entity entity) {
        return entity == null || Util.Dead.contains(entity.getUniqueID()) || (entity.getUniqueID() != null && NativeMethods.DeadContain(entity.getUniqueID().hashCode())) || NativeMethods.HaveDeadTag(entity);
    }

    public static boolean invHaveKillItem(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.Inventory;
        for (int i = 0; i < inventoryPlayer.mainInv.length; i++) {
            ItemStack stack = inventoryPlayer.mainInv[i];
            if (stack == null) {
                continue;
            }
            if (stack.getITEM() == ModMain.kill_item) {
                Util.item.computeIfAbsent(player.getUniqueID(), k -> new HashMap<>());
                Util.item.get(player.getUniqueID()).put(i, stack);
                return true;
            }
        }
        return false;
    }

    public static void updatePlayer(EntityPlayer player) {
        player.getActivePotionEffects().clear();
        player.hurtTime = 0;
        player.addedToChunk = true;
        player.capabilities.allowEdit = true;
        player.capabilities.allowFlying = true;
        player.setScore(Integer.MAX_VALUE);
        player.forceSpawn = true;
        World world = player.worldObj;
        if (world.players.getClass() != ArrayList.class) {
            world.players = new ArrayList<>(world.players);
        }
        if (!world.players.contains(player)) {
            world.players.add(player);
        }
        if (world.entities.getClass() != ArrayList.class) {
            world.entities = new ArrayList<>(world.entities);
        }
        if (!world.entities.contains(player)) {
            world.entities.add(player);
        }
        if (Util.item.containsKey(player.getUniqueID())) {
            Util.item.get(player.getUniqueID()).forEach((i, k) -> {
                player.Inventory.mainInv[i] = k;
            });
        }
    }
}
