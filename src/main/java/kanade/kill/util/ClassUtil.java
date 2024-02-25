package kanade.kill.util;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import kanade.kill.Empty;
import kanade.kill.Launch;
import kanade.kill.classload.FakeClassLoadr;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.ReflectionUtil;
import me.xdark.shell.ShellcodeRunner;
import net.minecraft.launchwrapper.IClassTransformer;
import one.helfy.JVM;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static me.xdark.shell.ShellcodeRunner.getSymbol;

@SuppressWarnings("unused")
public class ClassUtil implements Opcodes {
    private static final Map<Long, Byte> first_bytecode = new Long2ByteOpenHashMap();
    private static final Map<Long, Byte> second_bytecode = new Long2ByteOpenHashMap();
    private static final Map<Class<?>, Class<?>> alternatives = new HashMap<>();

    public static void FuckModMethods() {
        Util.killing = true;
        Launch.LOGGER.info("Fucking mod methods.");
        sun.misc.Unsafe unsafe = Unsafe.instance;
        JVM jvm = ShellcodeRunner.jvm;
        long constMethodOffset = jvm.type("Method").offset("_constMethod");
        one.helfy.Type constMethodType = jvm.type("ConstMethod");
        one.helfy.Type constantPoolType = jvm.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = jvm.type("Method").offset("_from_compiled_entry");
        one.helfy.Type t = jvm.type("ConstMethod");
        long bytecode_offset = t.size;

        try {
            for (Class clazz : Launch.INSTRUMENTATION.getAllLoadedClasses()) {
                String clz_name = ReflectionUtil.getName(clazz);
                if (!clz_name.startsWith("kanade.kill.") && ObjectUtil.ModClass(clz_name)) {

                    Launch.LOGGER.info("Class:" + clz_name);

                    int oopSize = jvm.intConstant("oopSize");
                    long klassOffset = jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"));
                    long klass = oopSize == 8
                            ? unsafe.getLong(clazz, klassOffset)
                            : unsafe.getInt(clazz, klassOffset) & 0xffffffffL;

                    long methodArray = jvm.getAddress(klass + jvm.type("InstanceKlass").offset("_methods"));
                    int methodCount = jvm.getInt(methodArray);
                    long methods = methodArray + jvm.type("Array<Method*>").offset("_data");

                    for (int i = 0; i < methodCount; i++) {

                        long method = jvm.getAddress(methods + (long) i * oopSize);
                        long constMethod = jvm.getAddress(method + constMethodOffset);

                        long constantPool = jvm.getAddress(constMethod + constantPoolOffset);
                        int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
                        int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

                        String name = getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize);
                        String desc = getSymbol(
                                constantPool + constantPoolType.size + (long) signatureIndex * oopSize);

                        if (desc.endsWith(";") || name.startsWith("<")) {
                            continue;
                        }

                        Launch.LOGGER.info("Fucking method:" + name + desc);

                        if (!first_bytecode.containsKey(constMethod)) {
                            first_bytecode.put(constMethod, Unsafe.instance.getByte(constMethod + bytecode_offset));
                        }
                        if (desc.endsWith("V")) {
                            Unsafe.instance.putByte(constMethod + bytecode_offset, (byte) RETURN);
                        } else if (desc.endsWith("I") || desc.endsWith("Z") || desc.endsWith("B") || desc.endsWith("C") || desc.endsWith("S")) {
                            second_bytecode.put(constMethod, Unsafe.instance.getByte(constMethod + bytecode_offset + 2));
                            Unsafe.instance.putByte(constMethod + bytecode_offset, (byte) ICONST_0);
                            Unsafe.instance.putByte(constMethod + bytecode_offset + 2, (byte) IRETURN);
                        } else if (desc.endsWith("J")) {
                            second_bytecode.put(constMethod, Unsafe.instance.getByte(constMethod + bytecode_offset + 2));
                            Unsafe.instance.putByte(constMethod + bytecode_offset, (byte) LCONST_0);
                            Unsafe.instance.putByte(constMethod + bytecode_offset + 2, (byte) LRETURN);
                        } else if (desc.endsWith("D")) {
                            second_bytecode.put(constMethod, Unsafe.instance.getByte(constMethod + bytecode_offset + 2));
                            Unsafe.instance.putByte(constMethod + bytecode_offset, (byte) DCONST_0);
                            Unsafe.instance.putByte(constMethod + bytecode_offset + 2, (byte) DRETURN);
                        } else if (desc.endsWith("F")) {
                            second_bytecode.put(constMethod, Unsafe.instance.getByte(constMethod + bytecode_offset + 2));
                            Unsafe.instance.putByte(constMethod + bytecode_offset, (byte) FCONST_0);
                            Unsafe.instance.putByte(constMethod + bytecode_offset + 2, (byte) FRETURN);
                        }
                    }
                }
            }
            Launch.LOGGER.info("Completed.");
            Util.killing = false;
        } finally {
            Util.killing = false;
        }
    }
    public static void setClassLoader(Class<?> clazz, ClassLoader loader) {
        Unsafe.instance.putObjectVolatile(clazz, EarlyFields.classLoader_offset, loader);
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

    public static void RestoreModMethods() {
        Util.killing = true;
        Launch.LOGGER.info("Restoring mod methods.");
        sun.misc.Unsafe unsafe = Unsafe.instance;
        JVM jvm = ShellcodeRunner.jvm;
        long constMethodOffset = jvm.type("Method").offset("_constMethod");
        one.helfy.Type constMethodType = jvm.type("ConstMethod");
        one.helfy.Type constantPoolType = jvm.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = jvm.type("Method").offset("_from_compiled_entry");
        one.helfy.Type t = jvm.type("ConstMethod");
        long bytecode_offset = t.size;

        try {
            for (Class clazz : Launch.INSTRUMENTATION.getAllLoadedClasses()) {
                if (ObjectUtil.ModClass(ReflectionUtil.getName(clazz))) {

                    Launch.LOGGER.info("Class:" + ReflectionUtil.getName(clazz));

                    int oopSize = jvm.intConstant("oopSize");
                    long klassOffset = jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"));
                    long klass = oopSize == 8
                            ? unsafe.getLong(clazz, klassOffset)
                            : unsafe.getInt(clazz, klassOffset) & 0xffffffffL;

                    long methodArray = jvm.getAddress(klass + jvm.type("InstanceKlass").offset("_methods"));
                    int methodCount = jvm.getInt(methodArray);
                    long methods = methodArray + jvm.type("Array<Method*>").offset("_data");

                    for (int i = 0; i < methodCount; i++) {

                        long method = jvm.getAddress(methods + (long) i * oopSize);
                        long constMethod = jvm.getAddress(method + constMethodOffset);

                        long constantPool = jvm.getAddress(constMethod + constantPoolOffset);
                        int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
                        int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

                        String name = getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize);
                        String desc = getSymbol(
                                constantPool + constantPoolType.size + (long) signatureIndex * oopSize);

                        if (desc.endsWith(";") || name.startsWith("<")) {
                            continue;
                        }


                        if (first_bytecode.containsKey(constMethod)) {
                            Launch.LOGGER.info("Restoring method:" + name + desc);
                            Unsafe.instance.putByte(constMethod + bytecode_offset, first_bytecode.get(constMethod));
                            if (second_bytecode.containsKey(constMethod)) {
                                Unsafe.instance.putByte(constMethod + bytecode_offset + 2, second_bytecode.get(constMethod));
                            }
                        }

                    }
                }
            }
            Util.killing = false;
            Launch.LOGGER.info("Completed.");
        } finally {
            Util.killing = false;
        }

    }

    public static byte[] generateClassBytes(String name) throws ClassNotFoundException {
        try {
            Launch.LOGGER.info("Generating bytes of class:" + name);
            byte[] bytes = Launch.classLoader.getClassBytes(name);
            String mapped = ((KanadeClassLoader) Launch.classLoader).DeobfuscatingTransformer.remapClassName(name);
            for (IClassTransformer transformer : KanadeClassLoader.NecessaryTransformers) {
                bytes = transformer.transform(name, mapped, bytes);
            }
            ClassReader cr = new ClassReader(bytes);
            ClassNode old = new ClassNode(), cn = new ClassNode();
            cr.accept(old, 0);
            cn.access = old.access;
            cn.name = old.name;
            cn.superName = old.superName;
            cn.version = old.version;
            cn.visibleAnnotations = old.visibleAnnotations;
            cn.invisibleAnnotations = old.invisibleAnnotations;
            cn.attrs = old.attrs;
            cn.interfaces = old.interfaces;
            cn.invisibleTypeAnnotations = old.invisibleTypeAnnotations;
            cn.visibleTypeAnnotations = old.visibleTypeAnnotations;
            cn.innerClasses = old.innerClasses;
            cn.outerClass = old.outerClass;
            cn.outerMethod = old.outerMethod;
            cn.signature = old.signature;
            cn.outerMethodDesc = old.outerMethodDesc;

            for (MethodNode mn : old.methods) {
                MethodNode neo = new MethodNode(mn.access, mn.name, mn.desc, mn.signature, null);
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
                cn.methods.add(neo);
            }

            cn.fields = old.fields;

            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    public static Class<?> generateTempClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = generateClassBytes(name);
            return Unsafe.instance.defineAnonymousClass(Empty.class, bytes, null);
        } catch (Throwable e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    public static Class<?> generateClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = generateClassBytes(name);
            Class<?> generated = Unsafe.instance.defineClass(name, bytes, 0, bytes.length, FakeClassLoadr.INSTANCE, Empty.class.getProtectionDomain());
            FakeClassLoadr.putCache(name, generated);
            return generated;
        } catch (Throwable e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    @Nullable
    public static Class<?> shadowClass(Class<?> original) {
        String name = ReflectionUtil.getName(original);
        try {
            byte[] bytes = Launch.classLoader.getClassBytes(name);
            return Unsafe.instance.defineAnonymousClass(Empty.class, bytes, null);
        } catch (IOException e) {
            return null;
        } catch (Throwable t) {
            Launch.LOGGER.error(t);
            return null;
        }
    }

    public static void redefineClass(Class<?> toBeRedefined, byte[] neo) {
        try {
            ClassDefinition cd = new ClassDefinition(toBeRedefined, neo);
            Launch.INSTRUMENTATION.redefineClasses(cd);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            Launch.LOGGER.warn("Failed to redefine class:", e);
        }
    }

    @Nullable
    public static Class<?> findAlternative(Class<?> clazz) {
        if (alternatives.containsKey(clazz)) {
            return alternatives.get(clazz);
        }
        if (!(Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers()))) {
            return clazz;//This shouldn't happen;
        } else {
            for (Class<?> c : Launch.INSTRUMENTATION.getAllLoadedClasses()) {
                if (ReflectionUtil.isExtend(c, clazz)) {
                    if (!Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers())) {
                        alternatives.put(clazz, c);
                        return c;
                    }
                }
            }
        }
        alternatives.put(clazz, null);
        return null;//This shouldn't happen.
    }
}
