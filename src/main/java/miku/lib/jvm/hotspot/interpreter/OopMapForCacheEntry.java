package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.CellTypeStateList;
import miku.lib.jvm.hotspot.oops.GenerateOopMap;
import miku.lib.jvm.hotspot.oops.Method;

import java.util.List;

public class OopMapForCacheEntry extends GenerateOopMap {
    private OopMapCacheEntry entry;
    private int bci;
    private int stackTop;

    OopMapForCacheEntry(Method method, int bci, OopMapCacheEntry entry) {
        super(method);
        this.entry = entry;
        this.bci = bci;
        this.stackTop = -1;
    }

    public boolean reportResults() {
        return false;
    }

    public boolean possibleGCPoint(BytecodeStream bcs) {
        return false;
    }

    public void fillStackmapProlog(int nof_gc_points) {
    }

    public void fillStackmapEpilog() {
    }

    public void fillStackmapForOpcodes(BytecodeStream bcs, CellTypeStateList vars, CellTypeStateList stack, int stackTop) {
        if (bcs.bci() == this.bci) {
            this.entry.setMask(vars, stack, stackTop);
            this.stackTop = stackTop;
        }

    }

    public void fillInitVars(List initVars) {
    }

    public void computeMap() {

        if (this.method().getCodeSize() != 0L && this.method().getMaxLocals() + this.method().getMaxStack() != 0L) {
            super.computeMap();
            this.resultForBasicblock(this.bci);
        } else {
            this.entry.setEmptyMask();
        }

    }

    public int size() {

        return (this.method().isStatic() ? 0 : 1) + this.method().getMaxLocals() + this.stackTop;
    }
}
