package kanade.kill.asm.injections;

import kanade.kill.Core;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FMLClientHandler implements Opcodes {
    public static void AddField(ClassNode cn) {
        Core.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "instance", "Lnet/minecraftforge/fml/client/FMLClientHandler;", null, null));
    }
}
