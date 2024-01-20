package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.network.NetworkHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TimeBack implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<TimeBack, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(TimeBack message, MessageContext ctx) {
            kanade.kill.timemanagement.TimeBack.back();
            NetworkHandler.INSTANCE.sendMessageToAllPlayer(new ClientReload());
            return null;
        }
    }
}
