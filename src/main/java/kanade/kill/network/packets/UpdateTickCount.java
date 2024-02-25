package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UpdateTickCount implements IMessage {
    private int i;

    public UpdateTickCount() {
    }

    public UpdateTickCount(int i) {
        this.i = i;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        i = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(i);
    }

    public static class MessageHandler implements IMessageHandler<UpdateTickCount, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(UpdateTickCount message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            Minecraft.getMinecraft().timer.elapsedTicks = message.i;
            kanade.kill.asm.hooks.Minecraft.tickCount = message.i;
            return null;
        }
    }
}
