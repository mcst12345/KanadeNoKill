package miku.lib.jvm.hotspot.oops;

import me.xdark.shell.JVMUtil;
import miku.lib.jvm.hotspot.utilities.U1Array;
import miku.lib.utils.NumberTransformer;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//ConstMethod extends MetaspaceObj @ 48
//  uint64_t _fingerprint @ 0
//  ConstantPool* _constants @ 8
//  Array<u1>* _stackmap_data @ 16
//  int _constMethod_size @ 24
//  u2 _flags @ 28
//  u2 _code_size @ 32
//  u2 _name_index @ 34
//  u2 _signature_index @ 36
//  u2 _method_idnum @ 38
//  u2 _max_stack @ 40
//  u2 _max_locals @ 42
//  u2 _size_of_parameters @ 44

public class ConstMethod extends VMObject {
    private static final long _fingerprint_offset;
    private static final long _constants_offset;
    private static final long _stackmap_data_offset;
    private static final long _constMethod_size_offset;
    private static final long _flags_offset;
    private static final long _code_size_offset;
    private static final long _name_index_offset;
    private static final long _signature_index_offset;
    private static final long _method_idnum_offset;
    private static final long _max_stack_offset;
    private static final long _max_locals_offset;
    private static final long _size_of_parameters_offset;

    private static final long bytecodeOffset;

    static {
        Type type = JVM.type("ConstMethod");
        _fingerprint_offset = type.offset("_fingerprint");
        _constants_offset = type.offset("_constants");
        _stackmap_data_offset = type.offset("_stackmap_data");
        _constMethod_size_offset = type.offset("_constMethod_size");
        _flags_offset = type.offset("_flags");
        _code_size_offset = type.offset("_code_size");
        _name_index_offset = type.offset("_name_index");
        _signature_index_offset = type.offset("_signature_index");
        _method_idnum_offset = type.offset("_method_idnum");
        _max_stack_offset = type.offset("_max_stack");
        _max_locals_offset = type.offset("_max_locals");
        _size_of_parameters_offset = type.offset("_size_of_parameters");
        bytecodeOffset = type.size;
    }


    private static final int HAS_LINENUMBER_TABLE;
    private static final int HAS_CHECKED_EXCEPTIONS;
    private static final int HAS_LOCALVARIABLE_TABLE;
    private static final int HAS_EXCEPTION_TABLE;
    private static final int HAS_GENERIC_SIGNATURE;
    private static final int HAS_METHOD_ANNOTATIONS;
    private static final int HAS_PARAMETER_ANNOTATIONS;
    private static final int HAS_METHOD_PARAMETERS;
    private static final int HAS_DEFAULT_ANNOTATIONS;
    private static final int HAS_TYPE_ANNOTATIONS;

    static {
        HAS_LINENUMBER_TABLE = JVM.intConstant("ConstMethod::_has_linenumber_table");
        HAS_CHECKED_EXCEPTIONS = JVM.intConstant("ConstMethod::_has_checked_exceptions");
        HAS_LOCALVARIABLE_TABLE = JVM.intConstant("ConstMethod::_has_localvariable_table");
        HAS_EXCEPTION_TABLE = JVM.intConstant("ConstMethod::_has_exception_table");
        HAS_GENERIC_SIGNATURE = JVM.intConstant("ConstMethod::_has_generic_signature");
        HAS_METHOD_ANNOTATIONS = JVM.intConstant("ConstMethod::_has_method_annotations");
        HAS_PARAMETER_ANNOTATIONS = JVM.intConstant("ConstMethod::_has_parameter_annotations");
        HAS_METHOD_PARAMETERS = JVM.intConstant("ConstMethod::_has_method_parameters");
        HAS_DEFAULT_ANNOTATIONS = JVM.intConstant("ConstMethod::_has_default_annotations");
        HAS_TYPE_ANNOTATIONS = JVM.intConstant("ConstMethod::_has_type_annotations");
    }

    private static final long methodParametersElementSize;
    private static final long checkedExceptionElementSize;
    private static final long localVariableTableElementSize;
    private static final long exceptionTableElementSize;

    static {
        methodParametersElementSize = JVM.type("MethodParametersElement").size;
        checkedExceptionElementSize = JVM.type("CheckedExceptionElement").size;
        localVariableTableElementSize = JVM.type("LocalVariableTableElement").size;
        exceptionTableElementSize = JVM.type("ExceptionTableElement").size;
    }

    public ConstMethod(long address) {
        super(address);
    }

    public Method getMethod() {
        InstanceKlass ik = this.getConstants().getPoolHolder();
        return ik.getMethods().at(getIdNum());
    }

    public ConstantPool getConstants() {
        return new ConstantPool(unsafe.getAddress(getAddress() + _constants_offset));
    }

    public long getGenericSignatureIndex() {
        return this.hasGenericSignature() ? NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + this.offsetOfGenericSignatureIndex(), 2), true) : 0;
    }

    private long offsetOfGenericSignatureIndex() {
        return this.offsetOfLastU2Element();
    }

    private long offsetOfLastU2Element() {
        int offset = 0;
        if (this.hasMethodAnnotations()) {
            ++offset;
        }

        if (this.hasParameterAnnotations()) {
            ++offset;
        }

        if (this.hasTypeAnnotations()) {
            ++offset;
        }

        if (this.hasDefaultAnnotations()) {
            ++offset;
        }

        int wordSize = VM.oopSize;
        return (long) this.getSize() * wordSize - (long) offset * wordSize - 2;
    }

    public short getNativeShortArg(int bci) {
        int hi = getBytecodeOrBPAt(bci);
        int lo = getBytecodeOrBPAt(bci + 1);
        return (short) ((lo << 8) | hi);
    }

    public int getNativeIntArg(int bci) {
        int b4 = getBytecodeOrBPAt(bci);
        int b3 = getBytecodeOrBPAt(bci + 1);
        int b2 = getBytecodeOrBPAt(bci + 2);
        int b1 = getBytecodeOrBPAt(bci + 3);

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public int getSize() {
        return this.getConstMethodSize();
    }

    public short getIdNum() {
        return unsafe.getShort(getAddress() + _method_idnum_offset);
    }

    public short getNameIndex() {
        return unsafe.getShort(getAddress() + _name_index_offset);
    }

    public short getCodeSize() {
        return unsafe.getShort(getAddress() + _code_size_offset);
    }

    public int getConstMethodSize() {
        return unsafe.getInt(getAddress() + _constMethod_size_offset);
    }

    public short getFlags() {
        return unsafe.getShort(getAddress() + _flags_offset);
    }

    public short getSignatureIndex() {
        return unsafe.getShort(getAddress() + _signature_index_offset);
    }

    public short getMaxStack() {
        return unsafe.getShort(getAddress() + _max_stack_offset);
    }

    public short getMaxLocals() {
        return unsafe.getShort(getAddress() + _max_locals_offset);
    }

    public short getSizeOfParameters() {
        return unsafe.getShort(getAddress() + _size_of_parameters_offset);
    }

    public boolean hasCheckedExceptions() {
        return (this.getFlags() & HAS_CHECKED_EXCEPTIONS) != 0;
    }

    public byte[] getByteCode() {
        byte[] bc = new byte[(int) this.getCodeSize()];

        for (int i = 0; i < bc.length; ++i) {
            long offs = bytecodeOffset + (long) i;
            bc[i] = unsafe.getByte(getAddress() + offs);
        }

        return bc;
    }

    public CheckedExceptionElement[] getCheckedExceptions() {
        if (!this.hasCheckedExceptions()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        CheckedExceptionElement[] ret = new CheckedExceptionElement[this.getCheckedExceptionsLength()];
        long offset = this.offsetOfCheckedExceptions();

        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new CheckedExceptionElement(this.getAddress(), offset);
            offset += checkedExceptionElementSize;
        }

        return ret;
    }

    public boolean hasLineNumberTable() {
        return (this.getFlags() & HAS_LINENUMBER_TABLE) != 0;
    }

    public LineNumberTableElement[] getLineNumberTable() {
        if (!this.hasLineNumberTable()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        int len = this.getLineNumberTableLength();
        CompressedLineNumberReadStream stream = new CompressedLineNumberReadStream(this.getAddress(), (int) this.offsetOfCompressedLineNumberTable());
        LineNumberTableElement[] ret = new LineNumberTableElement[len];

        for (int idx = 0; idx < len; ++idx) {
            stream.readPair();
            ret[idx] = new LineNumberTableElement(stream.bci(), stream.line());
        }

        return ret;
    }

    private int getLineNumberTableLength() {
        int len = 0;
        if (this.hasLineNumberTable()) {
            for (CompressedLineNumberReadStream stream = new CompressedLineNumberReadStream(this.getAddress(), (int) this.offsetOfCompressedLineNumberTable()); stream.readPair(); ++len) {
            }
        }

        return len;
    }

    public boolean hasLocalVariableTable() {
        return (this.getFlags() & HAS_LOCALVARIABLE_TABLE) != 0;
    }

    public LocalVariableTableElement[] getLocalVariableTable() {
        if (!this.hasLocalVariableTable()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        LocalVariableTableElement[] ret = new LocalVariableTableElement[this.getLocalVariableTableLength()];
        long offset = this.offsetOfLocalVariableTable();

        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new LocalVariableTableElement(this.getAddress(), offset);
            offset += localVariableTableElementSize;
        }

        return ret;
    }

    private long offsetOfLocalVariableTable() {
        long offset = this.offsetOfLocalVariableTableLength();
        long length = this.getLocalVariableTableLength();
        if (length <= 0L) {
            throw new IllegalStateException("should only be called if table is present");
        }

        offset -= length * localVariableTableElementSize;
        return offset;
    }

    private int getLocalVariableTableLength() {
        return this.hasLocalVariableTable() ? (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + offsetOfLocalVariableTableLength(), 2), true) : 0;
    }

    private long offsetOfLocalVariableTableLength() {
        if (!this.hasLocalVariableTable()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        if (this.hasExceptionTable()) {
            return this.offsetOfExceptionTable() - 2L;
        } else if (this.hasCheckedExceptions()) {
            return this.offsetOfCheckedExceptions() - 2L;
        } else if (this.hasMethodParameters()) {
            return this.offsetOfMethodParameters() - 2L;
        } else {
            return this.hasGenericSignature() ? this.offsetOfLastU2Element() - 2L : this.offsetOfLastU2Element();
        }
    }

    private boolean isNative() {
        return this.getMethod().isNative();
    }

    private long offsetOfCodeEnd() {
        return bytecodeOffset + this.getCodeSize();
    }

    private long offsetOfCompressedLineNumberTable() {
        return this.offsetOfCodeEnd() + (this.isNative() ? 2L * unsafe.addressSize() : 0L);
    }

    public boolean hasExceptionTable() {
        return (this.getFlags() & HAS_EXCEPTION_TABLE) != 0;
    }

    public ExceptionTableElement[] getExceptionTable() {
        if (!this.hasExceptionTable()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        ExceptionTableElement[] ret = new ExceptionTableElement[this.getExceptionTableLength()];
        long offset = this.offsetOfExceptionTable();

        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new ExceptionTableElement(getAddress(), offset);
            offset += exceptionTableElementSize;
        }

        return ret;
    }

    private long offsetOfExceptionTable() {
        long offset = this.offsetOfExceptionTableLength();
        long length = this.getExceptionTableLength();
        if (length <= 0L) {
            throw new IllegalStateException("should only be called if table is present");
        }

        offset -= length * exceptionTableElementSize;
        return offset;
    }

    private int getExceptionTableLength() {
        return this.hasExceptionTable() ? (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + this.offsetOfExceptionTableLength(), 2), true) : 0;
    }

    private long offsetOfExceptionTableLength() {
        if (!this.hasExceptionTable()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        if (this.hasCheckedExceptions()) {
            return this.offsetOfCheckedExceptions() - 2L;
        } else if (this.hasMethodParameters()) {
            return this.offsetOfMethodParameters() - 2L;
        } else {
            return this.hasGenericSignature() ? this.offsetOfLastU2Element() - 2L : this.offsetOfLastU2Element();
        }
    }

    private long offsetOfCheckedExceptions() {
        long offset = this.offsetOfCheckedExceptionsLength();
        long length = this.getCheckedExceptionsLength();
        if (length <= 0L) {
            throw new IllegalStateException("should only be called if table is present");
        }

        offset -= length * checkedExceptionElementSize;
        return offset;
    }

    private int getCheckedExceptionsLength() {
        return this.hasCheckedExceptions() ? (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + this.offsetOfCheckedExceptionsLength(), 2), true) : 0;
    }

    private long offsetOfCheckedExceptionsLength() {
        if (this.hasMethodParameters()) {
            return this.offsetOfMethodParameters() - 2L;
        } else {
            return this.hasGenericSignature() ? this.offsetOfLastU2Element() - 2L : this.offsetOfLastU2Element();
        }
    }

    private int getMethodParametersLength() {
        return this.hasMethodParameters() ? (int) NumberTransformer.dataToCInteger(JVMUtil.getBytes(getAddress() + this.offsetOfMethodParametersLength(), 2), true) : 0;
    }

    private long offsetOfMethodParameters() {
        long offset = this.offsetOfMethodParametersLength();
        long length = this.getMethodParametersLength();
        if (length <= 0L) {
            throw new IllegalStateException("should only be called if method parameter information is present");
        }

        offset -= length * methodParametersElementSize;
        return offset;
    }

    public boolean hasStackMapTable() {
        return unsafe.getAddress(getAddress() + _stackmap_data_offset) != 0;
    }

    public U1Array getStackMapData() {
        long address = unsafe.getAddress(getAddress() + _stackmap_data_offset);
        return address != 0 ? new U1Array(address) : null;
    }

    public int getBytecodeOrBPAt(int bci) {
        return unsafe.getByte(getAddress() + bytecodeOffset + (long) bci) & 255;
    }

    public int getBytecodeIntArg(int bci) {
        int b4 = this.getBytecodeOrBPAt(bci);
        int b3 = this.getBytecodeOrBPAt(bci + 1);
        int b2 = this.getBytecodeOrBPAt(bci + 2);
        int b1 = this.getBytecodeOrBPAt(bci + 3);
        return b4 << 24 | b3 << 16 | b2 << 8 | b1;
    }

    public byte getBytecodeByteArg(int bci) {
        return (byte) this.getBytecodeOrBPAt(bci);
    }

    public short getBytecodeShortArg(int bci) {
        int hi = this.getBytecodeOrBPAt(bci);
        int lo = this.getBytecodeOrBPAt(bci + 1);
        return (short) (hi << 8 | lo);
    }

    private long offsetOfMethodParametersLength() {
        if (!this.hasMethodParameters()) {
            throw new IllegalStateException("should only be called if table is present");
        }

        return this.hasGenericSignature() ? this.offsetOfLastU2Element() - 2L : this.offsetOfLastU2Element();
    }

    private boolean hasMethodParameters() {
        return (this.getFlags() & HAS_METHOD_PARAMETERS) != 0;
    }

    private boolean hasGenericSignature() {
        return (this.getFlags() & HAS_GENERIC_SIGNATURE) != 0;
    }

    private boolean hasMethodAnnotations() {
        return (this.getFlags() & HAS_METHOD_ANNOTATIONS) != 0;
    }

    private boolean hasParameterAnnotations() {
        return (this.getFlags() & HAS_PARAMETER_ANNOTATIONS) != 0;
    }

    private boolean hasDefaultAnnotations() {
        return (this.getFlags() & HAS_DEFAULT_ANNOTATIONS) != 0;
    }

    private boolean hasTypeAnnotations() {
        return (this.getFlags() & HAS_TYPE_ANNOTATIONS) != 0;
    }

}
