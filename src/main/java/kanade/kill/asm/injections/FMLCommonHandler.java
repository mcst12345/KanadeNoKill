package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FMLCommonHandler implements Opcodes {
    public static void AddFields(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "EventBus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;", null, null));
    }
}
