package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.ConstantPool;
import miku.lib.jvm.hotspot.oops.Field;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.oops.Symbol;
import miku.lib.jvm.hotspot.runtime.SignatureConverter;

public abstract class BytecodeGetPut extends BytecodeWithCPIndex {
    BytecodeGetPut(Method method, int bci) {
        super(method, bci);
    }

    public Symbol name() {
        ConstantPool cp = this.method().getConstants();
        return cp.getNameRefAt(this.index());
    }

    public Symbol signature() {
        ConstantPool cp = this.method().getConstants();
        return cp.getSignatureRefAt(this.index());
    }

    public Field getField() {
        return this.method().getConstants().getFieldRefAt(this.index());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getJavaBytecodeName());
        buf.append(" ");
        buf.append('#');
        buf.append(this.indexForFieldOrMethod());
        buf.append(" [Field ");
        StringBuffer sigBuf = new StringBuffer();
        (new SignatureConverter(this.signature(), sigBuf)).dispatchField();
        buf.append(sigBuf.toString().replace('/', '.'));
        buf.append(" ");
        buf.append(this.name().toString());
        buf.append("]");
        if (this.code() != this.javaCode()) {
            buf.append(" ");
            buf.append('[');
            buf.append(this.getBytecodeName());
            buf.append(']');
        }

        return buf.toString();
    }

    public abstract boolean isStatic();
}
