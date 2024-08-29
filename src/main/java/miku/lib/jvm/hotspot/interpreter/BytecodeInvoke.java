package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.ConstantPool;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.oops.Symbol;
import miku.lib.jvm.hotspot.runtime.ResultTypeFinder;
import miku.lib.jvm.hotspot.runtime.SignatureConverter;

public class BytecodeInvoke extends BytecodeWithCPIndex {
    BytecodeInvoke(Method method, int bci) {
        super(method, bci);
    }

    public static BytecodeInvoke at(Method method, int bci) {
        BytecodeInvoke b = new BytecodeInvoke(method, bci);

        return b;
    }

    public static BytecodeInvoke atCheck(Method method, int bci) {
        BytecodeInvoke b = new BytecodeInvoke(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeInvoke at(BytecodeStream bcs) {
        return new BytecodeInvoke(bcs.method(), bcs.bci());
    }

    public Symbol name() {
        ConstantPool cp = this.method().getConstants();
        return this.isInvokedynamic() ? cp.uncachedGetNameRefAt(this.indexForFieldOrMethod()) : cp.getNameRefAt(this.index());
    }

    public Symbol signature() {
        ConstantPool cp = this.method().getConstants();
        return this.isInvokedynamic() ? cp.uncachedGetSignatureRefAt(this.indexForFieldOrMethod()) : cp.getSignatureRefAt(this.index());
    }

    public Method getInvokedMethod() {
        return this.method().getConstants().getMethodRefAt(this.index());
    }

    public int resultType() {
        ResultTypeFinder rts = new ResultTypeFinder(this.signature());
        rts.iterate();
        return rts.type();
    }

    public int adjustedInvokeCode() {
        return this.javaCode();
    }

    public boolean isInvokeinterface() {
        return this.adjustedInvokeCode() == 185;
    }

    public boolean isInvokevirtual() {
        return this.adjustedInvokeCode() == 182;
    }

    public boolean isInvokestatic() {
        return this.adjustedInvokeCode() == 184;
    }

    public boolean isInvokespecial() {
        return this.adjustedInvokeCode() == 183;
    }

    public boolean isInvokedynamic() {
        return this.adjustedInvokeCode() == 186;
    }

    public boolean isValid() {
        return this.isInvokeinterface() || this.isInvokevirtual() || this.isInvokestatic() || this.isInvokespecial();
    }


    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getJavaBytecodeName());
        buf.append(" ");
        buf.append('#');
        buf.append(this.indexForFieldOrMethod());
        if (this.isInvokedynamic()) {
            buf.append('(');
            buf.append(this.index());
            buf.append(')');
        }

        buf.append(" [Method ");
        StringBuffer sigBuf = new StringBuffer();
        (new SignatureConverter(this.signature(), sigBuf)).iterateReturntype();
        buf.append(sigBuf.toString().replace('/', '.'));
        buf.append(" ");
        buf.append(this.name().toString());
        buf.append('(');
        sigBuf = new StringBuffer();
        (new SignatureConverter(this.signature(), sigBuf)).iterateParameters();
        buf.append(sigBuf.toString().replace('/', '.'));
        buf.append(')');
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
