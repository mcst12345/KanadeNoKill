package kanade.kill.network;

import kanade.kill.network.packets.CoreDump;
import kanade.kill.network.packets.KillAllEntities;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("unused")
public enum NetworkHandler {
    INSTANCE;

    private final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("kanade");

    NetworkHandler() {
        channel.registerMessage(KillAllEntities.MessageHandler.class, KillAllEntities.class, 0, Side.CLIENT);
        channel.registerMessage(CoreDump.MessageHandler.class, CoreDump.class, 0, Side.CLIENT);
        channel.registerMessage(KillAllEntities.MessageHandler.class, KillAllEntities.class, 0, Side.CLIENT);
    }

    public void sendMessageToPlayer(IMessage msg, EntityPlayerMP player) {
        channel.sendTo(msg, player);
    }

    public void sendMessageToAll(IMessage msg) {
        channel.sendToAll(msg);
    }

    public void sendMessageToServer(IMessage msg) {
        channel.sendToServer(msg);
    }

    public void sendMessageToAllPlayer(IMessage msg) {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            sendMessageToPlayer(msg, player);
        }
    }
}
