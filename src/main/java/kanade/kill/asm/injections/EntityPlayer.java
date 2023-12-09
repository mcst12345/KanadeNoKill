package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityPlayer implements Opcodes {
    public static void InjectOnUpdate(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "invHaveKillItem", "(Lnet/minecraft/entity/player/EntityPlayer;)Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/item/KillItem", "AddToList", "(Ljava/lang/Object;)V", false));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "updatePlayer", "(Lnet/minecraft/entity/player/EntityPlayer;)V", false));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into onUpdate.");
    }

    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Inventory", "Lnet/minecraft/entity/player/InventoryPlayer;", null, null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "net/minecraft/entity/player/InventoryPlayer"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/entity/player/InventoryPlayer", "<init>", "(Lnet/minecraft/entity/player/EntityPlayer;)V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/player/EntityPlayer", "field_71071_by", "Lnet/minecraft/entity/player/InventoryPlayer;"));
        mn.instructions.insertBefore(mn.instructions.getLast(), list);
        Launch.LOGGER.info("Inject into <init>.");
    }
}
