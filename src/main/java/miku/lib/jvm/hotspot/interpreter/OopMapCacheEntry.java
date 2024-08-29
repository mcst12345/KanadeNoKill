package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.CellTypeState;
import miku.lib.jvm.hotspot.oops.CellTypeStateList;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.utilities.BitMap;

public class OopMapCacheEntry {
    private Method method;
    private int bci;
    private int maskSize;
    private BitMap mask;

    public OopMapCacheEntry() {
    }

    public boolean isValue(int offset) {
        return !this.entryAt(offset);
    }

    public boolean isOop(int offset) {
        return this.entryAt(offset);
    }

    public void iterateOop(OffsetClosure oopClosure) {
        int n = this.numberOfEntries();

        for (int i = 0; i < n; ++i) {
            if (this.entryAt(i)) {
                oopClosure.offsetDo(i);
            }
        }

    }

    public void fill(Method method, int bci) {
        this.method = method;
        this.bci = bci;
        if (method.isNative()) {
            this.fillForNative();
        } else {
            OopMapForCacheEntry gen = new OopMapForCacheEntry(method, bci, this);
            gen.computeMap();
        }

    }

    public void setMask(CellTypeStateList vars, CellTypeStateList stack, int stackTop) {
        int maxLocals = (int) this.method.getMaxLocals();
        int nEntries = maxLocals + stackTop;
        this.maskSize = nEntries;
        this.allocateBitMask();
        CellTypeStateList curList = vars;
        int listIdx = 0;

        for (int entryIdx = 0; entryIdx < nEntries; ++listIdx) {
            if (entryIdx == maxLocals) {
                curList = stack;
                listIdx = 0;
            }

            CellTypeState cell = curList.get(listIdx);
            if (cell.isReference()) {
                this.mask.atPut(entryIdx, true);
            }

            ++entryIdx;
        }


    }

    Method method() {
        return this.method;
    }

    int bci() {
        return this.bci;
    }

    int numberOfEntries() {
        return this.maskSize;
    }

    boolean entryAt(int offset) {
        return this.mask.at(offset);
    }

    void setEmptyMask() {
        this.mask = null;
    }

    void allocateBitMask() {
        if (this.maskSize > 0) {
            this.mask = new BitMap(this.maskSize);
        }

    }

    void fillForNative() {

        this.maskSize = this.method.getSizeOfParameters();
        this.allocateBitMask();
        MaskFillerForNative mf = new MaskFillerForNative(this.method, this.mask, this.maskSize);
        mf.generate();
    }

    boolean verifyMask(CellTypeStateList vars, CellTypeStateList stack, int maxLocals, int stackTop) {
        VerifyClosure blk = new VerifyClosure(this);
        this.iterateOop(blk);
        return !blk.failed();
    }

    static class VerifyClosure implements OffsetClosure {
        private OopMapCacheEntry entry;
        private boolean failed;

        VerifyClosure(OopMapCacheEntry entry) {
            this.entry = entry;
        }

        public void offsetDo(int offset) {
            if (!this.entry.isOop(offset)) {
                this.failed = true;
            }

        }

        boolean failed() {
            return this.failed;
        }
    }
}
