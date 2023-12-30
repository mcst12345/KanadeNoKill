package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
