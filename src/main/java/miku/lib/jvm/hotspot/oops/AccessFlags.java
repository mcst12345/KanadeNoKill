package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

import java.io.PrintStream;

public class AccessFlags extends VMObject {
    private static final long _flags_offset;

    static {
        Type type = JVM.type("AccessFlags");
        _flags_offset = type.offset("_flags");
    }

    private int flags;

    public AccessFlags(long address) {
        super(address);
        flags = unsafe.getInt(address + _flags_offset);
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
        unsafe.putInt(getAddress() + _flags_offset, flags);
    }

    public boolean isPublic() {
        return (this.flags & 1) != 0;
    }

    public boolean isPrivate() {
        return (this.flags & 2) != 0;
    }

    public boolean isProtected() {
        return (this.flags & 4) != 0;
    }

    public boolean isStatic() {
        return (this.flags & 8) != 0;
    }

    public boolean isFinal() {
        return (this.flags & 16) != 0;
    }

    public boolean isSynchronized() {
        return (this.flags & 32) != 0;
    }

    public boolean isSuper() {
        return (this.flags & 32) != 0;
    }

    public boolean isVolatile() {
        return (this.flags & 64) != 0;
    }

    public boolean isBridge() {
        return (this.flags & 64) != 0;
    }

    public boolean isTransient() {
        return (this.flags & 128) != 0;
    }

    public boolean isVarArgs() {
        return (this.flags & 128) != 0;
    }

    public boolean isNative() {
        return (this.flags & 256) != 0;
    }

    public boolean isEnum() {
        return (this.flags & 16384) != 0;
    }

    public boolean isAnnotation() {
        return (this.flags & 8192) != 0;
    }

    public boolean isInterface() {
        return (this.flags & 512) != 0;
    }

    public boolean isAbstract() {
        return (this.flags & 1024) != 0;
    }

    public boolean isStrict() {
        return (this.flags & 2048) != 0;
    }

    public boolean isSynthetic() {
        return (this.flags & 4096) != 0;
    }

    public long getValue() {
        return this.flags;
    }

    public boolean isMonitorMatching() {
        return (this.flags & 268435456) != 0;
    }

    public boolean hasMonitorBytecodes() {
        return (this.flags & 536870912) != 0;
    }

    public boolean hasLoops() {
        return (this.flags & 1073741824) != 0;
    }

    public boolean loopsFlagInit() {
        return (this.flags & -2147483648) != 0;
    }

    public boolean queuedForCompilation() {
        return (this.flags & 16777216) != 0;
    }

    public boolean isNotOsrCompilable() {
        return (this.flags & 134217728) != 0;
    }

    public boolean hasLineNumberTable() {
        return (this.flags & 1048576) != 0;
    }

    public boolean hasCheckedExceptions() {
        return (this.flags & 4194304) != 0;
    }

    public boolean hasJsrs() {
        return (this.flags & 8388608) != 0;
    }

    public boolean isObsolete() {
        return (this.flags & 65536) != 0;
    }

    public boolean hasMirandaMethods() {
        return (this.flags & 268435456) != 0;
    }

    public boolean hasVanillaConstructor() {
        return (this.flags & 536870912) != 0;
    }

    public boolean hasFinalizer() {
        return (this.flags & 1073741824) != 0;
    }

    public boolean isCloneable() {
        return (this.flags & -2147483648) != 0;
    }

    public boolean hasLocalVariableTable() {
        return (this.flags & 2097152) != 0;
    }

    public boolean fieldAccessWatched() {
        return (this.flags & 8192) != 0;
    }

    public boolean fieldModificationWatched() {
        return (this.flags & 32768) != 0;
    }

    public boolean fieldHasGenericSignature() {
        return (this.flags & 2048) != 0;
    }

    public void printOn(PrintStream tty) {
        if (this.isPublic()) {
            tty.print("public ");
        }

        if (this.isPrivate()) {
            tty.print("private ");
        }

        if (this.isProtected()) {
            tty.print("protected ");
        }

        if (this.isStatic()) {
            tty.print("static ");
        }

        if (this.isFinal()) {
            tty.print("final ");
        }

        if (this.isSynchronized()) {
            tty.print("synchronized ");
        }

        if (this.isVolatile()) {
            tty.print("volatile ");
        }

        if (this.isBridge()) {
            tty.print("bridge ");
        }

        if (this.isTransient()) {
            tty.print("transient ");
        }

        if (this.isVarArgs()) {
            tty.print("varargs ");
        }

        if (this.isNative()) {
            tty.print("native ");
        }

        if (this.isEnum()) {
            tty.print("enum ");
        }

        if (this.isInterface()) {
            tty.print("interface ");
        }

        if (this.isAbstract()) {
            tty.print("abstract ");
        }

        if (this.isStrict()) {
            tty.print("strict ");
        }

        if (this.isSynthetic()) {
            tty.print("synthetic ");
        }

    }

    public int getStandardFlags() {
        return this.flags & 32767;
    }
}
