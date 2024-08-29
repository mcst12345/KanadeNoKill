package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.utils.NumberTransformer;
import miku.lib.jvm.hotspot.memory.Universe;
import miku.lib.jvm.hotspot.runtime.BasicType;
import miku.lib.jvm.hotspot.runtime.VM;
import one.helfy.Type;

public class Array extends Oop {
    public Array(long address) {
        super(address);
    }

    private static final long headerSize;
    private static long lengthOffsetInBytes;
    private static final long typeSize;

    static {
        Type type = jvm.type("arrayOopDesc");
        typeSize = type.size;
        if (VM.compressedKlassPointersEnabled) {
            headerSize = typeSize;
        } else {
            headerSize = VM.alignUp(typeSize + VM.JIntSize, VM.heapWordSize);
        }
        if (VM.compressedKlassPointersEnabled) {
            lengthOffsetInBytes = typeSize - VM.JIntSize;
        } else {
            lengthOffsetInBytes = typeSize;
        }
    }

    private static long headerSize(BasicType type) {
        return Universe.elementTypeShouldBeAligned(type) ? alignObjectSize(headerSize) / (long) VM.heapWordSize : headerSize / (long) VM.heapWordSize;
    }

    public static long alignObjectSize(long size) {
        return VM.alignUp(size, VM.objectAlignmentInBytes);
    }

    public long getLength() {
        return NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + lengthOffsetInBytes, VM.JIntSize), true);
    }
}
