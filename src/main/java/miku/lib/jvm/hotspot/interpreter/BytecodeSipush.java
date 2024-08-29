package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeSipush extends Bytecode {
    BytecodeSipush(Method method, int bci) {
        super(method, bci);
    }


    public short getValue() {
        return this.javaShortAt(1);
    }

    public boolean isValid() {
        return this.javaCode() == 17;
    }

    public static BytecodeSipush at(Method method, int bci) {
        return new BytecodeSipush(method, bci);
    }

    public static BytecodeSipush atCheck(Method method, int bci) {
        BytecodeSipush b = new BytecodeSipush(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeSipush at(BytecodeStream bcs) {
        return new BytecodeSipush(bcs.method(), bcs.bci());
    }

    public String toString() {
        return "sipush" +
                " " +
                this.getValue();
    }
}
