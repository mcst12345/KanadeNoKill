package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Symbol;

public abstract class SignatureInfo extends SignatureIterator {
    public SignatureInfo(Symbol signature) {
        super(signature);
    }

    protected boolean hasIterated;
    protected int size;
    protected int type = BasicType.getTIllegal();

    protected void lazyIterate() {
        if (!this.hasIterated) {
            this.iterate();
            this.hasIterated = true;
        }

    }

    protected abstract void set(int var1, int var2);

    public void doBool() {
        this.set(BasicTypeSize.getTBooleanSize(), BasicType.getTBoolean());
    }

    public void doChar() {
        this.set(BasicTypeSize.getTCharSize(), BasicType.getTChar());
    }

    public void doFloat() {
        this.set(BasicTypeSize.getTFloatSize(), BasicType.getTFloat());
    }

    public void doDouble() {
        this.set(BasicTypeSize.getTDoubleSize(), BasicType.getTDouble());
    }

    public void doByte() {
        this.set(BasicTypeSize.getTByteSize(), BasicType.getTByte());
    }

    public void doShort() {
        this.set(BasicTypeSize.getTShortSize(), BasicType.getTShort());
    }

    public void doInt() {
        this.set(BasicTypeSize.getTIntSize(), BasicType.getTInt());
    }

    public void doLong() {
        this.set(BasicTypeSize.getTLongSize(), BasicType.getTLong());
    }

    public void doVoid() {
        this.set(BasicTypeSize.getTVoidSize(), BasicType.getTVoid());
    }

    public void doObject(int begin, int end) {
        this.set(BasicTypeSize.getTObjectSize(), BasicType.getTObject());
    }

    public void doArray(int begin, int end) {
        this.set(BasicTypeSize.getTArraySize(), BasicType.getTArray());
    }

    public int size() {
        this.lazyIterate();
        return this.size;
    }

    public int type() {
        this.lazyIterate();
        return this.type;
    }
}
