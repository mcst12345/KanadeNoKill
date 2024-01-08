package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class NetHandlerPlayServer implements Opcodes {
    public static void OverwriteIsMoveVehiclePacketInvalid(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new InsnNode(ICONST_0));
        mn.instructions.add(new InsnNode(IRETURN));
        Launch.LOGGER.info("Overwrite isMoveVehiclePacketInvalid(Lnet/minecraft/network/play/client/CPacketVehicleMove;)V.");
    }

    public static void OverwriteIsMovePlayerPacketInvalid(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new InsnNode(ICONST_0));
        mn.instructions.add(new InsnNode(IRETURN));
        Launch.LOGGER.info("Overwrite isMovePlayerPacketInvalid(Lnet/minecraft/network/play/client/CPacketPlayer;)V");
    }
}
