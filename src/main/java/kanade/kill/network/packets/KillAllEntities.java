package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.reflection.LateFields;
import kanade.kill.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.concurrent.util.Unsafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KillAllEntities implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<KillAllEntities, IMessage> {
        @Override
        @SuppressWarnings({"unchecked", "raw"})
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KillAllEntities message, MessageContext ctx) {
            if (!Launch.client) {
                return null;
            }
            WorldClient world = Minecraft.getMinecraft().WORLD;
            List<Entity> targets = new ArrayList<>();
            targets.addAll(world.entities);
            targets.addAll(world.players);
            targets.addAll((Collection) Unsafe.instance.getObjectVolatile(world, LateFields.entityLists_offset));
            for (Entity e : targets) {
                Util.Kill(e);
            }
            return null;
        }
    }
}
