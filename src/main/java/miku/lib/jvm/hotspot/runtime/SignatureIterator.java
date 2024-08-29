package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Symbol;

public abstract class SignatureIterator {
    protected Symbol _signature;
    protected int _index;
    protected int _parameter_index;

    protected void expect(char c) {
        if (this._signature.getByteAt(this._index) != (byte) c) {
            throw new RuntimeException("expecting '" + c + "'");
        } else {
            ++this._index;
        }
    }

    protected void skipOptionalSize() {
        for (byte c = this._signature.getByteAt(this._index); 48 <= c && c <= 57; c = this._signature.getByteAt(++this._index)) {
        }

    }

    protected int parseType() {
        int begin;
        switch (this._signature.getByteAt(this._index)) {
            case 66:
                this.doByte();
                ++this._index;
                return BasicTypeSize.getTByteSize();
            case 67:
                this.doChar();
                ++this._index;
                return BasicTypeSize.getTCharSize();
            case 68:
                this.doDouble();
                ++this._index;
                return BasicTypeSize.getTDoubleSize();
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
            case 87:
            case 88:
            case 89:
            default:
                throw new RuntimeException("Should not reach here: char " + (char) this._signature.getByteAt(this._index) + " @ " + this._index + " in " + this._signature.toString());
            case 70:
                this.doFloat();
                ++this._index;
                return BasicTypeSize.getTFloatSize();
            case 73:
                this.doInt();
                ++this._index;
                return BasicTypeSize.getTIntSize();
            case 74:
                this.doLong();
                ++this._index;
                return BasicTypeSize.getTLongSize();
            case 76:
                begin = ++this._index;

                while (this._signature.getByteAt(this._index++) != 59) {
                }

                this.doObject(begin, this._index);
                return BasicTypeSize.getTObjectSize();
            case 83:
                this.doShort();
                ++this._index;
                return BasicTypeSize.getTShortSize();
            case 86:
                if (!this.isReturnType()) {
                    throw new RuntimeException("illegal parameter type V (void)");
                }

                this.doVoid();
                ++this._index;
                return BasicTypeSize.getTVoidSize();
            case 90:
                this.doBool();
                ++this._index;
                return BasicTypeSize.getTBooleanSize();
            case 91:
                begin = ++this._index;
                this.skipOptionalSize();

                while (this._signature.getByteAt(this._index) == 91) {
                    ++this._index;
                    this.skipOptionalSize();
                }

                if (this._signature.getByteAt(this._index) == 76) {
                    while (true) {
                        if (this._signature.getByteAt(this._index++) != 59) {
                            continue;
                        }
                    }
                } else {
                    ++this._index;
                }

                this.doArray(begin, this._index);
                return BasicTypeSize.getTArraySize();
        }
    }

    protected void checkSignatureEnd() {
        if ((long) this._index < this._signature.length()) {
            System.err.println("too many chars in signature");
            System.err.println(_signature.toString());
            System.err.println(" @ " + this._index);
        }

    }

    public SignatureIterator(Symbol signature) {
        this._signature = signature;
        this._parameter_index = 0;
    }

    public void dispatchField() {
        this._index = 0;
        this._parameter_index = 0;
        this.parseType();
        this.checkSignatureEnd();
    }

    public void iterateParameters() {
        this._index = 0;
        this._parameter_index = 0;
        this.expect('(');

        while (this._signature.getByteAt(this._index) != 41) {
            this._parameter_index += this.parseType();
        }

        this.expect(')');
        this._parameter_index = 0;
    }

    public void iterateReturntype() {
        this._index = 0;
        this.expect('(');

        while (this._signature.getByteAt(this._index) != 41) {
            ++this._index;
        }

        this.expect(')');
        this._parameter_index = -1;
        this.parseType();
        this.checkSignatureEnd();
        this._parameter_index = 0;
    }

    public void iterate() {
        this._index = 0;
        this._parameter_index = 0;
        this.expect('(');

        while (this._signature.getByteAt(this._index) != 41) {
            this._parameter_index += this.parseType();
        }

        this.expect(')');
        this._parameter_index = -1;
        this.parseType();
        this.checkSignatureEnd();
        this._parameter_index = 0;
    }

    public int parameterIndex() {
        return this._parameter_index;
    }

    public boolean isReturnType() {
        return this.parameterIndex() < 0;
    }

    public abstract void doBool();

    public abstract void doChar();

    public abstract void doFloat();

    public abstract void doDouble();

    public abstract void doByte();

    public abstract void doShort();

    public abstract void doInt();

    public abstract void doLong();

    public abstract void doVoid();

    public abstract void doObject(int var1, int var2);

    public abstract void doArray(int var1, int var2);
}
