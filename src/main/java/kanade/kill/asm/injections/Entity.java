package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Entity implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "HatedByLife", "Z", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mX", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mY", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mZ", "D", null, null));
    }

    public static void OverwriteApplyEntityCollision(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Entity", "applyEntityCollision", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite applyEntityCollision().");
    }
}
