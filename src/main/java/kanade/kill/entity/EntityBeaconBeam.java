package kanade.kill.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityBeaconBeam extends Entity {
    private final int last_time;

    public EntityBeaconBeam(World worldIn, int last_time) {
        super(worldIn);
        this.last_time = last_time;
    }

    public EntityBeaconBeam(World worldIn) {
        this(worldIn, 100);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public void readEntityFromNBT(@Nonnull NBTTagCompound compound) {

    }

    @Override
    public void writeEntityToNBT(@Nonnull NBTTagCompound compound) {

    }

    @Override
    public void onUpdate() {
        if (this.ticksExisted >= last_time) {
            this.HatedByLife = true;
        }
        super.onUpdate();
    }
}
