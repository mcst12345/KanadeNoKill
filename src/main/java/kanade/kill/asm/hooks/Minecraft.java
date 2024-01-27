package kanade.kill.asm.hooks;

import kanade.kill.Core;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.Annihilation;
import kanade.kill.network.packets.BlackHole;
import kanade.kill.network.packets.ServerTimeStop;
import kanade.kill.timemanagement.TimeStop;
import net.minecraft.block.material.Material;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.util.JsonException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static kanade.kill.item.KillItem.mode;
import static net.minecraft.client.Minecraft.LOGGER;

public class Minecraft {
    public static final ResourceLocation BlackAndWhite = new ResourceLocation("shaders/post/desaturate.json");
    static final boolean optifineInstalled;

    static {
        boolean tmp;
        try {
            Class.forName("optifine.OptiFineForgeTweaker");
            tmp = true;
        } catch (ClassNotFoundException e) {
            tmp = false;
        }
        optifineInstalled = tmp;
    }

    public static void rightClickMouse(net.minecraft.client.Minecraft mc) {
        mc.rightClickDelayTimer = 4;

        boolean flag = true;
        ItemStack itemstack = mc.PLAYER.Inventory.getCurrentItem();

        boolean kanade = KillItem.inList(mc.PLAYER);

        if (kanade) {
            if (mc.PLAYER.isSneaking()) {
                if (mode == 0) {
                    NetworkHandler.INSTANCE.sendMessageToServer(new Annihilation(mc.PLAYER.dimension, (int) mc.PLAYER.posX, (int) mc.PLAYER.posY, (int) mc.PLAYER.posZ));
                } else if (!Core.isDemo) {
                    if (mode == 1) {
                        NetworkHandler.INSTANCE.sendMessageToServer(new ServerTimeStop(!TimeStop.isTimeStop()));
                    }
                } else {
                    mc.PLAYER.sendChatMessage("当前版本为试用版，完整版请至#QQ2981196615购买。");
                }
            }
        }

        if (mc.objectMouseOver == null) {
            LOGGER.warn("Null returned as 'hitResult', mc shouldn't happen!");
        } else {
            switch (net.minecraft.client.Minecraft.SwitchMovingObjectType.field_152390_a[mc.objectMouseOver.typeOfHit.ordinal()]) {
                case 1:
                    if (mc.playerController.interactWithEntitySendPacket(mc.PLAYER, mc.objectMouseOver.entityHit)) {
                        flag = false;
                    }

                    break;
                case 2:
                    int i = mc.objectMouseOver.blockX;
                    int j = mc.objectMouseOver.blockY;
                    int k = mc.objectMouseOver.blockZ;

                    if (!mc.WORLD.getBlock(i, j, k).isAir(mc.WORLD, i, j, k)) {
                        int l = itemstack != null ? itemstack.stackSize : 0;

                        boolean result = !net.minecraftforge.event.ForgeEventFactory.onPlayerInteract(mc.PLAYER, net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, i, j, k, mc.objectMouseOver.sideHit, mc.WORLD).isCanceled();
                        if (result && mc.playerController.onPlayerRightClick(mc.PLAYER, mc.WORLD, itemstack, i, j, k, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                            flag = false;
                            mc.PLAYER.swingItem();
                        }

                        if (itemstack == null) {
                            return;
                        }

                        if (itemstack.stackSize == 0) {
                            mc.PLAYER.Inventory.mainInventory[mc.PLAYER.Inventory.currentItem] = null;
                        } else if (itemstack.stackSize != l || mc.playerController.isInCreativeMode()) {
                            mc.EntityRenderer.itemRenderer.resetEquippedProgress();
                        }
                    }
            }
        }
        if (flag) {
            ItemStack itemstack1 = mc.PLAYER.inventory.getCurrentItem();

            boolean result = !net.minecraftforge.event.ForgeEventFactory.onPlayerInteract(mc.PLAYER, net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, mc.WORLD).isCanceled();
            if (result && itemstack1 != null && mc.playerController.sendUseItem(mc.PLAYER, mc.WORLD, itemstack1)) {
                mc.EntityRenderer.itemRenderer.resetEquippedProgress2();
            }
        }
    }

    public static void clickMouse(net.minecraft.client.Minecraft mc) {
        if (mc.leftClickCounter <= 0) {
            boolean flag = KillItem.inList(mc.PLAYER);
            if (mc.objectMouseOver == null) {
                LOGGER.error("Null returned as 'hitResult', mc shouldn't happen!");

                if (mc.playerController.isNotCreative() && !flag) {
                    mc.leftClickCounter = 10;
                }
            } else {
                switch (mc.objectMouseOver.typeOfHit) {
                    case ENTITY:
                        mc.playerController.attackEntity(mc.PLAYER, mc.objectMouseOver.entityHit);
                        break;
                    case BLOCK:
                        int i = mc.objectMouseOver.blockX;
                        int j = mc.objectMouseOver.blockY;
                        int k = mc.objectMouseOver.blockZ;

                        if (mc.WORLD.getBlock(i, j, k).getMaterial() == Material.air) {
                            if (mc.playerController.isNotCreative()) {
                                mc.leftClickCounter = 10;
                            }
                        } else {
                            mc.playerController.clickBlock(i, j, k, mc.objectMouseOver.sideHit);
                        }

                    case MISS:

                        if (mc.playerController.isNotCreative() && !flag) {
                            mc.leftClickCounter = 10;
                        }

                        if (flag && mc.PLAYER.isSneaking()) {
                            NetworkHandler.INSTANCE.sendMessageToServer(new BlackHole(mc.PLAYER.getUniqueID()));
                        }
                }

            }
        }
    }

    public static void runGameLoop(net.minecraft.client.Minecraft mc) throws JsonException {
        if (!optifineInstalled && TimeStop.isTimeStop()) {
            ShaderGroup Group = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), BlackAndWhite);
            Group.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            mc.EntityRenderer.theShaderGroup = Group;
        } else {
            mc.EntityRenderer.theShaderGroup = null;
        }
    }
}
