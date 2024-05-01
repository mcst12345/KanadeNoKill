package kanade.kill.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class Infector extends EntityProtected {
    public Infector(World worldIn) {
        super(worldIn);
        setSize(1, 1);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIAttackMelee(this, 0.5D, true));
        this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 128.0F));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 10, false, true, p_apply_1_ -> p_apply_1_ != null && !(p_apply_1_ instanceof EntityProtected)));
        this.tasks.addTask(2, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(3);
        this.getEntityAttribute(SWIM_SPEED).setBaseValue(5);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(5.0D);
    }

    @Override
    public boolean attackEntityAsMob(@Nonnull Entity entityIn) {
        if (entityIn instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entityIn;
            living.tasks.taskEntries.clear();
            living.tasks.addTask(0, new EntityAIAttackMelee(this, 0.5D, true));
            living.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 128.0F));
            living.tasks.addTask(2, new EntityAIMoveIndoors(this));
            living.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 10, false, true, p_apply_1_ -> p_apply_1_ != null && !(p_apply_1_ instanceof EntityProtected)));
            living.tasks.addTask(2, new EntityAIMoveTowardsRestriction(this, 0.6D));
            living.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
            living.dataManager.set(HEALTH, living.dataManager.get(HEALTH) - 5.0F);
            living.hurtTime = 10;
            return true;
        }
        return super.attackEntityAsMob(entityIn);
    }
}
