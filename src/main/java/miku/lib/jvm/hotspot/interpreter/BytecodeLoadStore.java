package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public abstract class BytecodeLoadStore extends BytecodeWideable {
    BytecodeLoadStore(Method method, int bci) {
        super(method, bci);
    }

    public String toString() {
        return this.getJavaBytecodeName() +
                " " +
                '#' +
                this.getLocalVarIndex();
    }
}
