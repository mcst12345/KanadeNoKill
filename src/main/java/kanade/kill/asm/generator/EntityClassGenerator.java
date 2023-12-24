package kanade.kill.asm.generator;

import kanade.kill.Launch;
import kanade.kill.reflection.ReflectionUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class EntityClassGenerator {
    public static byte[] Generate(Class<?> clazz) throws IOException {
        String name = ReflectionUtil.getName(clazz);
        byte[] original_bytes = Launch.classLoader.getClassBytes(name);
        ClassNode old = new ClassNode();
        ClassNode ret = new ClassNode();
        ClassReader cr = new ClassReader(original_bytes);
        cr.accept(old, 0);

        //uncompleted.
        return new byte[0];
    }
}
