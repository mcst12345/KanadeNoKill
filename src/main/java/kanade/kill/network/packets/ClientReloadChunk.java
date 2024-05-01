package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientReloadChunk implements IMessage {
    public int x, z;

    public ClientReloadChunk() {
    }

    public ClientReloadChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(z);
    }

    public static class MessageHandler implements IMessageHandler<ClientReloadChunk, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientReloadChunk message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            WorldClient wc = Minecraft.getMinecraft().WORLD;
            ChunkProviderClient provider = (ChunkProviderClient) wc.chunkProvider;
            provider.unloadChunk(message.x, message.z);
            provider.loadChunk(message.x, message.z).markDirty();
            return null;
        }
    }
}
