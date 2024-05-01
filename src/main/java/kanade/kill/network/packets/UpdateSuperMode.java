package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.util.ClassUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateSuperMode implements IMessage {
    public boolean value;

    public UpdateSuperMode() {
    }

    public UpdateSuperMode(boolean value) {
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.value);
    }

    public static class MessageHandler implements IMessageHandler<UpdateSuperMode, IMessage> {

        @Override
        public IMessage onMessage(UpdateSuperMode message, MessageContext ctx) {
            Launch.LOGGER.info("{} SuperMode.", message.value ? "Enabling" : "Disabling");
            Config.SuperMode = message.value;
            if (message.value) {
                ClassUtil.FuckModMethods();
            } else {
                ClassUtil.RestoreModMethods();
            }
            return null;
        }
    }
}
