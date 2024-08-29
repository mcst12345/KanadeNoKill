package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public abstract class BytecodeJmp extends Bytecode {
    BytecodeJmp(Method method, int bci) {
        super(method, bci);
    }

    public abstract int getTargetBCI();

    public String toString() {
        return this.getJavaBytecodeName() +
                " " +
                this.getTargetBCI();
    }
}
