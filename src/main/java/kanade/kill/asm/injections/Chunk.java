package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Chunk implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "entities", "[Ljava/util/List;", null, null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new IntInsnNode(BIPUSH, 16));
        list.add(new TypeInsnNode(ANEWARRAY, "java/util/List"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/chunk/Chunk", "field_76645_j", "[Ljava/util/List;"));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into <init>.");
    }
}
