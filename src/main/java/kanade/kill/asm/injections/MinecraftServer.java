package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class MinecraftServer implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "backup", "[Lnet/minecraft/world/WorldServer;", null, null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new InsnNode(ICONST_0));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/world/WorldServer"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/server/MinecraftServer", "backup", "[Lnet/minecraft/world/WorldServer;"));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into <init>.");
    }

    public static void InjectTick(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        list.add(label0);
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "tasks", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 1));
        list.add(label2);
        list.add(new FrameNode(F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label1));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "java/lang/Runnable"));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true));
        list.add(new JumpInsnNode(GOTO, label2));
        list.add(label1);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "tasks", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "clear", "()V", true));
        list.add(label3);
        mn.instructions.insert(list);
        mn.localVariables.add(new LocalVariableNode("task", "Ljava/lang/Runnable;", null, label0, label3, 2));
        Launch.LOGGER.info("Inject into tick().");
    }

    public static void InjectRun(MethodNode mn) {
        Iterator<AbstractInsnNode> iterator = mn.instructions.iterator();
        int index = -1;
        while (iterator.hasNext()) {
            AbstractInsnNode ain = iterator.next();
            if (ain instanceof FieldInsnNode) {
                if (ain.getOpcode() == GETFIELD && ((FieldInsnNode) ain).name.equals("field_71317_u") && index == -1) {
                    index = mn.instructions.indexOf(ain) + 2;
                }
            } else if (ain instanceof VarInsnNode) {
                VarInsnNode vin = (VarInsnNode) ain;
                if (((vin.getOpcode() == LSTORE || vin.getOpcode() == LLOAD) && vin.var != 1) || vin.var == 11) {
                    vin.var++;
                }
            }
        }

        LabelNode k_end = null;

        for (LocalVariableNode lvn : mn.localVariables) {
            if (lvn.name.equals("k")) {
                k_end = lvn.end;
                lvn.index++;
                break;
            }
        }
        if (k_end == null) {
            throw new IllegalStateException("The fuck?");
        }
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "CheckWorlds", "(Lnet/minecraft/server/MinecraftServer;)V", false));
        if (index == -1) {
            throw new IllegalStateException("The Fuck?");
        }

        mn.instructions.insertBefore(mn.instructions.get(index), list);
        Launch.LOGGER.info("Inject into run().");
    }
}
