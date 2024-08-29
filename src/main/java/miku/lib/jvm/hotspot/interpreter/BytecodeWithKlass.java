package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.ConstantPool;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.oops.Symbol;


public class BytecodeWithKlass extends BytecodeWithCPIndex {
    BytecodeWithKlass(Method method, int bci) {
        super(method, bci);
    }

    protected Klass getKlass() {
        return this.method().getConstants().getKlassAt(this.index());
    }

    public Symbol getClassName() {
        ConstantPool.CPSlot obj = this.method().getConstants().getSlotAt(this.index());
        return obj.isUnresolved() ? obj.getSymbol() : obj.getKlass().getSymbol();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getJavaBytecodeName());
        buf.append(" ");
        buf.append('#');
        buf.append(this.index());
        buf.append(" ");
        buf.append("[Class ");
        buf.append(this.getClassName().toString().replace('/', '.'));
        buf.append(']');
        if (this.code() != this.javaCode()) {
            buf.append(" ");
            buf.append('[');
            buf.append(this.getBytecodeName());
            buf.append(']');
        }

        return buf.toString();
    }
}
