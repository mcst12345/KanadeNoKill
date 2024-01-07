package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.timemanagement.TimeStop;

public class ClientTimeStop implements IMessage {
    public boolean stop;

    public ClientTimeStop() {
    }

    public ClientTimeStop(boolean stop) {
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

    public static class MessageHandler implements IMessageHandler<ClientTimeStop, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientTimeStop message, MessageContext ctx) {
            TimeStop.SetTimeStop(message.stop);
            Launch.LOGGER.info("TimeStop on client:" + TimeStop.isTimeStop());
            return null;
        }
    }
}
