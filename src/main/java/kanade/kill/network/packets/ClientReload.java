package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientReload implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ClientReload, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientReload message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            WorldClient old = mc.WORLD;
            IntegratedServer server = mc.integratedServer;
            WorldSummary summary = new WorldSummary(old.worldInfo, server.folderName, old.worldInfo.getWorldName(), 0L, false);
            Runnable task = () -> {
                mc.loadWorld(null);
                mc.launchIntegratedServer(summary.getFileName(), summary.getDisplayName(), null);
            };
            kanade.kill.asm.hooks.Minecraft.tasks.add(task);
            return null;
        }
    }
}
