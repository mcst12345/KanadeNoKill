package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.runtime.NativeSignatureIterator;
import miku.lib.jvm.hotspot.utilities.BitMap;

public class MaskFillerForNative extends NativeSignatureIterator {
    private BitMap mask;
    private int size;

    MaskFillerForNative(Method method, BitMap mask, int maskSize) {
        super(method);
        this.mask = mask;
        this.size = maskSize;
    }

    public void passInt() {
    }

    public void passLong() {
    }

    public void passFloat() {
    }

    public void passDouble() {
    }

    public void passObject() {
        this.mask.atPut(this.offset(), true);
    }

    public void generate() {
        super.iterate();
    }
}
