package net.minecraft.item;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStack {

    public static final ItemStack EMPTY = null;
    public int stackSize;

    public ItemStack(Item killItem) {
    }

    public ItemStack(NBTTagCompound nbtbase) {

    }

    public Item getITEM() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }
}
