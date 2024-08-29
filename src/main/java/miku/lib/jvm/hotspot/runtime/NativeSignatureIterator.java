package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Method;

public abstract class NativeSignatureIterator extends SignatureIterator {
    private Method method;
    private int offset;
    private int prepended;
    private int jni_offset;

    public void doBool() {
        this.passInt();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doChar() {
        this.passInt();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doFloat() {
        if (VM.isLP64) {
            this.passFloat();
        } else {
            this.passInt();
        }

        ++this.jni_offset;
        ++this.offset;
    }

    public void doDouble() {
        if (VM.isLP64) {
            this.passDouble();
            ++this.jni_offset;
            this.offset += 2;
        } else {
            this.passDouble();
            this.jni_offset += 2;
            this.offset += 2;
        }

    }

    public void doByte() {
        this.passInt();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doShort() {
        this.passInt();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doInt() {
        this.passInt();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doLong() {
        if (VM.isLP64) {
            this.passLong();
            ++this.jni_offset;
            this.offset += 2;
        } else {
            this.passLong();
            this.jni_offset += 2;
            this.offset += 2;
        }

    }

    public void doVoid() {
        throw new RuntimeException("should not reach here");
    }

    public void doObject(int begin, int end) {
        this.passObject();
        ++this.jni_offset;
        ++this.offset;
    }

    public void doArray(int begin, int end) {
        this.passObject();
        ++this.jni_offset;
        ++this.offset;
    }

    public Method method() {
        return this.method;
    }

    public int offset() {
        return this.offset;
    }

    public int jniOffset() {
        return this.jni_offset + this.prepended;
    }

    public boolean isStatic() {
        return this.method.isStatic();
    }

    public abstract void passInt();

    public abstract void passLong();

    public abstract void passObject();

    public abstract void passFloat();

    public abstract void passDouble();

    public NativeSignatureIterator(Method method) {
        super(method.getSignature());
        this.method = method;
        this.offset = 0;
        this.jni_offset = 0;
        int JNIEnv_words = 1;
        int mirror_words = 1;
        this.prepended = !this.isStatic() ? JNIEnv_words : JNIEnv_words + mirror_words;
    }

    public void iterate() {
        if (!this.isStatic()) {
            this.passObject();
            ++this.jni_offset;
            ++this.offset;
        }

        this.iterateParameters();
    }
}
