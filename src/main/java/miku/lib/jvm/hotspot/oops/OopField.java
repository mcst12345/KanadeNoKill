package miku.lib.jvm.hotspot.oops;

public class OopField extends Field {
    OopField(FieldIdentifier id, long offset, boolean isVMField) {
        super(id, offset, isVMField);
    }

    OopField(InstanceKlass holder, int fieldIndex) {
        super(holder, fieldIndex);
    }


}
