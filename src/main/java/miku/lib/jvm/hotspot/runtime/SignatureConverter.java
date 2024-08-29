package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Symbol;

public class SignatureConverter extends SignatureIterator {
    private StringBuffer buf;
    private boolean first = true;

    public SignatureConverter(Symbol sig, StringBuffer buf) {
        super(sig);
        this.buf = buf;
    }

    public void doBool() {
        this.appendComma();
        this.buf.append("boolean");
    }

    public void doChar() {
        this.appendComma();
        this.buf.append("char");
    }

    public void doFloat() {
        this.appendComma();
        this.buf.append("float");
    }

    public void doDouble() {
        this.appendComma();
        this.buf.append("double");
    }

    public void doByte() {
        this.appendComma();
        this.buf.append("byte");
    }

    public void doShort() {
        this.appendComma();
        this.buf.append("short");
    }

    public void doInt() {
        this.appendComma();
        this.buf.append("int");
    }

    public void doLong() {
        this.appendComma();
        this.buf.append("long");
    }

    public void doVoid() {
        if (this.isReturnType()) {
            this.appendComma();
            this.buf.append("void");
        } else {
            throw new RuntimeException("Should not reach here");
        }
    }

    public void doObject(int begin, int end) {
        this.doObject(begin, end, true);
    }

    public void doArray(int begin, int end) {
        this.appendComma();
        int inner = this.arrayInnerBegin(begin);
        switch (this._signature.getByteAt((long) inner)) {
            case 66:
                this.buf.append("byte");
                break;
            case 67:
                this.buf.append("char");
                break;
            case 68:
                this.buf.append("double");
            case 69:
            case 71:
            case 72:
            case 75:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            default:
                break;
            case 70:
                this.buf.append("float");
                break;
            case 73:
                this.buf.append("int");
                break;
            case 74:
                this.buf.append("long");
                break;
            case 76:
                this.doObject(inner + 1, end, false);
                break;
            case 83:
                this.buf.append("short");
                break;
            case 90:
                this.buf.append("boolean");
        }

        for (int i = 0; i < inner - begin + 1; ++i) {
            this.buf.append("[]");
        }

    }

    public void appendComma() {
        if (!this.first) {
            this.buf.append(", ");
        }

        this.first = false;
    }

    private void doObject(int begin, int end, boolean comma) {
        if (comma) {
            this.appendComma();
        }

        this.appendSubstring(begin, end - 1);
    }

    private void appendSubstring(int begin, int end) {
        for (int i = begin; i < end; ++i) {
            this.buf.append((char) (this._signature.getByteAt((long) i) & 255));
        }

    }

    private int arrayInnerBegin(int begin) {
        while (this._signature.getByteAt((long) begin) == 91) {
            ++begin;
        }

        return begin;
    }
}
