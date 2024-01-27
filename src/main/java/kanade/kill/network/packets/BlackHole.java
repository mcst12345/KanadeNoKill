package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.util.EntityUtil;

import java.io.*;
import java.util.UUID;

public class BlackHole implements IMessage {
    public UUID uuid;

    public BlackHole() {
    }

    public BlackHole(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            this.uuid = (UUID) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(uuid);
            byte[] bytes = bos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MessageHandler implements IMessageHandler<BlackHole, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(BlackHole message, MessageContext ctx) {
            if (EntityUtil.blackHolePlayers.contains(message.uuid)) {
                EntityUtil.blackHolePlayers.remove(message.uuid);
            } else {
                EntityUtil.blackHolePlayers.add(message.uuid);
            }
            return null;
        }
    }
}
