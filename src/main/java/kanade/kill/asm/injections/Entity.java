package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Entity implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "HatedByLife", "Z", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mX", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "X", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mY", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Y", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mZ", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Z", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "RecentlyHIT", "I", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "WORLD", "Lnet/minecraft/world/World;", null, null));
    }

    public static void OverwriteWriteToNBT(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Entity", "writeToNBT", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", false));
        mn.instructions.add(new InsnNode(ARETURN));
        Launch.LOGGER.info("Overwrite writeToNBT().");
    }

    public static void OverwriteReadFromNBT(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Entity", "readFromNBT", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NBTTagCompound;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite readFromNBT().");
    }
}
