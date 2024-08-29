package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public abstract class BytecodeWideable extends Bytecode {
    BytecodeWideable(Method method, int bci) {
        super(method, bci);
    }

    public boolean isWide() {
        int prevBci = this.bci() - 1;
        return prevBci > -1 && this.method.getBytecodeOrBPAt(prevBci) == 196;
    }

    public int getLocalVarIndex() {
        return this.isWide() ? this.getIndexU2(this.code(), true) : this.getIndexU1();
    }
}
