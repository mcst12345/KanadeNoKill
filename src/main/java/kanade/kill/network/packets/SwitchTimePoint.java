package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.timemanagement.TimeBack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.*;
import java.util.UUID;

public class SwitchTimePoint implements IMessage {
    public UUID uuid;

    public SwitchTimePoint() {
    }

    public SwitchTimePoint(UUID uuid) {
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

    public static class MessageHandler implements IMessageHandler<SwitchTimePoint, IMessage> {

        @Override
        public IMessage onMessage(SwitchTimePoint message, MessageContext ctx) {
            File file = TimeBack.SwitchTimePoint();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            PlayerList list = server.getPlayerList();
            EntityPlayerMP player = list.getPlayerByUUID(message.uuid);


            if (file == null) {
                player.sendMessage(new TextComponentString("No time point saved."));
            } else {
                player.sendMessage(new TextComponentString("Switch to time point:" + file.getName()));
            }

            return null;
        }
    }
}
