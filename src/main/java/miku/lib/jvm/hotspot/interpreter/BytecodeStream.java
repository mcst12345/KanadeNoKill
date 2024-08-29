package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public class BytecodeStream {
    private Method _method;
    private int _bci;
    private int _next_bci;
    private int _end_bci;
    private int _code;
    private boolean _is_wide;

    public BytecodeStream(Method method) {
        this._method = method;
        this.setInterval(0, (int) method.getCodeSize());
    }

    public void setInterval(int beg_bci, int end_bci) {
        this._bci = beg_bci;
        this._next_bci = beg_bci;
        this._end_bci = end_bci;
    }

    public void setStart(int beg_bci) {
        this.setInterval(beg_bci, this._method.getCodeSize());
    }

    public int next() {
        this._bci = this._next_bci;
        int code;
        if (this.isLastBytecode()) {
            code = -1;
        } else {
            int rawCode = Bytecodes.codeAt(this._method, this._bci);

            code = Bytecodes.javaCode(rawCode);

            int l = Bytecodes.lengthFor(code);
            if (l == 0) {
                l = Bytecodes.lengthAt(this._method, this._bci);
            }

            this._next_bci += l;

            this._is_wide = false;
            if (code == 196) {
                code = this._method.getBytecodeOrBPAt(this._bci + 1);
                this._is_wide = true;
            }

        }

        this._code = code;
        return this._code;
    }

    public Method method() {
        return this._method;
    }

    public int bci() {
        return this._bci;
    }

    public int nextBCI() {
        return this._next_bci;
    }

    public int endBCI() {
        return this._end_bci;
    }

    public int code() {
        return this._code;
    }

    public boolean isWide() {
        return this._is_wide;
    }

    public boolean isActiveBreakpoint() {
        return Bytecodes.isActiveBreakpointAt(this._method, this._bci);
    }

    public boolean isLastBytecode() {
        return this._next_bci >= this._end_bci;
    }

    public void setNextBCI(int bci) {

        this._next_bci = bci;
    }

    public int dest() {
        return this.bci() + this._method.getBytecodeShortArg(this.bci() + 1);
    }

    public int dest_w() {
        return this.bci() + this._method.getBytecodeIntArg(this.bci() + 1);
    }

    public int getIndex() {
        return this.isWide() ? this._method.getBytecodeShortArg(this.bci() + 2) & '\uffff' : this._method.getBytecodeOrBPAt(this.bci() + 1) & 255;
    }

    public int getIndexU1() {
        return this._method.getBytecodeOrBPAt(this.bci() + 1) & 255;
    }

    public int getIndexU2() {
        return this._method.getBytecodeShortArg(this.bci() + 1) & '\uffff';
    }

    public int getIndexU4() {
        return this._method.getNativeIntArg(this.bci() + 1);
    }

    public boolean hasIndexU4() {
        return this.code() == 186;
    }

    public int getIndexU1Cpcache() {
        return this._method.getBytecodeOrBPAt(this.bci() + 1) & 255;
    }

    public int getIndexU2Cpcache() {
        return this._method.getNativeShortArg(this.bci() + 1) & '\uffff';
    }

    public int codeAt(int bci) {
        return this._method.getBytecodeOrBPAt(bci);
    }
}
