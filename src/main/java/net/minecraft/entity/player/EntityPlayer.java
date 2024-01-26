package net.minecraft.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;

public class EntityPlayer extends EntityLivingBase {
    public void resetCooldown() {
    }
    public InventoryPlayer Inventory;
    public InventoryEnderChest enderChest;
    public PlayerCapabilities capabilities;

    public EntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return null;
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return null;
    }

    public int getScore() {
        return 114514;
    }

    public void setScore(int maxValue) {

    }
}
