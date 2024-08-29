package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeBipush extends Bytecode {
    BytecodeBipush(Method method, int bci) {
        super(method, bci);
    }

    public byte getValue() {
        return this.javaByteAt(1);
    }

    public boolean isValid() {
        return this.javaCode() == 16;
    }

    public static BytecodeBipush at(Method method, int bci) {
        return new BytecodeBipush(method, bci);
    }

    public static BytecodeBipush atCheck(Method method, int bci) {
        BytecodeBipush b = new BytecodeBipush(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeBipush at(BytecodeStream bcs) {
        return new BytecodeBipush(bcs.method(), bcs.bci());
    }

    public String toString() {
        return "bipush" +
                " " +
                this.getValue();
    }
}
