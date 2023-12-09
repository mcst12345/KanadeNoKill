package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class RenderGlobal implements Opcodes {
    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC, "renderManager", "Lnet/minecraft/client/renderer/entity/RenderManager;", null, null));
        Launch.LOGGER.info("Adding field.");
    }
}
