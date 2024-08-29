package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.runtime.BasicType;

public class ConstantTag {
    private static final int JVM_CONSTANT_Utf8 = 1;
    private static final int JVM_CONSTANT_Unicode = 2;
    private static final int JVM_CONSTANT_Integer = 3;
    private static final int JVM_CONSTANT_Float = 4;
    private static final int JVM_CONSTANT_Long = 5;
    private static final int JVM_CONSTANT_Double = 6;
    private static final int JVM_CONSTANT_Class = 7;
    private static final int JVM_CONSTANT_String = 8;
    private static final int JVM_CONSTANT_Fieldref = 9;
    private static final int JVM_CONSTANT_Methodref = 10;
    private static final int JVM_CONSTANT_InterfaceMethodref = 11;
    private static final int JVM_CONSTANT_NameAndType = 12;
    private static final int JVM_CONSTANT_MethodHandle = 15;
    private static final int JVM_CONSTANT_MethodType = 16;
    private static final int JVM_CONSTANT_Dynamic = 17;
    private static final int JVM_CONSTANT_InvokeDynamic = 18;
    private static final int JVM_CONSTANT_Invalid = 0;
    private static final int JVM_CONSTANT_UnresolvedClass = 100;
    private static final int JVM_CONSTANT_ClassIndex = 101;
    private static final int JVM_CONSTANT_StringIndex = 102;
    private static final int JVM_CONSTANT_UnresolvedClassInError = 103;
    private static final int JVM_CONSTANT_MethodHandleInError = 104;
    private static final int JVM_CONSTANT_MethodTypeInError = 105;
    private static int JVM_REF_getField = 1;
    private static int JVM_REF_getStatic = 2;
    private static int JVM_REF_putField = 3;
    private static int JVM_REF_putStatic = 4;
    private static int JVM_REF_invokeVirtual = 5;
    private static int JVM_REF_invokeStatic = 6;
    private static int JVM_REF_invokeSpecial = 7;
    private static int JVM_REF_newInvokeSpecial = 8;
    private static int JVM_REF_invokeInterface = 9;
    private byte tag;

    public ConstantTag(byte tag) {
        this.tag = tag;
    }

    public int value() {
        return this.tag;
    }

    public boolean isKlass() {
        return this.tag == 7;
    }

    public boolean isField() {
        return this.tag == 9;
    }

    public boolean isMethod() {
        return this.tag == 10;
    }

    public boolean isInterfaceMethod() {
        return this.tag == 11;
    }

    public boolean isString() {
        return this.tag == 8;
    }

    public boolean isInt() {
        return this.tag == 3;
    }

    public boolean isFloat() {
        return this.tag == 4;
    }

    public boolean isLong() {
        return this.tag == 5;
    }

    public boolean isDouble() {
        return this.tag == 6;
    }

    public boolean isNameAndType() {
        return this.tag == 12;
    }

    public boolean isUtf8() {
        return this.tag == 1;
    }

    public boolean isMethodHandle() {
        return this.tag == 15;
    }

    public boolean isMethodType() {
        return this.tag == 16;
    }

    public boolean isInvokeDynamic() {
        return this.tag == 18;
    }

    public boolean isInvalid() {
        return this.tag == 0;
    }

    public boolean isUnresolvedKlass() {
        return this.tag == 100 || this.tag == 103;
    }

    public boolean isUnresolveKlassInError() {
        return this.tag == 103;
    }

    public boolean isKlassIndex() {
        return this.tag == 101;
    }

    public boolean isStringIndex() {
        return this.tag == 102;
    }

    public boolean isKlassReference() {
        return this.isKlassIndex() || this.isUnresolvedKlass();
    }

    public boolean isFieldOrMethod() {
        return this.isField() || this.isMethod() || this.isInterfaceMethod();
    }

    public boolean isSymbol() {
        return this.isUtf8();
    }

    public boolean isDynamicConstant() {
        return tag == JVM_CONSTANT_Dynamic;
    }


    public BasicType basicType() {
        switch (this.tag) {
            case 3:
                return BasicType.T_INT;
            case 4:
                return BasicType.T_FLOAT;
            case 5:
                return BasicType.T_LONG;
            case 6:
                return BasicType.T_DOUBLE;
            case 7:
            case 8:
            case 15:
            case 16:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
                return BasicType.T_OBJECT;
            default:
                throw new InternalError("unexpected tag: " + this.tag);
        }
    }

    public String toString() {
        return "ConstantTag:" + Integer.toString(this.tag);
    }
}
