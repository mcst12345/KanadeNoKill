package net.minecraft.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.world.World;

public abstract class EntityPlayer extends EntityLivingBase {
    public InventoryPlayer inventory;
    public InventoryPlayer Inventory;
    public InventoryEnderChest theInventoryEnderChest;
    public PlayerCapabilities capabilities;

    public EntityPlayer(World worldIn) {
        super(worldIn);
    }

    public void setScore(int maxValue) {

    }
}
