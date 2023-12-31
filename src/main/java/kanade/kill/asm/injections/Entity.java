package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class Entity implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "HatedByLife", "Z", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "motionX", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "motionY", "D", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "motionZ", "D", null, null));
    }
}
