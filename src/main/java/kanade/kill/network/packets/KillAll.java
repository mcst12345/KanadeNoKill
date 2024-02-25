package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Config;
import kanade.kill.network.NetworkHandler;
import kanade.kill.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KillAll implements IMessage {
    public UUID sender;
    public KillAll() {
    }

    public KillAll(UUID sender) {
        this.sender = sender;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            this.sender = (UUID) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(sender);
            byte[] bytes = bos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MessageHandler implements IMessageHandler<KillAll, IMessage> {

        @Override
        public IMessage onMessage(KillAll message, MessageContext ctx) {
            synchronized (kanade.kill.asm.hooks.MinecraftServer.futureTaskQueue) {
                kanade.kill.asm.hooks.MinecraftServer.AddTask(() -> {
                    List<Entity> targets = new ArrayList<>();
                    for (int id : DimensionManager.getIDs()) {
                        WorldServer world = DimensionManager.getWorld(id);
                        targets.addAll(world.entities);
                    }
                    EntityUtil.Kill(targets);
                });
            }
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null && server.isCallingFromMinecraftThread()) {
                for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                    if (message.sender.equals(player.getUniqueID())) {
                        continue;
                    }
                    NetworkHandler.INSTANCE.sendMessageToPlayer(new KillAllEntities(Config.fieldReset), player);
                }
            }
            return null;
        }
    }
}
