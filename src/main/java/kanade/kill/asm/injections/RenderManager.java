package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RenderManager implements Opcodes {
    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC, "EntityRenderMap", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/Class<+Lnet/minecraft/entity/Entity;>;Lnet/minecraft/client/renderer/entity/Render<+Lnet/minecraft/entity/Entity;>;>;", null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "com/google/common/collect/Maps", "newHashMap", "()Ljava/util/HashMap;", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/renderer/entity/RenderManager", "field_78729_o", "Ljava/util/Map;"));
        mn.instructions.insertBefore(mn.instructions.getLast(), list);
        Launch.LOGGER.info("Inject into <init>.");
    }
}
