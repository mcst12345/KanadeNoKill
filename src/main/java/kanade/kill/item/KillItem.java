package kanade.kill.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kanade.kill.Config;
import kanade.kill.Core;
import kanade.kill.Launch;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.Annihilation;
import kanade.kill.network.packets.KillAllEntities;
import kanade.kill.network.packets.ServerTimeStop;
import kanade.kill.timemanagement.TimeStop;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
public class KillItem extends Item {
    public static int mode = 0;
    private short timeStopCounter;
    private static final Set<UUID> list = new HashSet<>();

    public KillItem() {
        this.setTextureName("kanade:kill");
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    public static void AddToList(Object obj) {
        if (obj instanceof Entity) {
            UUID uuid = ((Entity) obj).getUniqueID();
            if (uuid != null) {
                list.add(uuid);
                NativeMethods.ProtectAdd(uuid.hashCode());
            }
        } else if (Launch.client) {
            if (obj instanceof Minecraft) {
                UUID uuid = ((Minecraft) obj).PLAYER.getUniqueID();
                if (uuid != null) {
                    list.add(uuid);
                    NativeMethods.ProtectAdd(uuid.hashCode());
                }
            }
        }
    }

    public static boolean inList(Object obj) {
        if (obj instanceof Entity) {
            if (Config.allPlayerProtect) {
                if (obj instanceof EntityPlayer) {
                    return true;
                }
            }
            UUID uuid = ((Entity) obj).getUniqueID();
            return list.contains(uuid) || (uuid != null && NativeMethods.ProtectContain(uuid.hashCode())) || (obj instanceof EntityItem && Util.NoRemove(((EntityItem) obj).getEntityItem()));
        } else if (Launch.client) {
            if (obj instanceof Minecraft) {
                EntityPlayer player = ((Minecraft) obj).PLAYER;
                if (player != null) {
                    return list.contains(player.getUniqueID());
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    @Nonnull
    public Item setFull3D() {
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return false;
    }

    @Override
    @Nonnull
    public String getUnlocalizedName(@Nullable ItemStack stack) {
        return "item.kanade.kill";
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nullable ItemStack stack) {
        return "Kill";
    }

    @Override
    public int getItemEnchantability(@Nullable ItemStack stack) {
        return 0;
    }

    @Override
    public boolean onLeftClickEntity(@Nullable ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        EntityUtil.summonThunder(entity, 20);
        EntityUtil.Kill(entity, true);
        return true;
    }

    @Override
    public void onUpdate(@Nullable ItemStack stack, @Nullable World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayer) {
            entityIn.worldObj.protects.add(entityIn);
            list.add(entityIn.getUniqueID());
        }
        if (timeStopCounter > 0) {
            timeStopCounter--;
        }
    }

    @Nonnull
    public ItemStack onItemRightClick(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull EntityPlayer playerIn) {
        if (!playerIn.isSneaking()) {
            synchronized (Util.tasks) {
                Util.tasks.add(() -> {
                    List<Entity> targets = new ArrayList<>();
                    for (int id : DimensionManager.getIDs()) {
                        WorldServer world = DimensionManager.getWorld(id);
                        targets.addAll(world.entities);
                    }
                    EntityUtil.Kill(targets);
                });
            }
            if (Thread.currentThread().getName().equals("Server thread")) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillAllEntities());
            }
        } else {
            if (mode == 0) {
                Config.Annihilation = !Config.Annihilation;
                if (playerIn.worldObj.isRemote) {
                    NetworkHandler.INSTANCE.sendMessageToServer(new Annihilation(playerIn.dimension, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ));
                }
            } else if (mode == 1) {
                if (!Core.isDemo) {
                    if (playerIn.worldObj.isRemote) {
                        if (timeStopCounter == 0) {
                            TimeStop.SetTimeStop(!TimeStop.isTimeStop());
                            NetworkHandler.INSTANCE.sendMessageToServer(new ServerTimeStop(TimeStop.isTimeStop()));
                            timeStopCounter = 10;
                        }
                    }
                } else {
                    if (playerIn instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) playerIn).addChatMessage(new ChatComponentText("当前版本为试用版，完整版请至#QQ2981196615购买。"));
                    }
                }
            }
        }
        return stack;
    }
}
