package kanade.kill.asm.injections;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class EntityLivingBase implements Opcodes {
    public static void AddField(ClassNode cn) {
        System.out.println("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Death_Time", "I", null, null));

    }
}
