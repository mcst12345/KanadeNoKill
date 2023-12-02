package kanade.kill.asm.injections;

import kanade.kill.Core;
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
        Core.LOGGER.info("Insert return in remove.");
    }

    public static void OverwriteClear(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        LabelNode label5 = new LabelNode();
        LabelNode label6 = new LabelNode();
        LabelNode label7 = new LabelNode();
        LabelNode label8 = new LabelNode();

        list.add(label0);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
        list.add(new JumpInsnNode(IFNONNULL, label1));

        list.add(label2);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "net/minecraft/util/NonNullList", "lambda$clear$0", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/AbstractList", "removeIf", "(Ljava/util/function/Predicate;)Z", false));
        list.add(new InsnNode(POP));
        list.add(new JumpInsnNode(GOTO, label3));
        list.add(label1);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new InsnNode(ICONST_0));
        list.add(new VarInsnNode(ISTORE, 1));
        list.add(label4);
        list.add(new FrameNode(F_APPEND, 1, new Object[]{INTEGER}, 0, null));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "size", "()I", false));
        list.add(new JumpInsnNode(IF_ICMPGE, label3));
        list.add(label5);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
        list.add(new JumpInsnNode(IFNE, label6));
        list.add(label7);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", false));
        list.add(new InsnNode(POP));
        list.add(label6);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new IincInsnNode(1, 1));
        list.add(new JumpInsnNode(GOTO, label4));
        list.add(label3);
        list.add(new FrameNode(F_CHOP, 1, null, 0, null));
        list.add(new InsnNode(RETURN));
        list.add(label8);
        mn.instructions = list;

        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("i", "I", null, label4, label3, 1));
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", null, label0, label8, 0));
        Core.LOGGER.info("Overwrite clear.");
    }

    public static void OverwriteSet(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
        list.add(new JumpInsnNode(IFEQ, label0));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
        list.add(new InsnNode(ARETURN));
        list.add(label0);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191198_a", "Ljava/util/List;"));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true));
        list.add(new InsnNode(ARETURN));
        list.add(label1);

        mn.instructions = list;
        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", "Lnet/minecraft/util/NonNullList<TE;>;", label0, label1, 0));
        mn.localVariables.add(new LocalVariableNode("p_set_1_", "I", null, label0, label1, 1));
        mn.localVariables.add(new LocalVariableNode("p_set_2_", "Ljava/lang/Object;", "TE;", label0, label1, 2));

        Core.LOGGER.info("Overwrite set.");
    }

    public static void AddMethod(ClassNode cn) {
        MethodNode mn = new MethodNode(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$clear$0", "(Ljava/lang/Object;)Z", null, null);
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        mn.instructions.add(label0);
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
        mn.instructions.add(new JumpInsnNode(IFNE, label1));
        mn.instructions.add(new InsnNode(ICONST_1));
        mn.instructions.add(new JumpInsnNode(GOTO, label2));
        mn.instructions.add(label1);
        mn.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.add(new InsnNode(ICONST_0));
        mn.instructions.add(label2);
        mn.instructions.add(new FrameNode(F_SAME1, 0, null, 1, new Object[]{INTEGER}));
        mn.instructions.add(new InsnNode(IRETURN));
        mn.instructions.add(label3);
        mn.localVariables.add(new LocalVariableNode("o", "Ljava/lang/Object;", null, label0, label3, 0));
        cn.methods.add(mn);
        Core.LOGGER.info("Adding method.");
    }
}
