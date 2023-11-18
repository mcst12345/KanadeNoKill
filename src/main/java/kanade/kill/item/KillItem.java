package kanade.kill.item;

import kanade.kill.ModMain;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
public class KillItem extends Item {
    private static final Set<UUID> list = new HashSet<>();
    public KillItem(){
        this.setRegistryName("kanade:kill");
        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    public static void AddToList(Object obj) {
        if (obj instanceof Entity) {
            list.add(((Entity) obj).getUniqueID());
        } else if (ModMain.client) {
            if (obj instanceof Minecraft) {
                list.add(((Minecraft) obj).PLAYER.getUniqueID());
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
            return list.contains(((Entity) obj).getUniqueID());
        } else if (ModMain.client) {
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
    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn,@Nonnull EnumHand handIn){
        Util.tasks.add(() -> {
            List<Entity> targets = new ArrayList<>();
            for (int id : DimensionManager.getIDs()) {
                WorldServer world = DimensionManager.getWorld(id);
                targets.addAll(world.loadedEntityList);
            }
            Util.Kill(targets);
        });

        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}