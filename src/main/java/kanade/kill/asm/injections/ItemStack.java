package kanade.kill.asm.injections;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ItemStack implements Opcodes {
    public static void AddMethod(ClassNode cn) {
        System.out.println("Adding method.");
        MethodNode mn = new MethodNode(ACC_PUBLIC, "getITEM", "()Lnet/minecraft/item/Item;", null, null);
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        mn.instructions.add(label0);
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "field_151002_e", "Lnet/minecraft/item/Item;"));
        mn.instructions.add(new InsnNode(ARETURN));
        mn.instructions.add(label1);
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/item/ItemStack;", null, label0, label1, 0));
        cn.methods.add(mn);
    }
}
