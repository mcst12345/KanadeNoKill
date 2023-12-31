package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FMLClientHandler implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "instance", "Lcpw/mods/fml/client/FMLClientHandler;", null, null));
    }
}
