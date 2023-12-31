package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;

public class CoreDump implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<CoreDump, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(CoreDump message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            if (KillItem.inList(Minecraft.getMinecraft())) {
                return null;
            }
            Util.CoreDump();
            return null;
        }
    }
}
