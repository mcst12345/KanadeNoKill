package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EventBus implements Opcodes {
    public static void InjectPost(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "disableEvent", "Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "shouldPostEvent", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
        list.add(new JumpInsnNode(IFNE, label));
        list.add(new InsnNode(ICONST_0));
        list.add(new InsnNode(IRETURN));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into post().");
    }
}
