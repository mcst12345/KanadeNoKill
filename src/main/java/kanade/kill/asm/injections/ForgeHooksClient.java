package kanade.kill.asm.injections;

import kanade.kill.Core;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ForgeHooksClient implements Opcodes {
    public static void InjectDrawScreen(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new JumpInsnNode(IFNONNULL, label));
        list.add(new InsnNode(RETURN));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Core.LOGGER.info("Inject into drawScreen(),");
    }
}
