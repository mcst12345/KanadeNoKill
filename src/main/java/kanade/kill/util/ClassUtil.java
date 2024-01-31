package kanade.kill.util;

import kanade.kill.Empty;
import kanade.kill.Launch;
import kanade.kill.reflection.EarlyFields;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import scala.concurrent.util.Unsafe;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class ClassUtil implements Opcodes {
    public static void setClassLoader(Class<?> clazz, ClassLoader loader) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.classLoaderOffset, loader);
    }

    public static void setName(Class<?> clazz, String name) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.name_offset, name);
    }

    public static void setRedefinedCount(Class<?> clazz, int count) {
        Unsafe.instance.putIntVolatile(clazz, EarlyFields.classRedefinedCount_offset, count);
    }

    public static SoftReference getReflectionData(Class<?> clazz) {
        return (SoftReference) Unsafe.instance.getObjectVolatile(clazz, EarlyFields.reflectionData_offset);
    }

    public static void setReflectionData(Class<?> clazz, SoftReference sr) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.reflectionData_offset, sr);
    }

    public static void setClass(Object obj, Class<?> clazz) {
        try {
            int tmp = Unsafe.instance.getIntVolatile(Unsafe.instance.allocateInstance(clazz), 8L);
            Unsafe.instance.putIntVolatile(obj, 8L, tmp);
        } catch (InstantiationException e) {
            Launch.LOGGER.error("Failed to set class of object!", e);
        }
    }

    public static Class<?> generateClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = Launch.classLoader.getClassBytes(name);
            ClassReader cr = new ClassReader(bytes);
            ClassNode old = new ClassNode(), cn = new ClassNode();
            cr.accept(old, 0);
            cn.access = ACC_PUBLIC;
            if (Modifier.isStatic(old.access)) {
                cn.access |= ACC_STATIC;
            }

            for (MethodNode mn : old.methods) {
                MethodNode neo = new MethodNode(Modifier.isStatic(mn.access) ? ACC_PUBLIC | ACC_STATIC : ACC_PUBLIC, mn.name, mn.desc, mn.signature, null);
                neo.maxStack = 1;
                neo.maxLocals = Type.getArgumentsAndReturnSizes(mn.desc);
                if (mn.desc.endsWith("V")) {
                    neo.instructions.add(new InsnNode(RETURN));
                    continue;
                }
                String ret = mn.desc;
                ret = ret.replace('/', '.');
                if (ret.endsWith(";")) {
                    ret = ret.substring(ret.lastIndexOf(")L") + 2, ret.length() - 1);
                } else {
                    ret = ret.substring(ret.length() - 1);
                }
                switch (ret) {
                    case "B":
                    case "S":
                    case "Z":
                    case "C":
                    case "I": {
                        mn.instructions.add(new InsnNode(ICONST_0));
                        mn.instructions.add(new InsnNode(IRETURN));
                        break;
                    }
                    case "J": {
                        mn.maxStack++;
                        mn.instructions.add(new InsnNode(LCONST_0));
                        mn.instructions.add(new InsnNode(LRETURN));
                        break;
                    }
                    case "F": {
                        mn.instructions.add(new InsnNode(FCONST_0));
                        mn.instructions.add(new InsnNode(FRETURN));
                        break;
                    }
                    case "D": {
                        mn.maxStack++;
                        mn.instructions.add(new InsnNode(DCONST_0));
                        mn.instructions.add(new InsnNode(DRETURN));
                        break;
                    }
                    default: {
                        mn.instructions.add(new LdcInsnNode(ret));
                        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/ObjectUtil", "generateObject", "(Ljava/lang/String;)Ljava/lang/Object;", false));
                        mn.instructions.add(new InsnNode(ARETURN));
                        break;
                    }
                }
            }

            cn.fields.addAll(old.fields);


            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            bytes = cw.toByteArray();
            return Unsafe.instance.defineAnonymousClass(Empty.class, bytes, null);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
