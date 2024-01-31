package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class KeyBinding implements Opcodes {
    public static void AddMethod(ClassNode cn) {
        Launch.LOGGER.info("Adding method.");
        MethodNode mn = new MethodNode(ACC_PUBLIC | ACC_STATIC, "func_74506_a", "()V", null, null);
        mn.instructions.add(new InsnNode(RETURN));
        cn.methods.add(mn);
    }
}
