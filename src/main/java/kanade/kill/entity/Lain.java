package kanade.kill.entity;

import kanade.kill.util.EntityUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Lain extends EntityCreature {
    @Override
    public void setDead() {
    }
    public Lain(World worldIn) {
        super(worldIn);
    }

    @Override
    public void setOnFireFromLava() {
    }

    @Override
    public void setFire(int seconds) {
    }

    @Override
    public void outOfWorld() {
    }

    @Override
    public void dealFireDamage(int amount) {
    }

    @Override
    public boolean isImmuneToFire() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return true;
    }

    @Override
    public boolean isEntityAlive() {
        return true;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void setInvisible(boolean invisible) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void onKillEntity(@Nonnull EntityLivingBase entityLivingIn) {
        EntityUtil.SafeKill(entityLivingIn, true);
    }

    @Override
    public void setInWeb() {
    }

    @Override
    public boolean hitByEntity(@Nonnull Entity entityIn) {
        return true;
    }

    @Override
    public boolean isEntityInvulnerable(@Nonnull DamageSource source) {
        return true;
    }

    @Override
    public boolean getIsInvulnerable() {
        return true;
    }

    @Override
    public void setEntityInvulnerable(boolean isInvulnerable) {
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public boolean isAddedToWorld() {
        return true;
    }

    @Override
    public void onDeathUpdate() {
    }

    @Override
    public void heal(float healAmount) {
    }

    @Override
    public float getHealth() {
        return 20.0f;
    }

    @Override
    public void onRemovedFromWorld() {
    }

    @Override
    @Nullable
    public DamageSource getLastDamageSource() {
        return null;
    }

    @Override
    public void playHurtSound(@Nonnull DamageSource source) {
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause) {
    }

    @Override
    public void damageEntity(@Nonnull DamageSource damageSrc, float damageAmount) {
    }

    @Override
    public float applyPotionDamageCalculations(@Nonnull DamageSource source, float damage) {
        return 0.0f;
    }

    @Override
    public float getMaxHealth() {
        return 20.0f;
    }

    @Override
    public boolean attackEntityAsMob(@Nonnull Entity entityIn) {
        EntityUtil.SafeKill(entityIn, true);
        return true;
    }

    @Override
    public void despawnEntity() {
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public void setNoAI(boolean disable) {
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }
    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.5);
        this.getEntityAttribute(SWIM_SPEED).setBaseValue(1.5);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1024.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(30.0D);
    }
}
