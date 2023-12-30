package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.reflection.LateFields;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.concurrent.util.Unsafe;

public class KillCurrentPlayer implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<KillCurrentPlayer, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KillCurrentPlayer message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            Minecraft.dead = true;
            Minecraft.getMinecraft().isGamePaused = true;
            if (Minecraft.getMinecraft().PLAYER != null) {
                Unsafe.instance.putBooleanVolatile(Minecraft.getMinecraft().PLAYER, LateFields.HatedByLife_offset, true);
            }
            Minecraft.getMinecraft().skipRenderWorld = true;
            Minecraft.getMinecraft().pointedEntity = null;
            Minecraft.getMinecraft().scheduledTasks.clear();
            return null;
        }
    }
}
