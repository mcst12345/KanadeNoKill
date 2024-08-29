package miku.lib.jvm.hotspot.oops;

public class Field {
    private long offset;
    private FieldIdentifier id;
    private boolean isVMField;
    private InstanceKlass holder;
    private FieldType fieldType;
    private Symbol signature;
    private Symbol genericSignature;
    private AccessFlags accessFlags;
    private int fieldIndex;

    Field(FieldIdentifier id, long offset, boolean isVMField) {
        this.offset = offset;
        this.id = id;
        this.isVMField = isVMField;
    }

    Field(InstanceKlass holder, int fieldIndex) {
        this.holder = holder;
        this.fieldIndex = fieldIndex;
    }

    public boolean isPublic() {
        return this.accessFlags.isPublic();
    }

    public boolean isPrivate() {
        return this.accessFlags.isPrivate();
    }

    public boolean isProtected() {
        return this.accessFlags.isProtected();
    }

    public boolean isPackagePrivate() {
        return !this.isPublic() && !this.isPrivate() && !this.isProtected();
    }

    public boolean isStatic() {
        return this.accessFlags.isStatic();
    }

    public boolean isFinal() {
        return this.accessFlags.isFinal();
    }

    public boolean isVolatile() {
        return this.accessFlags.isVolatile();
    }

    public boolean isTransient() {
        return this.accessFlags.isTransient();
    }

    public boolean isSynthetic() {
        return this.accessFlags.isSynthetic();
    }

    public boolean isEnumConstant() {
        return this.accessFlags.isEnum();
    }
}
