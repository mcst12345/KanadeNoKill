package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ClassInheritanceMultiMap implements Opcodes {
    public static void InjectGetByClass(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/ClassInheritanceMultiMap", "preGetByClass", "(Lnet/minecraft/util/ClassInheritanceMultiMap;)V", false));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into GetByClass().");
    }
}
