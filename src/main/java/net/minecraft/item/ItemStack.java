package net.minecraft.item;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStack {

    public static final ItemStack EMPTY = null;

    public ItemStack(Item killItem) {
    }

    public ItemStack(NBTTagCompound nbtbase) {

    }

    public ItemStack(Item item, int size, int meta) {

    }

    public Item getITEM() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public Item getItem() {
        return null;
    }

    public NBTTagCompound getTagCompound() {
        return null;
    }

    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public boolean hasTagCompound() {
        return false;
    }

    public int getItemDamage() {
        return 0;
    }

    public int getCount() {
        return 0;
    }
}
