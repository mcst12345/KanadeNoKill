package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.reflection.LateFields;
import kanade.kill.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
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
                EntityUtil.Kill(e, true);
            }
            return null;
        }
    }
}
