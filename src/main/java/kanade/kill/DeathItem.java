package kanade.kill;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeathItem extends Item {
    public DeathItem() {
        this.setRegistryName("kanade:death");
    }

    @Override
    public void onUpdate(@Nullable ItemStack stack, @Nullable World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        Util.Kill(entityIn);
    }

    @Override
    @Nonnull
    public String getTranslationKey(@Nullable ItemStack stack) {
        return "item.kanade.death";
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nullable ItemStack stack) {
        return "Death";
    }
}