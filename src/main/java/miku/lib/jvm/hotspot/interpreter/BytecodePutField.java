package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodePutField extends BytecodeGetPut {
    BytecodePutField(Method method, int bci) {
        super(method, bci);
    }

    public boolean isStatic() {
        return false;
    }


    public boolean isValid() {
        return this.javaCode() == 181;
    }

    public static BytecodePutField at(Method method, int bci) {
        return new BytecodePutField(method, bci);
    }

    public static BytecodePutField atCheck(Method method, int bci) {
        BytecodePutField b = new BytecodePutField(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodePutField at(BytecodeStream bcs) {
        return new BytecodePutField(bcs.method(), bcs.bci());
    }
}
