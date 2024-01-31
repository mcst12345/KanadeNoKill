package kanade.kill.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import kanade.kill.network.packets.*;
import net.minecraft.entity.player.EntityPlayerMP;

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
        channel.registerMessage(BlackHole.MessageHandler.class, BlackHole.class, index++, Side.SERVER);
        channel.registerMessage(ConfigUpdatePacket.MessageHandler.class, ConfigUpdatePacket.class, index, Side.SERVER);
        channel.registerMessage(ConfigUpdatePacket.MessageHandler.class, ConfigUpdatePacket.class, index++, Side.CLIENT);
        channel.registerMessage(UpdatePlayerProtectedState.MessageHandler.class, UpdatePlayerProtectedState.class, index, Side.SERVER);
        channel.registerMessage(UpdatePlayerProtectedState.MessageHandler.class, UpdatePlayerProtectedState.class, index++, Side.CLIENT);
        channel.registerMessage(Reset.MessageHandler.class, Reset.class, index++, Side.CLIENT);
        channel.registerMessage(KillAll.MessageHandler.class, KillAll.class, index, Side.SERVER);
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
        for (Object player : FMLCommonHandler.instance().getMinecraftServerInstance().serverConfigManager.playerEntityList) {
            sendMessageToPlayer(msg, (EntityPlayerMP) player);
        }
    }
}
