package kanade.kill.asm.injections;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MinecraftForge implements Opcodes {
    public static void InjectClassConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new TypeInsnNode(NEW, "net/minecraftforge/fml/common/eventhandler/EventBus"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "<init>", "()V", false));
        list.add(new FieldInsnNode(PUTSTATIC, "net/minecraftforge/common/MinecraftForge", "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
        mn.instructions.insert(list);
        System.out.println("Inject into <clinit>.");
    }

    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;", null, null));
        System.out.println("Adding field.");
    }
}
