package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Event implements Opcodes {
    public static void OverwriteSetCanceled(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ILOAD, 1));
        mn.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraftforge/fml/common/eventhandler/Event", "isCanceled", "Z"));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite setCanceled(Z)V.");
    }
}
