package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.classfile.ClassLoaderData;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.vmSymbols;
import miku.lib.jvm.hotspot.utilities.KlassArray;
import miku.lib.jvm.hotspot.utilities.MethodArray;
import miku.lib.jvm.hotspot.utilities.U2Array;
import one.helfy.JVM;
import one.helfy.Type;
import sun.jvm.hotspot.debugger.DebuggerException;
import sun.jvm.hotspot.utilities.Assert;

import java.util.ArrayList;
import java.util.List;

//InstanceKlass extends Klass @ 440
//  ClassLoaderData* _class_loader_data @ 144
//  Annotations* _annotations @ 200
//  Klass* _array_klasses @ 208
//  ConstantPool* _constants @ 216
//  Array<jushort>* _inner_classes @ 224
//  char* _source_debug_extension @ 232
//  int _nonstatic_field_size @ 248
//  int _static_field_size @ 252
//  u2 _generic_signature_index @ 256
//  u2 _source_file_name_index @ 258
//  u2 _static_oop_field_count @ 260
//  u2 _java_fields_count @ 262
//  int _nonstatic_oop_map_size @ 264
//  bool _is_marked_dependent @ 268
//  u2 _minor_version @ 274
//  u2 _major_version @ 276
//  Thread* _init_thread @ 280
//  int _vtable_len @ 288
//  int _itable_len @ 292
//  OopMapCache* _oop_map_cache @ 296
//  JNIid* _jni_ids @ 312
//  jmethodID* _methods_jmethod_ids @ 320
//  nmethodBucket* _dependencies @ 328
//  nmethod* _osr_nmethods_head @ 336
//  BreakpointInfo* _breakpoints @ 344
//  u2 _idnum_allocated_count @ 368
//  u1 _init_state @ 370
//  u1 _reference_type @ 371
//  Array<Method*>* _methods @ 384
//  Array<Method*>* _default_methods @ 392
//  Array<Klass*>* _local_interfaces @ 400
//  Array<Klass*>* _transitive_interfaces @ 408
//  Array<int>* _method_ordering @ 416
//  Array<int>* _default_vtable_indices @ 424
//  Array<u2>* _fields @ 432

public class InstanceKlass extends Klass {

    private static final int ACCESS_FLAGS_OFFSET;
    private static final int NAME_INDEX_OFFSET;
    private static final int SIGNATURE_INDEX_OFFSET;
    private static final int INITVAL_INDEX_OFFSET;
    private static final int LOW_OFFSET;
    private static final int HIGH_OFFSET;
    private static final int FIELD_SLOTS;
    private static final short FIELDINFO_TAG_SIZE;
    private static final short FIELDINFO_TAG_MASK;
    private static final short FIELDINFO_TAG_OFFSET;
    private static final int CLASS_STATE_ALLOCATED;
    private static final int CLASS_STATE_LOADED;
    private static final int CLASS_STATE_LINKED;
    private static final int CLASS_STATE_BEING_INITIALIZED;
    private static final int CLASS_STATE_FULLY_INITIALIZED;
    private static final int CLASS_STATE_INITIALIZATION_ERROR;

    private static final long _class_loader_data_offset;
    private static final long _fields_offset;
    private static final long _java_fields_count_offset;
    private static final long _methods_offset;
    private static final long _array_klasses_offset;
    private static final long _method_ordering_offset;
    private static final long _local_interfaces_offset;
    private static final long _transitive_interfaces_offset;
    private static final long _constants_offset;
    private static final long _source_debug_extension_offset;
    private static final long _inner_classes_offset;
    private static final long _source_file_name_index_offset;
    private static final long _nonstatic_field_size_offset;
    private static final long _static_field_size_offset;
    private static final long _static_oop_field_count_offset;
    private static final long _nonstatic_oop_map_size_offset;
    private static final long _is_marked_dependent_offset;
    private static final long _init_state_offset;
    private static final long _vtable_len_offset;
    private static final long _itable_len_offset;
    private static final long _breakpoints_offset;
    private static final long _generic_signature_index_offset;
    private static final long _major_version_offset;
    private static final long _minor_version_offset;

    private static final long headerSize;

    static {
        ACCESS_FLAGS_OFFSET = JVM.intConstant("FieldInfo::access_flags_offset");
        NAME_INDEX_OFFSET = JVM.intConstant("FieldInfo::name_index_offset");
        SIGNATURE_INDEX_OFFSET = JVM.intConstant("FieldInfo::signature_index_offset");
        INITVAL_INDEX_OFFSET = JVM.intConstant("FieldInfo::initval_index_offset");
        LOW_OFFSET = JVM.intConstant("FieldInfo::low_packed_offset");
        HIGH_OFFSET = JVM.intConstant("FieldInfo::high_packed_offset");
        FIELD_SLOTS = JVM.intConstant("FieldInfo::field_slots");
        FIELDINFO_TAG_SIZE = (short) JVM.intConstant("FIELDINFO_TAG_SIZE");
        FIELDINFO_TAG_MASK = (short) JVM.intConstant("FIELDINFO_TAG_MASK");
        FIELDINFO_TAG_OFFSET = (short) JVM.intConstant("FIELDINFO_TAG_OFFSET");
        CLASS_STATE_ALLOCATED = JVM.intConstant("InstanceKlass::allocated");
        CLASS_STATE_LOADED = JVM.intConstant("InstanceKlass::loaded");
        CLASS_STATE_LINKED = JVM.intConstant("InstanceKlass::linked");
        CLASS_STATE_BEING_INITIALIZED = JVM.intConstant("InstanceKlass::being_initialized");
        CLASS_STATE_FULLY_INITIALIZED = JVM.intConstant("InstanceKlass::fully_initialized");
        CLASS_STATE_INITIALIZATION_ERROR = JVM.intConstant("InstanceKlass::initialization_error");

        Type type = JVM.type("InstanceKlass");
        headerSize = Oop.alignObjectSize(type.size);
        _class_loader_data_offset = type.offset("_class_loader_data");
        _fields_offset = type.offset("_fields");
        _java_fields_count_offset = type.offset("_java_fields_count");
        _methods_offset = type.offset("_methods");
        _array_klasses_offset = type.offset("_array_klasses");
        _method_ordering_offset = type.offset("_method_ordering");
        _local_interfaces_offset = type.offset("_local_interfaces");
        _transitive_interfaces_offset = type.offset("_transitive_interfaces");
        _constants_offset = type.offset("_constants");
        _source_debug_extension_offset = type.offset("_source_debug_extension");
        _inner_classes_offset = type.offset("_inner_classes");
        _source_file_name_index_offset = type.offset("_source_file_name_index");
        _nonstatic_field_size_offset = type.offset("_nonstatic_field_size");
        _static_field_size_offset = type.offset("_static_field_size");
        _static_oop_field_count_offset = type.offset("_static_oop_field_count");
        _nonstatic_oop_map_size_offset = type.offset("_nonstatic_oop_map_size");
        _is_marked_dependent_offset = type.offset("_is_marked_dependent");
        _init_state_offset = type.offset("_init_state");
        _vtable_len_offset = type.offset("_vtable_len");
        _itable_len_offset = type.offset("_itable_len");
        _breakpoints_offset = type.offset("_breakpoints");
        _generic_signature_index_offset = type.offset("_generic_signature_index");
        _major_version_offset = type.offset("_major_version");
        _minor_version_offset = type.offset("_minor_version");

    }

    private ClassLoaderData _class_loader_data;
    private U2Array _fields;
    private short _java_fields_count;

    public InstanceKlass(long address) {
        super(address);
        Type type = JVM.type("InstanceKlass");
        _class_loader_data = new ClassLoaderData(unsafe.getAddress(address + _class_loader_data_offset));
        _fields = new U2Array(unsafe.getAddress(address + _fields_offset));
        _java_fields_count = unsafe.getShort(address + _java_fields_count_offset);
    }

    public InstanceKlass(Class<?> clazz) {
        this(JVM.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
    }

    public short getJavaFieldsCount() {
        return _java_fields_count;
    }

    public ClassLoaderData getClassLoaderData() {
        return _class_loader_data;
    }

    public U2Array getFields() {
        return _fields;
    }

    public U2Array getInnerClasses() {
        return new U2Array(unsafe.getAddress(getAddress() + _inner_classes_offset));
    }

    public int getNonstaticFieldSize() {
        return unsafe.getInt(getAddress() + _nonstatic_field_size_offset);
    }

    public int getFieldOffset(int index) {
        U2Array fields = this.getFields();
        short lo = fields.at(index * FIELD_SLOTS + LOW_OFFSET);
        short hi = fields.at(index * FIELD_SLOTS + HIGH_OFFSET);
        if ((lo & FIELDINFO_TAG_MASK) == FIELDINFO_TAG_OFFSET) {
            return VM.buildIntFromShorts(lo, hi) >> FIELDINFO_TAG_SIZE;
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public short getFieldAccessFlags(int index) {
        return this.getFields().at(index * FIELD_SLOTS + ACCESS_FLAGS_OFFSET);
    }

    public MethodArray getMethods() {
        return new MethodArray(unsafe.getAddress(getAddress() + _methods_offset));
    }

    public short getFieldNameIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        }
    }

    public BreakpointInfo getBreakpoints() {
        return new BreakpointInfo(unsafe.getAddress(getAddress() + _breakpoints_offset));
    }

    public Symbol getFieldName(int index) {
        int nameIndex = this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        return index < this.getJavaFieldsCount() ? this.getConstants().getSymbolAt(nameIndex) : vmSymbols.symbolAt(nameIndex);
    }

    public Method findMethod(Symbol name, Symbol sig) {
        return findMethod(this.getMethods(), name, sig);
    }

    private static Method findMethod(MethodArray methods, Symbol name, Symbol signature) {
        int len = methods.length();
        int l = 0;
        int h = len - 1;

        int mid;
        while (l <= h) {
            mid = l + h >> 1;
            Method m = methods.at(mid);
            int res = m.getName().fastCompare(name);
            if (res == 0) {
                if (m.getSignature().equals(signature)) {
                    return m;
                }

                int i;
                Method m1;
                for (i = mid - 1; i >= l; --i) {
                    m1 = methods.at(i);
                    if (!m1.getName().equals(name)) {
                        break;
                    }

                    if (m1.getSignature().equals(signature)) {
                        return m1;
                    }
                }

                for (i = mid + 1; i <= h; ++i) {
                    m1 = methods.at(i);
                    if (!m1.getName().equals(name)) {
                        break;
                    }

                    if (m1.getSignature().equals(signature)) {
                        return m1;
                    }
                }

                return null;
            }

            if (res < 0) {
                l = mid + 1;
            } else {
                h = mid - 1;
            }
        }

        return null;
    }

    public Field findInterfaceField(Symbol name, Symbol sig) {
        KlassArray interfaces = this.getLocalInterfaces();
        int n = interfaces.length();

        for (int i = 0; i < n; ++i) {
            InstanceKlass intf1 = (InstanceKlass) interfaces.getAt(i);

            Field f = intf1.findLocalField(name, sig);
            if (f != null) {

                return f;
            }

            f = intf1.findInterfaceField(name, sig);
            if (f != null) {
                return f;
            }
        }

        return null;
    }

    public Field findLocalField(Symbol name, Symbol sig) {
        int length = this.getJavaFieldsCount();

        for (int i = 0; i < length; ++i) {
            Symbol f_name = this.getFieldName(i);
            Symbol f_sig = this.getFieldSignature(i);
            if (name.equals(f_name) && sig.equals(f_sig)) {
                return this.newField(i);
            }
        }

        return null;
    }

    public Field findField(Symbol name, Symbol sig) {
        Field f = this.findLocalField(name, sig);
        if (f != null) {
            return f;
        } else {
            f = this.findInterfaceField(name, sig);
            if (f != null) {
                return f;
            } else {
                InstanceKlass supr = (InstanceKlass) this.getSuper();
                return supr != null ? supr.findField(name, sig) : null;
            }
        }
    }

    public ConstantPool getConstants() {
        return new ConstantPool(unsafe.getAddress(getAddress() + _constants_offset));
    }

    public int getAllFieldsCount() {
        int len = this.getFields().length();

        int allFieldsCount;
        for (allFieldsCount = 0; allFieldsCount * FIELD_SLOTS < len; ++allFieldsCount) {
            short flags = this.getFieldAccessFlags(allFieldsCount);
            if ((((long) flags) & 2048L) != 0L) {
                --len;
            }
        }

        return allFieldsCount;
    }

    public short getSourceFileNameIndex() {
        return unsafe.getShort(getAddress() + _source_file_name_index_offset);
    }

    public Symbol getSourceFileName() {
        System.out.println(unsafe.getShort(getAddress() + _source_file_name_index_offset));
        return this.getConstants().getSymbolAt(unsafe.getShort(getAddress() + _source_file_name_index_offset));
    }

    public short getFieldSignatureIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + SIGNATURE_INDEX_OFFSET);
        }
    }

    public Symbol getFieldSignature(int index) {
        int signatureIndex = this.getFields().at(index * FIELD_SLOTS + SIGNATURE_INDEX_OFFSET);
        return index < this.getJavaFieldsCount() ? this.getConstants().getSymbolAt((long) signatureIndex) : vmSymbols.symbolAt(signatureIndex);
    }

    public short getFieldGenericSignatureIndex(int index) {
        int allFieldsCount = this.getAllFieldsCount();
        int generic_signature_slot = allFieldsCount * FIELD_SLOTS;

        for (int i = 0; i < allFieldsCount; ++i) {
            short flags = this.getFieldAccessFlags(i);
            if (i == index) {
                if ((((long) flags) & 2048L) != 0L) {
                    return this.getFields().at(generic_signature_slot);
                }

                return 0;
            }

            if ((((long) flags) & 2048L) != 0L) {
                ++generic_signature_slot;
            }
        }

        return 0;
    }

    public Symbol getFieldGenericSignature(int index) {
        short genericSignatureIndex = this.getFieldGenericSignatureIndex(index);
        return genericSignatureIndex != 0 ? this.getConstants().getSymbolAt(genericSignatureIndex) : null;
    }

    public short getFieldInitialValueIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + INITVAL_INDEX_OFFSET);
        }
    }

    public short getStaticOopFieldCount() {
        return unsafe.getShort(getAddress() + _static_oop_field_count_offset);
    }

    public int getNonstaticOopMapSize() {
        return unsafe.getInt(getAddress() + _nonstatic_oop_map_size_offset);
    }

    public boolean getIsMarkedDependent() {
        return unsafe.getByte(getAddress() + _is_marked_dependent_offset) != 0;
    }

    public int getVtableLen() {
        return unsafe.getInt(getAddress() + _vtable_len_offset);
    }

    public int getItableLen() {
        return unsafe.getInt(getAddress() + _itable_len_offset);
    }

    public short majorVersion() {
        return unsafe.getShort(getAddress() + _major_version_offset);
    }

    public short minorVersion() {
        return unsafe.getShort(getAddress() + _minor_version_offset);
    }

    public Symbol getGenericSignature() {
        short index = unsafe.getShort(getAddress() + _generic_signature_index_offset);
        return index != 0L ? this.getConstants().getSymbolAt(index) : null;
    }

    public long getSizeHelper() {
        int lh = this.getLayoutHelper();
        if (lh <= 0) {
            throw new RuntimeException("layout helper initialized for instance class");
        }
        return (long) lh / unsafe.addressSize();
    }

    public Field[] getStaticFields() {
        U2Array fields = this.getFields();
        int length = this.getJavaFieldsCount();
        ArrayList<Field> result = new ArrayList<>();

        for (int index = 0; index < length; ++index) {
            Field f = this.newField(index);
            if (f.isStatic()) {
                result.add(f);
            }
        }

        return result.toArray(new Field[0]);
    }

    private Field newField(int index) {
        FieldType type = new FieldType(getFieldSignature(index));
        if (type.isOop()) {
            //return new
        }
        return null;
    }

    public KlassArray getLocalInterfaces() {
        return new KlassArray(unsafe.getAddress(getAddress() + _local_interfaces_offset));
    }

    public KlassArray getTransitiveInterfaces() {
        return new KlassArray(unsafe.getAddress(getAddress() + _transitive_interfaces_offset));
    }
}
