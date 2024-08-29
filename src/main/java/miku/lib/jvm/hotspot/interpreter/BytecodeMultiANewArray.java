package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeMultiANewArray extends BytecodeWithKlass {
    BytecodeMultiANewArray(Method method, int bci) {
        super(method, bci);
    }

    public Klass getKlass() {
        return super.getKlass();
    }

    public int getDimension() {
        return 255 & this.javaByteAt(2);
    }

    public boolean isValid() {
        return this.javaCode() == 197;
    }

    public static BytecodeMultiANewArray at(Method method, int bci) {

        return new BytecodeMultiANewArray(method, bci);
    }

    public static BytecodeMultiANewArray atCheck(Method method, int bci) {
        BytecodeMultiANewArray b = new BytecodeMultiANewArray(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeMultiANewArray at(BytecodeStream bcs) {
        return new BytecodeMultiANewArray(bcs.method(), bcs.bci());
    }

    public String toString() {
        return super.toString() +
                " " +
                this.getDimension();
    }
}
