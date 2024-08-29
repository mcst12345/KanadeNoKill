package kanade.kill.asm.hooks;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.ModMain;
import kanade.kill.item.KillItem;
import kanade.kill.timemanagement.TimeStop;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemStack {
    public static boolean isEmpty(net.minecraft.item.ItemStack stack) {
        if (stack.getITEM() == ModMain.kill_item || stack.getITEM() == ModMain.death_item) {
            return false;
        }
        if (stack == net.minecraft.item.ItemStack.EMPTY) {
            return true;
        } else if (stack.getITEM() != null && stack.getITEM() != Items.AIR) {
            if (stack.stackSize <= 0) {
                return true;
            } else {
                return stack.itemDamage < -32768 || stack.itemDamage > 65535;
            }
        } else {
            return true;
        }
    }

    public static void updateAnimation(net.minecraft.item.ItemStack stack, World worldIn, Entity entityIn, int inventorySlot, boolean isCurrentItem) {
        if (TimeStop.isTimeStop()) {
            return;
        }
        if (stack.animationsToGo > 0) {
            --stack.animationsToGo;
        }

        if (stack.ITEM != null && !Config.SuperMode && !Config.allReturn) {
            try {
                stack.ITEM.onUpdate(stack, worldIn, entityIn, inventorySlot, isCurrentItem);
            } catch (Throwable t) {
                Launch.LOGGER.warn(t);
            }
        }
    }

    public static ActionResult<net.minecraft.item.ItemStack> useItemRightClick(net.minecraft.item.ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (TimeStop.isTimeStop() && !KillItem.inList(playerIn)) {
            return new ActionResult<>(EnumActionResult.PASS, net.minecraft.item.ItemStack.EMPTY);
        }
        try {
            return stack.getItem().onItemRightClick(worldIn, playerIn, hand);
        } catch (Throwable t) {
            Launch.LOGGER.warn(t);
            return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(hand));
        }
    }
}
