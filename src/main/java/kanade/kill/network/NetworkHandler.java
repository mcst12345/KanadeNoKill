package kanade.kill.network;

import kanade.kill.network.packets.*;
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
        int index = 0;
        channel.registerMessage(KillAllEntities.MessageHandler.class, KillAllEntities.class, index++, Side.CLIENT);
        channel.registerMessage(CoreDump.MessageHandler.class, CoreDump.class, index++, Side.CLIENT);
        channel.registerMessage(KillEntity.MessageHandler.class, KillEntity.class, index++, Side.CLIENT);
        channel.registerMessage(Annihilation.MessageHandler.class, Annihilation.class, index++, Side.SERVER);
        channel.registerMessage(KillCurrentPlayer.MessageHandler.class, KillCurrentPlayer.class, index++, Side.CLIENT);
        channel.registerMessage(ServerTimeStop.MessageHandler.class, ServerTimeStop.class, index++, Side.SERVER);
        channel.registerMessage(ClientTimeStop.MessageHandler.class, ClientTimeStop.class, index++, Side.CLIENT);
        channel.registerMessage(SwitchTimePoint.MessageHandler.class, SwitchTimePoint.class, index++, Side.SERVER);
        channel.registerMessage(SaveTimePoint.MessageHandler.class, SaveTimePoint.class, index++, Side.SERVER);
        channel.registerMessage(TimeBack.MessageHandler.class, TimeBack.class, index++, Side.SERVER);
        channel.registerMessage(ClientReload.MessageHandler.class, ClientReload.class, index, Side.CLIENT);
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
