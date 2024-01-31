package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.NativeMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Reset implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<Reset, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(Reset message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            NativeMethods.Reset();
            KillItem.list.clear();
            EntityUtil.Dead.clear();
            EntityUtil.blackHolePlayers.clear();
            EntityPlayerSP player = Minecraft.getMinecraft().PLAYER;
            if (player != null) {
                player.Inventory = new InventoryPlayer(player);
            }
            return null;
        }
    }
}
