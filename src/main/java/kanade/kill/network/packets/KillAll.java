package kanade.kill.network.packets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.network.NetworkHandler;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class KillAll implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<KillAll, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(KillAll message, MessageContext ctx) {
            synchronized (Util.tasks) {
                Util.tasks.add(() -> {
                    List<Entity> targets = new ArrayList<>();
                    for (int id : DimensionManager.getIDs()) {
                        WorldServer world = DimensionManager.getWorld(id);
                        targets.addAll(world.entities);
                    }
                    EntityUtil.Kill(targets);
                });
            }
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillAllEntities());
            }
            return null;
        }
    }
}
