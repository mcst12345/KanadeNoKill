package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EntityTracker implements Opcodes {
    public static void OverwriteTick(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/EntityTracker", "tck", "(Lnet/minecraft/entity/EntityTracker;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite tick().");
    }
}
