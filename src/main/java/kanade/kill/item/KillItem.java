package kanade.kill.item;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.Annihilation;
import kanade.kill.network.packets.KillAllEntities;
import kanade.kill.network.packets.ServerTimeStop;
import kanade.kill.timemanagement.TimeStop;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
public class KillItem extends Item {
    public static int mode;
    private short timeStopCounter = 0;
    private static final Set<UUID> list = new HashSet<>();
    public KillItem(){
        this.setRegistryName("kanade:kill");
        this.setCreativeTab(CreativeTabs.COMBAT);
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

    @Override
    public boolean isDamageable(){
        return false;
    }

    @Override
    @Nonnull
    public Item setFull3D(){
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D(){
        return false;
    }

    @Override
    @Nonnull
    public String getTranslationKey(@Nullable ItemStack stack){
        return "item.kanade.kill";
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nullable ItemStack stack){
        return "Kill";
    }

    @Override
    public boolean isEnchantable(@Nullable ItemStack stack){
        return false;
    }

    @Override
    public boolean onLeftClickEntity(@Nullable ItemStack stack,@Nonnull EntityPlayer player,@Nonnull Entity entity){
        Util.Kill(entity);
        return true;
    }

    public static boolean inList(Object obj) {
        if (obj instanceof Entity) {
            if (Config.allPlayerProtect) {
                if (obj instanceof EntityPlayer) {
                    return true;
                }
            }
            UUID uuid = ((Entity) obj).getUniqueID();
            return list.contains(uuid) || (uuid != null && NativeMethods.ProtectContain(uuid.hashCode())) || (obj instanceof EntityItem && Util.NoRemove(((EntityItem) obj).getItem()));
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
    public void onUpdate(@Nullable ItemStack stack,@Nullable World worldIn,@Nonnull Entity entityIn, int itemSlot, boolean isSelected)
    {
        if(entityIn instanceof EntityPlayer){
            entityIn.world.protects.add(entityIn);
            list.add(entityIn.getUniqueID());
        }
        if (timeStopCounter > 0) {
            timeStopCounter--;
        }
    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn,@Nonnull EnumHand handIn){
        if (!playerIn.isSneaking()) {
            synchronized (Util.tasks) {
                Util.tasks.add(() -> {
                    List<Entity> targets = new ArrayList<>();
                    for (int id : DimensionManager.getIDs()) {
                        WorldServer world = DimensionManager.getWorld(id);
                        targets.addAll(world.entities);
                    }
                    Util.Kill(targets);
                });
            }
            if (FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillAllEntities());
            }
        } else {
            if (mode == 0) {
                Config.Annihilation = !Config.Annihilation;
                if (playerIn.world.isRemote) {
                    NetworkHandler.INSTANCE.sendMessageToServer(new Annihilation(playerIn.dimension, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ));
                }
            } else if (mode == 1) {
                if (playerIn.world.isRemote) {
                    if (timeStopCounter == 0) {
                        TimeStop.SetTimeStop(!TimeStop.isTimeStop());
                        NetworkHandler.INSTANCE.sendMessageToServer(new ServerTimeStop(TimeStop.isTimeStop()));
                        timeStopCounter = 10;
                    }
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
