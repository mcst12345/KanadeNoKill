package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.ConstantPool;
import miku.lib.jvm.hotspot.oops.ConstantPoolCache;
import miku.lib.jvm.hotspot.oops.Method;

public abstract class BytecodeWithCPIndex extends Bytecode {
    BytecodeWithCPIndex(Method method, int bci) {
        super(method, bci);
    }


    public int index() {
        if (this.code() == 186) {
            int index = this.getIndexU4();
            return ConstantPool.isInvokedynamicIndex(index) ? ConstantPool.decodeInvokedynamicIndex(index) : index;
        } else {
            return this.getIndexU2(this.code(), false);
        }
    }

    protected int indexForFieldOrMethod() {
        ConstantPoolCache cpCache = this.method().getConstants().getCache();
        int cpCacheIndex = this.index();
        return cpCache == null ? cpCacheIndex : cpCache.getEntryAt('\uffff' & cpCacheIndex).getConstantPoolIndex();
    }
}
