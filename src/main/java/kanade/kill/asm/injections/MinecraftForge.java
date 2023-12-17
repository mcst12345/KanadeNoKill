package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class MinecraftForge implements Opcodes {

    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;", null, null));
        Launch.LOGGER.info("Adding field.");
    }
}
