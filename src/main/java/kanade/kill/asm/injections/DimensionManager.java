package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class DimensionManager implements Opcodes {
    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, "Dimensions", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap<Lnet/minecraftforge/common/DimensionManager$Dimension;>;", null));
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, "UnloadQueue", "Lit/unimi/dsi/fastutil/ints/IntSet;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, "Worlds", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap<Lnet/minecraft/world/WorldServer;>;", null));
        Launch.LOGGER.info("Adding field.");
    }
    public static void InjectSetWorld(MethodNode mn) {
        AbstractInsnNode index = null;
        Iterator<AbstractInsnNode> iterator = mn.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode ain = iterator.next();
            if (ain instanceof InsnNode && ain.getOpcode() == RETURN) {
                index = ain;
                break;
            }
        }
        if (index == null) {
            throw new IllegalStateException("The fuck?");
        }
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new InsnNode(ICONST_0));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/world/WorldServer"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/ArrayList", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", false));
        list.add(new TypeInsnNode(CHECKCAST, "[Lnet/minecraft/world/WorldServer;"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/server/MinecraftServer", "Worlds", "[Lnet/minecraft/world/WorldServer;"));
        mn.instructions.insertBefore(index, list);
        list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/DimensionManager", "checkWorld", "(Lnet/minecraft/world/World;)Z", false));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new InsnNode(RETURN));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into setWorld(int,WorldServer,MinecraftServer).");
    }
}
