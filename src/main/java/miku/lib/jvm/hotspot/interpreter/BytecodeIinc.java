package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeIinc extends BytecodeWideable {
    BytecodeIinc(Method method, int bci) {
        super(method, bci);
    }

    public int getIncrement() {
        return this.isWide() ? this.javaShortAt(3) : this.javaByteAt(2);
    }

    public boolean isValid() {
        return this.javaCode() == 132;
    }

    public static BytecodeIinc at(Method method, int bci) {
        return new BytecodeIinc(method, bci);
    }

    public static BytecodeIinc atCheck(Method method, int bci) {
        BytecodeIinc b = new BytecodeIinc(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeIinc at(BytecodeStream bcs) {
        return new BytecodeIinc(bcs.method(), bcs.bci());
    }

    public String toString() {
        return "iinc" +
                " " +
                '#' +
                this.getLocalVarIndex() +
                " by " +
                this.getIncrement();
    }
}
