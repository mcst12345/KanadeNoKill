package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.utilities.Bits;

public class Bytecode {
    Method method;
    int bci;

    static final int jintSize = 4;
    static final String spaces = " ";
    static final String comma = ", ";

    Bytecode(Method method, int bci) {
        this.method = method;
        this.bci = bci;
    }

    int alignedOffset(int offset) {
        return Bits.roundTo(this.bci + offset, 4) - this.bci;
    }

    public int getIndexU1() {
        return this.method.getBytecodeOrBPAt(this.bci() + 1) & 255;
    }

    public int getIndexU2(int bc, boolean isWide) {
        return can_use_native_byte_order(bc, isWide) ? this.method.getNativeShortArg(this.bci() + (isWide ? 2 : 1)) & '\uffff' : this.method.getBytecodeShortArg(this.bci() + (isWide ? 2 : 1)) & '\uffff';
    }

    public int getIndexU4() {
        return this.method.getNativeIntArg(this.bci() + 1);
    }

    public boolean hasIndexU4() {
        return this.code() == 186;
    }

    public int getIndexU1Cpcache() {
        return this.method.getBytecodeOrBPAt(this.bci() + 1) & 255;
    }

    public int getIndexU2Cpcache() {
        return this.method.getNativeShortArg(this.bci() + 1) & '\uffff';
    }

    public int code() {
        return Bytecodes.codeAt(this.method(), this.bci());
    }

    static boolean can_use_native_byte_order(int bc, boolean is_wide) {
        return Bytecodes.native_byte_order(bc);
    }

    int javaSignedWordAt(int offset) {
        return this.method.getBytecodeIntArg(this.bci + offset);
    }

    short javaShortAt(int offset) {
        return this.method.getBytecodeShortArg(this.bci + offset);
    }

    byte javaByteAt(int offset) {
        return this.method.getBytecodeByteArg(this.bci + offset);
    }

    public Method method() {
        return this.method;
    }

    public int bci() {
        return this.bci;
    }


    public int javaCode() {
        return Bytecodes.javaCode(this.code());
    }

    public String getBytecodeName() {
        return Bytecodes.name(this.code());
    }

    public String getJavaBytecodeName() {
        return Bytecodes.name(this.javaCode());
    }

    public int getLength() {
        return Bytecodes.lengthAt(this.method(), this.bci());
    }

    public int getJavaLength() {
        return Bytecodes.javaLengthAt(this.method(), this.bci());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(this.getJavaBytecodeName());
        if (this.code() != this.javaCode()) {
            buf.append(" ");
            buf.append('[');
            buf.append(this.getBytecodeName());
            buf.append(']');
        }

        return buf.toString();
    }
}
