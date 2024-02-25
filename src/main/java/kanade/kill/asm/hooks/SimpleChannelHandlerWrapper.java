package kanade.kill.asm.hooks;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import kanade.kill.Launch;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@SuppressWarnings("unused")
public class SimpleChannelHandlerWrapper {
    public static void channelRead0(net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper handler, ChannelHandlerContext ctx, IMessage msg) {
        INetHandler iNetHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        MessageContext context = new MessageContext(iNetHandler, handler.side);
        IMessage result = null;
        try {
            result = handler.messageHandler.onMessage(msg, context);
        } catch (Throwable t) {
            Launch.LOGGER.warn("Catch exception:", t);
        }
        if (result != null) {
            ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY);
            ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }
}
