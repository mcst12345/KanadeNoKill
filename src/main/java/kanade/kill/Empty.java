package kanade.kill;

import me.xdark.shell.ShellcodeRunner;
import one.helfy.JVM;
import one.helfy.Type;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static me.xdark.shell.ShellcodeRunner.getSymbol;

public class Empty implements Opcodes {
    private static final Method getInterfaces0;
    static long test = 0;
    private static Unsafe unsafe;

    static {
        getUnsafe();
        try {
            getInterfaces0 = Class.class.getDeclaredMethod("getInterfaces0");
            getInterfaces0.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Empty() {
    }

    public static void test() {
        System.out.println("Test is called!");
    }

    public static void test1() {
        System.out.println("Test1  is  called!");
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
        for(int i = 0 ; i < 20001 ; i++){
            //test();
            //test1();
        }

        //test();
        //test1();
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println(pid);
        //new byte[]{(byte) 0x90, (byte) 0xeb, (byte) 0xfd} halt
        //ShellcodeRunner.SetCompiledEntry(Empty.class, "test", "(Ljava/lang/String;)V",ShellcodeRunner.GetCompiledEntry(Empty.class,"wuizfnwf","()V") );
        Class<?> clazz = Class.forName("sun.reflect.NativeMethodAccessorImpl");
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = jvm.intConstant("oopSize");
        long klassOffset = jvm.	getInt(jvm.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(clazz, klassOffset)
                : unsafe.getInt(clazz, klassOffset) & 0xffffffffL;
        long methodArray = jvm.getAddress(klass + jvm.type("InstanceKlass").offset("_methods"));
        int methodCount = jvm.getInt(methodArray);
        long methods = methodArray + jvm.type("Array<Method*>").offset("_data");

        long constMethodOffset = jvm.type("Method").offset("_constMethod");
        Type constMethodType = jvm.type("ConstMethod");
        Type constantPoolType = jvm.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = jvm.type("Method").offset("_from_compiled_entry");
        long _from_interpreted_entry = jvm.type("Method").offset("_from_interpreted_entry");//AccessFlags
        System.out.println(Arrays.toString(jvm.type("AccessFlags").fields));
	    System.out.println(Arrays.toString(jvm.type("Method").fields));
        System.out.println(Arrays.toString(jvm.type("ConstantPool").fields));
        System.out.println(Arrays.toString(jvm.type("InstanceKlass").fields));
        long access_flag = jvm.type("Method").offset("_access_flags");


        for (int i = 0; i < methodCount; i++) {
            long method = jvm.getAddress(methods + (long) i * oopSize);
            long constMethod = jvm.getAddress(method + constMethodOffset);

            long constantPool = jvm.getAddress(constMethod + constantPoolOffset);
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;
            String s = getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize);
            System.out.println(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize) + getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize));

            int access = getUnsafe().getInt(method+access_flag);
            System.out.println(access);
            System.out.println(Modifier.toString(access));
            if(Modifier.isPrivate(access)) {
                access &= ~ ACC_PRIVATE;
                access |= ACC_PUBLIC;
                getUnsafe().putInt(method+access_flag,access);
            }
        }



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
