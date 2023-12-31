package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class DimensionManager implements Opcodes {
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
        list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/server/MinecraftServer", "field_71309_l", "Lnet/minecraft/server/MinecraftServer;"));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InsnNode(ICONST_0));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/world/WorldServer"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/ArrayList", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", false));
        list.add(new TypeInsnNode(CHECKCAST, "[Lnet/minecraft/world/WorldServer;"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/server/MinecraftServer", "backup", "[Lnet/minecraft/world/WorldServer;"));
        mn.instructions.insertBefore(index, list);
        Launch.LOGGER.info("Inject into setWorld(int,WorldServer,MinecraftServer).");
    }
}
