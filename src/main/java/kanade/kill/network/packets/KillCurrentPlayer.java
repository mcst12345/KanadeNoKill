package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.reflection.LateFields;
import kanade.kill.thread.DisplayGui;
import net.minecraft.client.Minecraft;
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
            if (Config.forceRender) {
                Minecraft.dead = true;
            }
            Minecraft.getMinecraft().isGamePaused = true;
            if (Minecraft.getMinecraft().PLAYER != null) {
                Unsafe.instance.putBooleanVolatile(Minecraft.getMinecraft().PLAYER, LateFields.HatedByLife_offset, true);
            }
            Minecraft.getMinecraft().skipRenderWorld = true;
            Minecraft.getMinecraft().pointedEntity = null;
            Minecraft.getMinecraft().field_152351_aB.clear();
            if (Config.forceRender) {
                DisplayGui.display();
            }
            return null;
        }
    }
}
