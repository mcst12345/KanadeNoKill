package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodePutStatic extends BytecodeGetPut {
    BytecodePutStatic(Method method, int bci) {
        super(method, bci);
    }

    public boolean isStatic() {
        return true;
    }

    public boolean isValid() {
        return this.javaCode() == 179;
    }

    public static BytecodePutStatic at(Method method, int bci) {
        return new BytecodePutStatic(method, bci);
    }

    public static BytecodePutStatic atCheck(Method method, int bci) {
        BytecodePutStatic b = new BytecodePutStatic(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodePutStatic at(BytecodeStream bcs) {
        return new BytecodePutStatic(bcs.method(), bcs.bci());
    }

}
