package kanade.kill.network.packets;

import io.netty.buffer.ByteBuf;
import kanade.kill.Launch;
import kanade.kill.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class KillAllEntities implements IMessage {
    public boolean reset;
    public KillAllEntities() {
    }

    public KillAllEntities(boolean reset) {
        this.reset = reset;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        reset = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(reset);
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
            Minecraft.getMinecraft().scheduledTasks.clear();
            List<Entity> targets = new ArrayList<>();
            targets.addAll(world.entities);
            targets.addAll(world.players);
            targets.addAll(world.EntityList);
            for (Entity e : targets) {
                EntityUtil.Kill(e, message.reset);
            }
            Minecraft.getMinecraft().IngameGUI.overlayBoss.clearBossInfos();
            return null;
        }
    }
}
