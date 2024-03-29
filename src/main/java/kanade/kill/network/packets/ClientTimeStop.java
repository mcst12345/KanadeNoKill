package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.timemanagement.TimeStop;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
            return null;
        }
    }
}
