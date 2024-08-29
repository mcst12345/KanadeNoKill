package miku.lib.jvm.hotspot.runtime;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import miku.lib.jvm.hotspot.memory.SymbolTable;
import miku.lib.utils.InternalUtils;
import one.helfy.Field;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

public class VM {

    public static SymbolTable getSymbolTable() {
        return SymbolTable.getTheTable();
    }

    private static final JVM jvm = JVM.getInstance();
    public static final int JIntSize = JVM.type("jint").size;
    public static final int objectAlignmentInBytes;
    public static final int minObjAlignmentInBytes;
    public static final long stackBias;
    public static final int invocationEntryBCI;
    public static final int invalidOSREntryBCI;
    public static final int bytesPerWord;
    public static final int heapWordSize;
    public static final int oopSize;
    public static final boolean isLP64 = InternalUtils.getUnsafe().addressSize() == 8;

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static final int AddressSize = unsafe.addressSize();
    public static final int bytesPerLong;

    public static final int heapOopSize;
    public static final int klassPtrSize;


    public static final boolean compressedOopsEnabled;
    public static final Object2ByteOpenHashMap<String> Flags = new Object2ByteOpenHashMap<>();


    private static void readCommandLineFlags() {
        Type flagType = JVM.type("Flag");
        int flagSize = flagType.size;

        Field flagsField = flagType.field("flags");
        long flagsFieldAddress = JVM.getAddress(flagsField.offset);

        Field numFlagsField = flagType.field("numFlags");
        int numFlagsValue = JVM.getInt(numFlagsField.offset);

        Field _nameField = flagType.field("_name");
        Field _addrField = flagType.field("_addr");

        // iterate until `numFlagsValue - 1` because last flag contains null values
        for (int i = 0; i < numFlagsValue - 1; i++) {
            long flagAddress = flagsFieldAddress + ((long) i * flagSize);
            long flagValueAddress = JVM.getAddress(flagAddress + _addrField.offset);
            long flagNameAddress = JVM.getAddress(flagAddress + _nameField.offset);
            String flagName = JVM.getString(flagNameAddress);
            /*if ("UnlockDiagnosticVMOptions".equals(flagName)) {
                if (jvm.getByte(flagValueAddress) == 0) {
                    jvm.putByte(flagValueAddress, (byte) 1);
                    System.out.println(flagName + " has been enabled");
                } else {
                    System.out.println(flagName + " is already enabled");
                }
            }
             */
            Flags.put(flagName, unsafe.getByte(flagValueAddress));
        }
    }

    public static final boolean compressedKlassPointersEnabled;


    static {

        readCommandLineFlags();
        stackBias = JVM.intConstant("STACK_BIAS");
        invocationEntryBCI = JVM.intConstant("InvocationEntryBci");
        invalidOSREntryBCI = JVM.intConstant("InvalidOSREntryBci");
        bytesPerWord = JVM.intConstant("BytesPerWord");
        oopSize = JVM.intConstant("oopSize");
        heapWordSize = JVM.intConstant("HeapWordSize");
        bytesPerLong = JVM.intConstant("BytesPerLong");

        compressedOopsEnabled = Flags.getByte("UseCompressedOops") == 1;
        compressedKlassPointersEnabled = Flags.getByte("UseCompressedClassPointers") == 1;

        if (compressedOopsEnabled) {
            heapOopSize = JIntSize;
        } else {
            heapOopSize = oopSize;
        }

        if (compressedKlassPointersEnabled) {
            klassPtrSize = JIntSize;
        } else {
            klassPtrSize = oopSize;
        }

        objectAlignmentInBytes = Flags.getByte("ObjectAlignmentInBytes");
        minObjAlignmentInBytes = objectAlignmentInBytes;

    }

    public static long alignUp(long size, long alignment) {
        return size + alignment - 1L & -alignment;
    }

    public static long buildLongFromIntsPD(int oneHalf, int otherHalf) {
        return (long) oneHalf << 32 | (long) otherHalf & 4294967295L;
    }

    public static int buildIntFromShorts(short low, short high) {
        return high << 16 | low & '\uffff';
    }
}
