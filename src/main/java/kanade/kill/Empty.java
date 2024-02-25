package kanade.kill;

import me.xdark.shell.ShellcodeRunner;
import one.helfy.JVM;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static me.xdark.shell.ShellcodeRunner.getSymbol;

public class Empty {
    public static final Method EmptyVoidMethod;
    private static final Method getInterfaces0;
    private static final Method doNothing;
    private static final Method w;
    static long test = 0;
    private static int x1;
    private static Unsafe unsafe;

    static {
        try {
            w = Empty.class.getDeclaredMethod("wuizfnwf");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            doNothing = Empty.class.getDeclaredMethod("doNothing", long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        getUnsafe();
        try {
            getInterfaces0 = Class.class.getDeclaredMethod("getInterfaces0");
            getInterfaces0.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            EmptyVoidMethod = Empty.class.getDeclaredMethod("EmptyVoidMethod");
            int i = 20000;
            while (i-- > 0) {
                EmptyVoidMethod();
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Empty() {
    }

    public static long test(String abc) {
        x1++;
        test += 2;
        System.out.println("now we are going to return");
        return Integer.parseInt(abc);
    }

    public static void doNothing(long abc) {
        System.out.println("Do nothing is called!!!!");
        System.out.println(abc);
        System.out.println("Nyanyanyanyanyanya!!!");
        System.load(null);
    }

    public static void diwejdo() {
        System.out.println(1);
        System.out.println(4);
        System.out.println(3);
        System.out.println(11);
        System.out.println(2);
    }

    public static void doNothing2() {
    }

    public static void euidhewdwhdfewhiu() {
    }

    public static void main(String[] args) throws Throwable {
        Empty empty = (Empty) getUnsafe().allocateInstance(Empty.class);
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println(pid);
        //new byte[]{(byte) 0x90, (byte) 0xeb, (byte) 0xfd} halt
        //ShellcodeRunner.SetCompiledEntry(Empty.class, "test", "(Ljava/lang/String;)V",ShellcodeRunner.GetCompiledEntry(Empty.class,"wuizfnwf","()V") );
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = jvm.intConstant("oopSize");
        long klassOffset = jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(Empty.class, klassOffset)
                : unsafe.getInt(Empty.class, klassOffset) & 0xffffffffL;
        long methodArray = jvm.getAddress(klass + jvm.type("InstanceKlass").offset("_methods"));
        int methodCount = jvm.getInt(methodArray);
        long methods = methodArray + jvm.type("Array<Method*>").offset("_data");

        long constMethodOffset = jvm.type("Method").offset("_constMethod");
        one.helfy.Type constMethodType = jvm.type("ConstMethod");
        one.helfy.Type constantPoolType = jvm.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = jvm.type("Method").offset("_from_compiled_entry");
        one.helfy.Type t = jvm.type("ConstMethod");


        System.out.println(test("12132"));
        System.out.println(test("4343"));
        System.out.println(test("655656"));

        long a = t.size;
        for (int i = 0; i < methodCount; i++) {
            long method = jvm.getAddress(methods + (long) i * oopSize);
            long constMethod = jvm.getAddress(method + constMethodOffset);

            long constantPool = jvm.getAddress(constMethod + constantPoolOffset);
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;
            if (!getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize).equals("test")) {
                continue;
            }
            System.out.println(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize) + getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize));
            //System.out.println("START");
            //for(int ii = 0 ; ii < 10 ; ii++){
            //    System.out.println(scala.concurrent.util.Unsafe.instance.getByte(constMethod+a+ii) & 255);
            //}
            scala.concurrent.util.Unsafe.instance.putByte(constMethod + a, (byte) Opcodes.LCONST_1);
            scala.concurrent.util.Unsafe.instance.putByte(constMethod + a + 2, (byte) Opcodes.LRETURN);
            //System.out.println("END");
        }

        System.out.println(test("12132"));
        System.out.println(test("4343"));
        System.out.println(test("655656"));
        //System.out.println(x1);
        //System.out.println(test);
    }

    public static void Test() {
        System.out.println("Test is called!!!");
    }

    private static void print(long address) {
        System.out.println(Long.toHexString(address));
    }

    public static Unsafe getUnsafe() {
        if (unsafe == null) {
            Field f;
            try {
                f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            f.setAccessible(true);
        }
        return unsafe;
    }

    public static long location(Object object) {

        Unsafe unsafe = getUnsafe();


        Object[] array = new Object[]{object};


        long baseOffset = unsafe.arrayBaseOffset(Object[].class);

        int addressSize = unsafe.addressSize();

        long location;

        switch (addressSize) {

            case 4:

                location = unsafe.getInt(array, baseOffset);

                break;

            case 8:

                location = unsafe.getLong(array, baseOffset);

                break;

            default:

                throw new Error("unsupported address size: " + addressSize);

        }

        return (location);

    }

    private static void EmptyVoidMethod() {

    }

    public void wuizfnwf() {
        System.out.println("je3r3urh34uirfh34uifh3if4");
    }

    private static class A {
        public String m1() {
            return "weudhwA@(@y9d3hd2h2H98hxh3x2";
        }

        public String m2() {
            return "ijdjo12ijd21d22@(@y9d3hd2h2H98hxdwedwdwdwh3x2";
        }

        public String m3() {
            return "ejiwj3298u32j@*(E@(n8eu23";
        }

    }

    static class B extends A {
    }
}
