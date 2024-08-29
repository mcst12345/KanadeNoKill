package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SimpleChannelHandlerWrapper implements Opcodes {
    public static void OverwriteChannelRead0(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.maxStack = 3;
        mn.maxLocals = 3;
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new VarInsnNode(ALOAD, 2));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/SimpleChannelHandlerWrapper", "channelRead0", "(Lnet/minecraftforge/fml/common/network/simpleimpl/SimpleChannelHandlerWrapper;Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite channelRead0");
    }
}
