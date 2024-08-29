package kanade.kill.asm.injections;

import kanade.kill.Launch;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class NonNullList implements Opcodes {
    public static void InjectRemove(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
        list.add(new InsnNode(ARETURN));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Insert return in remove.");
    }

    public static void OverwriteClear(MethodNode mn) {
        ASMUtil.clearMethod(mn);
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/NonNullList", "clear", "(Lnet/minecraft/util/NonNullList;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        mn.maxStack = 1;
        mn.maxLocals = 1;
        Launch.LOGGER.info("Overwrite clear.");
    }

    public static void OverwriteSet(MethodNode mn) {
        ASMUtil.clearMethod(mn);
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ILOAD, 1));
        mn.instructions.add(new VarInsnNode(ALOAD, 2));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/NonNullList", "set", "(Lnet/minecraft/util/NonNullList;ILjava/lang/Object;)Ljava/lang/Object;", false));
        mn.instructions.add(new InsnNode(ARETURN));
        mn.maxStack = 3;
        mn.maxLocals = 3;
        Launch.LOGGER.info("Overwrite set.");
    }
}
