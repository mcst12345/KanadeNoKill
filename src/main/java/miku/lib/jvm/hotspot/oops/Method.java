package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.utilities.U1Array;
import one.helfy.JVM;
import one.helfy.Type;

//Method extends Metadata @ 88
//  ConstMethod* _constMethod @ 8
//  MethodData* _method_data @ 16
//  MethodCounters* _method_counters @ 24
//  AccessFlags _access_flags @ 32
//  int _vtable_index @ 36
//  u2 _method_size @ 40
//  u1 _intrinsic_id @ 42
//  address _i2i_entry @ 48
//  AdapterHandlerEntry* _adapter @ 56
//  address _from_compiled_entry @ 64
//  nmethod* _code @ 72
//  address _from_interpreted_entry @ 80

public class Method extends Metadata {

    private static final long _constMethod_offset;
    private static final long _method_data_offset;
    private static final long _method_counters_offset;
    private static final long _access_flags_offset;
    private static final long _vtable_index_offset;
    private static final long _method_size_offset;
    private static final long _intrinsic_id_offset;
    private static final long _i2i_entry_offset;
    private static final long _adapter_offset;
    private static final long _from_compiled_entry_offset;
    private static final long _code_offset;
    private static final long _from_interpreted_entry_offset;

    private static final long SIZE;

    static {
        Type type = JVM.type("Method");
        _constMethod_offset = type.offset("_constMethod");
        _method_data_offset = type.offset("_method_data");
        _method_counters_offset = type.offset("_method_counters");
        _access_flags_offset = type.offset("_access_flags");
        _vtable_index_offset = type.offset("_vtable_index");
        _method_size_offset = type.offset("_method_size");
        _intrinsic_id_offset = type.offset("_intrinsic_id");
        _i2i_entry_offset = type.offset("_i2i_entry");
        _adapter_offset = type.offset("_adapter");
        _from_compiled_entry_offset = type.offset("_from_compiled_entry");
        _code_offset = type.offset("_code");
        _from_interpreted_entry_offset = type.offset("_from_interpreted_entry");
        SIZE = type.size;
    }

    private static final Symbol objectInitializerName;
    private static final Symbol classInitializerName;

    static {
        objectInitializerName = VM.getSymbolTable().probe("<init>");
        classInitializerName = VM.getSymbolTable().probe("<clinit>");
    }

    public short getCodeSize() {
        return this.getConstMethod().getCodeSize();
    }

    public Method(long address) {
        super(address);
    }

    public Symbol getName() {
        return getConstants().getSymbolAt(getNameIndex());
    }

    public boolean hasStackMapTable() {
        return getConstMethod().hasStackMapTable();
    }

    public U1Array getStackMapData() {
        return getConstMethod().getStackMapData();
    }

    private static Symbol objectInitializerName() {
        return objectInitializerName;
    }

    private static Symbol classInitializerName() {
        return classInitializerName;
    }

    public ConstMethod getConstMethod() {
        return new ConstMethod(unsafe.getAddress(getAddress() + _constMethod_offset));
    }

    public int getNativeIntArg(int bci) {
        return getConstMethod().getNativeIntArg(bci);
    }


    public short getNameIndex() {
        return getConstMethod().getNameIndex();
    }

    public short getSignatureIndex() {
        return getConstMethod().getSignatureIndex();
    }

    public AccessFlags getAccessFlags() {
        return new AccessFlags(getAddress() + _access_flags_offset);
    }

    public long getGenericSignatureIndex() {
        return getConstMethod().getGenericSignatureIndex();
    }

    public ConstantPool getConstants() {
        return getConstMethod().getConstants();
    }

    public Symbol getSignature() {
        return this.getConstants().getSymbolAt(this.getSignatureIndex());
    }

    public boolean isStatic() {
        return this.getAccessFlags().isStatic();
    }

    public short getSizeOfParameters() {
        return this.getConstMethod().getSizeOfParameters();
    }

    public Symbol getGenericSignature() {
        long index = getGenericSignatureIndex();
        return index != 0L ? this.getConstants().getSymbolAt(index) : null;
    }

    public short getNativeShortArg(int bci) {
        return getConstMethod().getNativeShortArg(bci);
    }

    public byte[] getByteCode() {
        return getConstMethod().getByteCode();
    }

    public int getBytecodeOrBPAt(int bci) {
        return this.getConstMethod().getBytecodeOrBPAt(bci);
    }

    public int getBytecodeIntArg(int bci) {
        return this.getConstMethod().getBytecodeIntArg(bci);
    }

    public byte getBytecodeByteArg(int bci) {
        return this.getConstMethod().getBytecodeByteArg(bci);
    }

    public short getBytecodeShortArg(int bci) {
        return this.getConstMethod().getBytecodeShortArg(bci);
    }

    public short getMaxStack() {
        return getConstMethod().getMaxStack();
    }

    public short getMaxLocals() {
        return getConstMethod().getMaxLocals();
    }

    public boolean hasCheckedExceptions() {
        return this.getConstMethod().hasCheckedExceptions();
    }

    public boolean hasExceptionTable() {
        return this.getConstMethod().hasExceptionTable();
    }

    public boolean hasLineNumberTable() {
        return this.getConstMethod().hasLineNumberTable();
    }

    public LineNumberTableElement[] getLineNumberTable() {
        return this.getConstMethod().getLineNumberTable();
    }

    public boolean hasLocalVariableTable() {
        return this.getConstMethod().hasLocalVariableTable();
    }

    public LocalVariableTableElement[] getLocalVariableTable() {
        return this.getConstMethod().getLocalVariableTable();
    }

    public CheckedExceptionElement[] getCheckedExceptions() {
        return this.getConstMethod().getCheckedExceptions();
    }

    public boolean isNative() {
        return this.getAccessFlags().isNative();
    }

    public ExceptionTableElement[] getExceptionTable() {
        return this.getConstMethod().getExceptionTable();
    }

    public InstanceKlass getMethodHolder() {
        return getConstants().getPoolHolder();
    }

    public int getOrigBytecodeAt(int bci) {
        BreakpointInfo bp;
        for (bp = this.getMethodHolder().getBreakpoints(); bp != null; bp = bp.getNext()) {
            if (bp.match(this, bci)) {
                return bp.getOrigBytecode();
            }
        }

        System.err.println("Requested bci " + bci);

        while (bp != null) {
            System.err.println("Breakpoint at bci " + bp.getBCI() + ", bytecode " + bp.getOrigBytecode());
            bp = bp.getNext();
        }

        throw new IllegalStateException("Should not reach here");
    }

    public long getI2IEntry() {
        return unsafe.getAddress(getAddress() + _i2i_entry_offset);
    }


    public void setI2IEntry(long i2i) {
        unsafe.putAddress(getAddress() + _i2i_entry_offset, i2i);
    }

    public long getFromCompiledEntry() {
        return unsafe.getAddress(getAddress() + _from_compiled_entry_offset);
    }


    public void setFromCompiledEntry(long compiled) {
        unsafe.putAddress(getAddress() + _from_compiled_entry_offset, compiled);
    }

    public long getFromInterpretedEntry() {
        return unsafe.getAddress(getAddress() + _from_interpreted_entry_offset);
    }


    public void setFromInterpretedEntry(long interpreted) {
        unsafe.putAddress(getAddress() + _from_interpreted_entry_offset, interpreted);
    }

    public long getCode() {
        return unsafe.getAddress(getAddress() + SIZE);
    }


    public void setCode(long neo) {
        unsafe.putAddress(getAddress() + SIZE, neo);
    }
}
