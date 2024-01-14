//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ic2.core.uu;

import ic2.core.util.StackUtil;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

class LeanItemStack {
    private final Item item;
    private final int meta;
    private final NBTTagCompound nbt;
    private final int size;
    private int hashCode;

    public LeanItemStack(ItemStack stack) {
        this(stack == null ? ModMain.EMPTY : stack.getItem(), stack == null ? 0 : StackUtil.getRawMeta(stack), stack == null ? new NBTTagCompound() : stack.getTagCompound(), StackUtil.getSize(stack));
        Launch.LOGGER.info("Constructor 1");
    }

    public LeanItemStack(ItemStack stack, int size) {
        this(stack == null ? ModMain.EMPTY : stack.getItem(), stack == null ? 0 : StackUtil.getRawMeta(stack), stack == null ? new NBTTagCompound() : stack.getTagCompound(), size);
        Launch.LOGGER.info("Constructor 2");
    }

    public LeanItemStack(Item item, int meta, NBTTagCompound nbt, int size) {
        Launch.LOGGER.info("Constructor 3");
        Launch.LOGGER.info(item);
        Launch.LOGGER.info(meta);
        Launch.LOGGER.info(size);
        if (item == null) {
            Launch.LOGGER.fatal("The fuck?");

        }
        {
            this.item = item;
            this.meta = meta;
            this.nbt = nbt;
            this.size = size;
        }

    }

    public Item getItem() {
        return this.item;
    }

    public int getMeta() {
        return this.meta;
    }

    public NBTTagCompound getNbt() {
        return this.nbt;
    }

    public int getSize() {
        return this.size;
    }

    public String toString() {
        return String.format("%dx%s@%d", this.size, this.item.getRegistryName(), this.meta);
    }

    public boolean hasSameItem(LeanItemStack o) {
        return this.item == o.item && (this.meta == o.meta || !this.item.getHasSubtypes()) && StackUtil.checkNbtEquality(this.nbt, o.nbt);
    }

    public LeanItemStack copy() {
        return this.copyWithSize(this.size);
    }

    public LeanItemStack copyWithSize(int newSize) {
        LeanItemStack ret = new LeanItemStack(this.item, this.meta, this.nbt, newSize);
        ret.hashCode = this.hashCode;
        return ret;
    }

    public ItemStack toMcStack() {
        if (this.size <= 0) {
            return StackUtil.emptyStack;
        } else {
            ItemStack ret = new ItemStack(this.item, this.size, this.meta);
            if (this.nbt != null) {
                ret.deserializeNBT(this.nbt);
            }
            return ret;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LeanItemStack)) {
            return false;
        } else {
            LeanItemStack o = (LeanItemStack) obj;
            return this.item == o.item && this.meta == o.meta && (this.nbt == null && o.nbt == null || this.nbt != null && o.nbt != null && this.nbt.equals(o.nbt));
        }
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = this.calculateHashCode();
        }

        return this.hashCode;
    }

    private int calculateHashCode() {
        int ret = System.identityHashCode(this.item);
        ret = ret * 31 + this.meta;
        if (this.nbt != null) {
            ret = ret * 61 + this.nbt.hashCode();
        }

        if (ret == 0) {
            ret = -1;
        }

        return ret;
    }
}
