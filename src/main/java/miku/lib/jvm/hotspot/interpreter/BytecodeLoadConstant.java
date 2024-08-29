package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.*;
import miku.lib.jvm.hotspot.runtime.BasicType;
import miku.lib.jvm.hotspot.utilities.ConstantTag;

public class BytecodeLoadConstant extends Bytecode {
    BytecodeLoadConstant(Method method, int bci) {
        super(method, bci);
    }


    public boolean hasCacheIndex() {
        return this.code() >= 203;
    }

    int rawIndex() {
        return this.javaCode() == 18 ? this.getIndexU1() : this.getIndexU2(this.code(), false);
    }

    public int poolIndex() {
        int index = this.rawIndex();
        return this.hasCacheIndex() ? this.method().getConstants().objectToCPIndex(index) : index;
    }

    public int cacheIndex() {
        return this.hasCacheIndex() ? this.rawIndex() : -1;
    }

    public BasicType resultType() {
        int index = this.poolIndex();
        ConstantTag tag = this.method().getConstants().getTagAt(index);
        return tag.basicType();
    }

    private Oop getCachedConstant() {
        int i = this.cacheIndex();
        if (i >= 0) {
            throw new InternalError("invokedynamic not implemented yet");
        } else {
            return null;
        }
    }

    public boolean isValid() {
        int jcode = this.javaCode();
        boolean codeOk = jcode == 18 || jcode == 19 || jcode == 20;
        if (!codeOk) {
            return false;
        } else {
            ConstantTag ctag = this.method().getConstants().getTagAt(this.poolIndex());
            if (jcode == 20) {
                return ctag.isDouble() || ctag.isLong();
            } else {
                return ctag.isString() || ctag.isUnresolvedKlass() || ctag.isKlass() || ctag.isMethodHandle() || ctag.isMethodType() || ctag.isInt() || ctag.isFloat();
            }
        }
    }

    public boolean isKlassConstant() {
        int jcode = this.javaCode();
        if (jcode == 20) {
            return false;
        } else {
            ConstantTag ctag = this.method().getConstants().getTagAt(this.poolIndex());
            return ctag.isKlass() || ctag.isUnresolvedKlass();
        }
    }

    public Object getKlass() {
        ConstantPool cpool = this.method().getConstants();
        int cpIndex = this.poolIndex();
        ConstantPool.CPSlot oop = cpool.getSlotAt(cpIndex);
        if (oop.isResolved()) {
            return oop.getKlass();
        } else if (oop.isUnresolved()) {
            return oop.getSymbol();
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public static BytecodeLoadConstant at(Method method, int bci) {
        return new BytecodeLoadConstant(method, bci);
    }

    public static BytecodeLoadConstant atCheck(Method method, int bci) {
        BytecodeLoadConstant b = new BytecodeLoadConstant(method, bci);
        return b.isValid() ? b : null;
    }

    public static BytecodeLoadConstant at(BytecodeStream bcs) {
        return new BytecodeLoadConstant(bcs.method(), bcs.bci());
    }

    public String getConstantValue() {
        ConstantPool cpool = this.method().getConstants();
        int cpIndex = this.poolIndex();
        ConstantTag ctag = cpool.getTagAt(cpIndex);
        if (ctag.isInt()) {
            return "<int " + cpool.getIntAt(cpIndex) + ">";
        } else if (ctag.isLong()) {
            return "<long " + cpool.getLongAt(cpIndex) + "L>";
        } else if (ctag.isFloat()) {
            return "<float " + cpool.getFloatAt(cpIndex) + "F>";
        } else if (ctag.isDouble()) {
            return "<double " + cpool.getDoubleAt(cpIndex) + "D>";
        } else if (ctag.isString()) {
            Symbol sym = cpool.getUnresolvedStringAt(cpIndex);
            return "<String \"" + sym.toString() + "\">";
        } else if (!ctag.isKlass() && !ctag.isUnresolvedKlass()) {
            Oop x;
            int refidx;
            if (ctag.isMethodHandle()) {
                x = this.getCachedConstant();
                refidx = cpool.getMethodHandleIndexAt(cpIndex);
                int refkind = cpool.getMethodHandleRefKindAt(cpIndex);
                return "<MethodHandle kind=" + refkind + " ref=" + refidx + (x == null ? "" : " @" + x.getAddress()) + ">";
            } else if (ctag.isMethodType()) {
                x = this.getCachedConstant();
                refidx = cpool.getMethodTypeIndexAt(cpIndex);
                return "<MethodType " + cpool.getSymbolAt(refidx).toString() + (x == null ? "" : " @" + x.getAddress()) + ">";
            } else {

                return null;
            }
        } else {
            ConstantPool.CPSlot obj = cpool.getSlotAt(cpIndex);
            if (obj.isResolved()) {
                Klass k = obj.getKlass();
                return "<Class " + k.getName() + "@" + k.getAddress() + ">";
            } else if (obj.isUnresolved()) {
                Symbol sym = obj.getSymbol();
                return "<Class " + sym.toString() + ">";
            } else {
                throw new RuntimeException("should not reach here");
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getJavaBytecodeName());
        buf.append(" ");
        buf.append('#');
        buf.append(this.poolIndex());
        if (this.hasCacheIndex()) {
            buf.append('(');
            buf.append(this.cacheIndex());
            buf.append(')');
        }

        buf.append(" ");
        buf.append(this.getConstantValue());
        if (this.code() != this.javaCode()) {
            buf.append(" ");
            buf.append('[');
            buf.append(this.getBytecodeName());
            buf.append(']');
        }

        return buf.toString();
    }
}
