package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.*;
import java.nio.charset.Charset;

public class ConfigUpdatePacket implements IMessage {
    public String clazz;
    public String config;
    public Object value;

    public ConfigUpdatePacket() {
    }
    public ConfigUpdatePacket(String clazz, String config, Object value) {
        this(config, value);
        this.clazz = clazz;
    }
    public ConfigUpdatePacket(String config, Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("The value should be Serializable.");
        }
        this.clazz = "kanade.kill.Config";
        this.config = config;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        config = (String) buf.readCharSequence(length, Charset.defaultCharset());
        length = buf.readInt();
        clazz = (String) buf.readCharSequence(length, Charset.defaultCharset());
        length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            this.value = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(config.length());
        buf.writeCharSequence(config, Charset.defaultCharset());
        buf.writeInt(clazz.length());
        buf.writeCharSequence(clazz, Charset.defaultCharset());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            byte[] bytes = bos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class MessageHandler implements IMessageHandler<ConfigUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(ConfigUpdatePacket message, MessageContext ctx) {
            Launch.LOGGER.info("Update " + message.clazz + " " + message.config + " " + message.value);
            try {
                Class.forName(message.clazz).getField(message.config).set(null, message.value);
            } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
