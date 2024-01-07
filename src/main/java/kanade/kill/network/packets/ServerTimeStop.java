package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.network.NetworkHandler;
import kanade.kill.timemanagement.TimeStop;

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
        @SideOnly(Side.SERVER)
        public IMessage onMessage(ServerTimeStop message, MessageContext ctx) {
            TimeStop.SetTimeStop(message.stop);
            Launch.LOGGER.info("TimeStop on server side:" + TimeStop.isTimeStop());
            NetworkHandler.INSTANCE.sendMessageToAllPlayer(new ClientTimeStop(message.stop));
            return null;
        }
    }
}
