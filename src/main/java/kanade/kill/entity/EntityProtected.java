package kanade.kill.entity;

import kanade.kill.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EntityProtected extends EntityCreature {
    public EntityProtected(World worldIn) {
        super(worldIn);
    }

    @Override
    public void setDead() {
    }

    @Override
    public void setOnFireFromLava() {
    }

    @Override
    public void onUpdate() {
        this.noClip = false;
        this.Death_Time = 0;
        this.hurtTime = 0;
        this.maxHurtTime = 0;
        this.isAddedToWorld = true;
        this.fire = 0;
        this.isInWeb = false;
        this.height = 2;
        this.width = 1;
        this.preventEntitySpawning = false;
        this.forceSpawn = true;
        super.onUpdate();
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
}
