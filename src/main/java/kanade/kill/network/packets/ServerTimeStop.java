package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.network.NetworkHandler;
import kanade.kill.timemanagement.TimeStop;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerTimeStop implements IMessage {
    public boolean stop;

    public ServerTimeStop() {
    }

    public ServerTimeStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stop = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(stop);
    }

    public static class MessageHandler implements IMessageHandler<ServerTimeStop, IMessage> {

        @Override
        public IMessage onMessage(ServerTimeStop message, MessageContext ctx) {
            TimeStop.SetTimeStop(message.stop);
            NetworkHandler.INSTANCE.sendMessageToAllPlayer(new ClientTimeStop(message.stop));
            return null;
        }
    }
}
