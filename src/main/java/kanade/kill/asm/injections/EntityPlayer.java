package kanade.kill.asm.injections;

import kanade.kill.Launch;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityPlayer implements Opcodes {
    public static void InjectOnUpdate(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/EntityUtil", "invHaveKillItem", "(Lnet/minecraft/entity/player/EntityPlayer;)Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/item/KillItem", "AddToList", "(Ljava/lang/Object;)V", false));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/EntityUtil", "updatePlayer", "(Lnet/minecraft/entity/player/EntityPlayer;)V", false));
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
        AbstractInsnNode index = null;
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof MethodInsnNode) {
                if (((MethodInsnNode) ain).owner.equals("net/minecraft/entity/EntityLivingBase") && ((MethodInsnNode) ain).name.equals("<init>")) {
                    index = ain.getNext();
                    break;
                }
            }
        }
        if (index == null) {
            throw new IllegalStateException("The fuck?");
        }
        mn.instructions.insertBefore(index, list);
        Launch.LOGGER.info("Inject into <init>.");
    }

    public static void InjectApplyEntityCollision(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(ASMUtil.inList());
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/util/EntityUtil", "blackHolePlayers", "Ljava/util/Set;"));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "func_110124_au", "()Ljava/util/UUID;", false));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new InsnNode(ICONST_0));
        list.add(ASMUtil.SafeKill());
        list.add(new InsnNode(RETURN));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into applyEntityCollision.");
    }
}
