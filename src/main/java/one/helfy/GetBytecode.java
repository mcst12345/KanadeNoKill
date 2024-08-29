package one.helfy;

import miku.lib.utils.InternalUtils;
import sun.misc.Unsafe;

import java.nio.charset.StandardCharsets;

public class GetBytecode {

    public static void main(String[] args) {
        JVM jvm = JVM.getInstance();
        Unsafe unsafe = InternalUtils.getUnsafe();
        Class targetClass = String.class;

        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(targetClass, klassOffset)
                : unsafe.getInt(targetClass, klassOffset) & 0xffffffffL;

        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");

        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long codeSizeOffset = constMethodType.offset("_code_size");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long symbolBodyOffset = JVM.type("Symbol").offset("_body");

        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            long constMethod = JVM.getAddress(method + constMethodOffset);

            long constantPool = JVM.getAddress(constMethod + constantPoolOffset);
            int codeSize = jvm.getShort(constMethod + codeSizeOffset) & 0xffff;
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

            String name = getSymbol(jvm, constantPool + constantPoolType.size + (long) nameIndex * oopSize);
            String signature = getSymbol(jvm, constantPool + constantPoolType.size + (long) signatureIndex * oopSize);
            System.out.println("Method " + name + signature);

            long bytecodeStart = constMethod + constMethodType.size;
            for (int bci = 0; bci < codeSize; bci++) {
                System.out.printf(" %02x", JVM.getByte(bytecodeStart + bci));
            }
            System.out.println();
        }
    }

    private static String getSymbol(JVM jvm, long symbolAddress) {
        Type symbolType = JVM.type("Symbol");
        long symbol = JVM.getAddress(symbolAddress);
        long body = symbol + symbolType.offset("_body");
        int length = jvm.getShort(symbol + symbolType.offset("_length")) & 0xffff;

        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = JVM.getByte(body + i);
        }
        return new String(b, StandardCharsets.UTF_8);
    }
}
