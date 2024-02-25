package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ItemStack implements Opcodes {
    public static void AddFields(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "ITEM", "Lnet/minecraft/item/Item;", null, null));
    }

    public static void AddMethods(ClassNode cn) {
        Launch.LOGGER.info("Adding method.");
        MethodNode mn = new MethodNode(ACC_PUBLIC, "getITEM", "()Lnet/minecraft/item/Item;", null, null);
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        mn.instructions.add(label0);
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "ITEM", "Lnet/minecraft/item/Item;"));
        mn.instructions.add(new InsnNode(ARETURN));
        mn.instructions.add(label1);
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/item/ItemStack;", null, label0, label1, 0));
        cn.methods.add(mn);
    }

    public static void OverwriteGetTooltip(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.maxLocals = 3;
        mn.maxStack = 3;
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new VarInsnNode(ALOAD, 2));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/ItemStackClient", "getTooltip", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/client/util/ITooltipFlag;)Ljava/util/List;", false));
        mn.instructions.add(new InsnNode(ARETURN));
        Launch.LOGGER.info("Overwrite getTooltip.");
    }

    public static void OverwriteUpdateAnimation(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.maxStack = 5;
        mn.maxLocals = 5;
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new VarInsnNode(ALOAD, 2));
        mn.instructions.add(new VarInsnNode(ILOAD, 3));
        mn.instructions.add(new VarInsnNode(ILOAD, 4));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/ItemStack", "updateAnimation", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite updateAnimation.");
    }

    public static void OverwriteUseItemRightClick(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.maxStack = 4;
        mn.maxLocals = 4;
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new VarInsnNode(ALOAD, 2));
        mn.instructions.add(new VarInsnNode(ALOAD, 3));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/ItemStack", "useItemRightClick", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;", false));
        mn.instructions.add(new InsnNode(ARETURN));
    }
}
