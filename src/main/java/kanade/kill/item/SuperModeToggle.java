package kanade.kill.item;

import kanade.kill.Config;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.UpdateSuperMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SuperModeToggle extends Item {
    public SuperModeToggle() {
        setRegistryName("kanade:super_mode");
        setMaxStackSize(1);
    }


    @Override
    @Nonnull
    public String getTranslationKey(@Nullable ItemStack stack) {
        return "item.kanade.super_mode";
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nullable ItemStack stack) {
        return "SuperMode:" + Config.SuperMode;
    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        boolean neo = !Config.SuperMode;
        Config.SuperAttack = neo;
        NetworkHandler.INSTANCE.sendMessageToAll(new UpdateSuperMode(neo));
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
