package kanade.kill.network.packets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kanade.kill.util.Util;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;


public class Annihilation implements IMessage {
    public int x, y, z, world;

    public Annihilation() {
    }

    public Annihilation(int world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        world = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(world);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class MessageHandler implements IMessageHandler<Annihilation, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(Annihilation message, MessageContext ctx) {
            Util.killing = true;
            WorldServer world = DimensionManager.getWorld(message.world);
            for (int i = message.x - 5; i <= message.x + 5; i++) {
                for (int j = message.y - 5; j <= message.y + 5; j++) {
                    for (int k = message.z - 5; k <= message.z + 5; k++) {
                        world.setBlock(i, j, k, Blocks.air, 0, 2);
                    }
                }
            }
            Util.killing = false;
            return null;
        }
    }
}
