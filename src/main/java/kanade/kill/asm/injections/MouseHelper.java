package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MouseHelper implements Opcodes {
    public static void OverwriteGrabMouseCursor(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MouseHelper", "grabMouseCursor", "(Lnet/minecraft/util/MouseHelper;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite grabMouseCursor.");
    }

    public static void OverwriteUngrabMouseCursor(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MouseHelper", "ungrabMouseCursor", "()V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite ungrabMouseCursor.");
    }

    public static void OverwriteMouseXYChange(MethodNode mn) {
        mn.instructions.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MouseHelper", "mouseXYChange", "(Lnet/minecraft/util/MouseHelper;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite MouseXYChange.");
    }
}