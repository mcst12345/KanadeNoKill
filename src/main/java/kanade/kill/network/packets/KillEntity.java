package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KillEntity implements IMessage {
    public int id;

    public KillEntity() {
    }

    public KillEntity(int id) {
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public static class MessageHandler implements IMessageHandler<KillEntity, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KillEntity message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            WorldClient world = Minecraft.getMinecraft().WORLD;
            if (world != null) {
                Entity entity = world.getEntityByID(message.id);
                if (entity != null) {
                    Util.Kill(entity);
                }
            }
            return null;
        }
    }
}
