package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EventBus implements Opcodes {
    public static void OverwritePost(MethodNode mn) {
        mn.tryCatchBlocks.clear();
        mn.instructions.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/EventBus", "post", "(Lcpw/mods/fml/common/eventhandler/EventBus;Lcpw/mods/fml/common/eventhandler/Event;)Z", false));
        mn.instructions.add(new InsnNode(IRETURN));
        mn.localVariables.clear();
        mn.maxLocals = 2;
        mn.maxStack = 2;
        Launch.LOGGER.info("Overwrite post(Event).");
    }
}
