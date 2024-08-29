package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.ClassConstants;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.utilities.ConstantTag;
import miku.lib.jvm.hotspot.utilities.U1Array;
import miku.lib.jvm.hotspot.utilities.U2Array;
import one.helfy.JVM;
import one.helfy.Type;

public class ConstantPool extends Metadata implements ClassConstants {
    private static final long tags_offset;
    private static final long operands_offset;
    private static final long cache_offset;
    private static final long poolHolder_offset;
    private static final long length_offset;
    private static final long _resolved_references_offset;
    private static final long _reference_map_offset;
    private static final long elementSize;
    private static final long headerSize;

    private static final int INDY_BSM_OFFSET;
    private static final int INDY_ARGC_OFFSET;
    private static final int INDY_ARGV_OFFSET;

    static {
        Type type = JVM.type("ConstantPool");
        headerSize = type.size;
        tags_offset = type.offset("_tags");
        operands_offset = type.offset("_operands");
        cache_offset = type.offset("_cache");
        poolHolder_offset = type.offset("_pool_holder");
        length_offset = type.offset("_length");
        _resolved_references_offset = type.offset("_resolved_references");
        _reference_map_offset = type.offset("_reference_map");
        elementSize = VM.oopSize;

        INDY_BSM_OFFSET = JVM.intConstant("ConstantPool::_indy_bsm_offset");
        INDY_ARGC_OFFSET = JVM.intConstant("ConstantPool::_indy_argc_offset");
        INDY_ARGV_OFFSET = JVM.intConstant("ConstantPool::_indy_argv_offset");
    }

    public ConstantPool(long address) {
        super(address);

    }

    public U1Array getTags() {
        return new U1Array(unsafe.getAddress(getAddress() + tags_offset));
    }

    public U2Array getOperands() {
        long address = unsafe.getAddress(getAddress() + operands_offset);
        return address != 0 ? new U2Array(address) : null;
    }

    public ConstantPoolCache getCache() {
        long address = unsafe.getAddress(getAddress() + cache_offset);
        return address != 0 ? new ConstantPoolCache(address) : null;
    }

    public short[] getBootstrapSpecifierAt(int i) {
        if (!(getTagAt(i).isInvokeDynamic() || getTagAt(i).isDynamicConstant())) {
            throw new RuntimeException("Corrupted constant pool");
        }
        int bsmSpec = extractLowShortFromInt(this.getIntAt(i));
        return getBootstrapMethodAt(bsmSpec);
    }

    public short[] getBootstrapMethodAt(int bsmIndex) {
        U2Array operands = getOperands();
        if (operands == null) return null;  // safety first
        int basePos = getOperandOffsetAt(operands, bsmIndex);
        int argv = basePos + INDY_ARGV_OFFSET;
        int argc = operands.at(basePos + INDY_ARGC_OFFSET);
        int endPos = argv + argc;
        short[] values = new short[endPos - basePos];
        for (int j = 0; j < values.length; j++) {
            values[j] = operands.at(basePos + j);
        }
        return values;
    }

    public int getBootstrapMethodArgsCount(int bsmIndex) {
        U2Array operands = getOperands();
        if (operands == null) {
            throw new RuntimeException("Operands is not present");
        }
        int bsmOffset = getOperandOffsetAt(operands, bsmIndex);
        return operands.at(bsmOffset + INDY_ARGC_OFFSET);
    }

    public int getBootstrapMethodsCount() {
        U2Array operands = getOperands();
        int count = 0;
        if (operands != null) {
            count = getOperandOffsetAt(operands, 0) / 2;
        }
        return count;
    }

    public int getNameAndTypeRefIndexAt(int index) {
        return this.implNameAndTypeRefIndexAt(index, false);
    }

    private int getOperandOffsetAt(U2Array operands, int bsmIndex) {
        return VM.buildIntFromShorts(operands.at(bsmIndex * 2),
                operands.at(bsmIndex * 2 + 1));
    }

    public InstanceKlass getPoolHolder() {
        return (InstanceKlass) Klass.getKlass(unsafe.getAddress(getAddress() + poolHolder_offset));
    }

    public int getLength() {
        return unsafe.getInt(getAddress() + length_offset);
    }

    public Oop getResolvedReferences() {
        long address = unsafe.getAddress(getAddress() + _resolved_references_offset);
        if (address != 0) {
            return new Oop(address);
        } else {
            return null;
        }
    }

    public U2Array referenceMap() {
        return new U2Array(unsafe.getAddress(getAddress() + _reference_map_offset));
    }

    public int objectToCPIndex(int index) {
        return this.referenceMap().at(index);
    }

    private long indexOffset(long index) {
        if (!(index >= 0L && index < (long) this.getLength())) {
            throw new IllegalArgumentException("invalid cp index " + index + " " + this.getLength());
        }

        return index * elementSize + headerSize;
    }

    public ConstantTag getTagAt(long index) {
        return new ConstantTag(this.getTags().at((int) index));
    }

    public CPSlot getSlotAt(long index) {
        return new CPSlot(this.getAddressAtRaw(index));
    }

    public long getAddressAtRaw(long index) {
        return unsafe.getAddress(getAddress() + this.indexOffset(index));
    }

    public Symbol getSymbolAt(long index) {
        if (this.getAddressAtRaw(index) == 0) {
            return null;
        }
        return new Symbol(this.getAddressAtRaw(index));
    }

    public int getIntAt(long index) {
        return unsafe.getInt(getAddress() + this.indexOffset(index));
    }

    public float getFloatAt(long index) {
        return unsafe.getFloat(getAddress() + this.indexOffset(index));
    }

    public long getLongAt(long index) {
        int oneHalf = unsafe.getInt(getAddress() + this.indexOffset(index + 1L));
        int otherHalf = unsafe.getInt(getAddress() + this.indexOffset(index));
        return VM.buildLongFromIntsPD(oneHalf, otherHalf);
    }

    public double getDoubleAt(long index) {
        return Double.longBitsToDouble(this.getLongAt(index));
    }

    public int getFieldOrMethodAt(int which) {
        int i;
        ConstantPoolCache cache = this.getCache();
        if (cache == null) {
            i = which;
        } else {
            i = cache.getEntryAt('\uffff' & which).getConstantPoolIndex();
        }

        if (!(this.getTagAt(i).isFieldOrMethod())) {
            throw new IllegalStateException("Corrupted constant pool");
        }

        return this.getIntAt(i);
    }

    public int[] getNameAndTypeAt(int which) {
        if (!(this.getTagAt(which).isNameAndType())) {
            throw new IllegalStateException("Corrupted constant pool: " + which + " " + this.getTagAt(which));
        }

        int i = this.getIntAt(which);
        return new int[]{extractLowShortFromInt(i), extractHighShortFromInt(i)};
    }

    private static int extractLowShortFromInt(int val) {
        return val & '\uffff';
    }

    private static int extractHighShortFromInt(int val) {
        return val >> 16 & '\uffff';
    }

    public Symbol getNameRefAt(int which) {
        return this.implGetNameRefAt(which, false);
    }

    public Symbol uncachedGetNameRefAt(int which) {
        return this.implGetNameRefAt(which, true);
    }

    private Symbol implGetNameRefAt(int which, boolean uncached) {
        int signatureIndex = this.getNameRefIndexAt(this.implNameAndTypeRefIndexAt(which, uncached));
        return this.getSymbolAt(signatureIndex);
    }

    public Symbol getSignatureRefAt(int which) {
        return this.implGetSignatureRefAt(which, false);
    }

    public Symbol uncachedGetSignatureRefAt(int which) {
        return this.implGetSignatureRefAt(which, true);
    }

    private Symbol implGetSignatureRefAt(int which, boolean uncached) {
        int signatureIndex = this.getSignatureRefIndexAt(this.implNameAndTypeRefIndexAt(which, uncached));
        return this.getSymbolAt(signatureIndex);
    }

    public static boolean isInvokedynamicIndex(int i) {
        return i < 0;
    }

    public static int decodeInvokedynamicIndex(int i) {
        if (!isInvokedynamicIndex(i)) {
            throw new RuntimeException();
        }
        return ~i;
    }

    public int invokedynamicCPCacheIndex(int index) {
        if (!isInvokedynamicIndex(index)) {
            throw new IllegalStateException("should be a invokedynamic index");
        }
        int rawIndex = decodeInvokedynamicIndex(index);
        return this.referenceMap().at(rawIndex);
    }

    ConstantPoolCacheEntry invokedynamicCPCacheEntryAt(int index) {
        int cpCacheIndex = this.invokedynamicCPCacheIndex(index);
        return this.getCache().getEntryAt(cpCacheIndex);
    }

    private int implNameAndTypeRefIndexAt(int which, boolean uncached) {
        int i = which;
        int refIndex;
        if (!uncached && this.getCache() != null) {
            if (isInvokedynamicIndex(which)) {
                refIndex = this.invokedynamicCPCacheEntryAt(which).getConstantPoolIndex();
                refIndex = this.invokeDynamicNameAndTypeRefIndexAt(refIndex);
                if (!(this.getTagAt((long) refIndex).isNameAndType())) {
                    throw new RuntimeException();
                }
                return refIndex;
            }

            i = this.remapInstructionOperandFromCache(which);
        } else if (this.getTagAt((long) which).isInvokeDynamic()) {
            refIndex = this.invokeDynamicNameAndTypeRefIndexAt(which);
            if (!(this.getTagAt((long) refIndex).isNameAndType())) {
                throw new RuntimeException();
            }
            return refIndex;
        }

        refIndex = this.getIntAt((long) i);
        return extractHighShortFromInt(refIndex);
    }

    private int remapInstructionOperandFromCache(int operand) {
        int cpc_index = operand;
        int member_index = this.getCache().getEntryAt(cpc_index).getConstantPoolIndex();
        return member_index;
    }


    int invokeDynamicNameAndTypeRefIndexAt(int which) {
        return extractHighShortFromInt(this.getIntAt(which));
    }

    public Klass getKlassAt(int which) {
        return !this.getTagAt(which).isKlass() ? null : (Klass) Metadata.instantiateWrapperFor(this.getAddressAtRaw(which));
    }

    public Symbol getKlassNameAt(int which) {
        CPSlot entry = this.getSlotAt((long) which);
        return entry.isResolved() ? entry.getKlass().getSymbol() : entry.getSymbol();
    }

    public Symbol getUnresolvedStringAt(int which) {
        return this.getSymbolAt((long) which);
    }

    public int getNameRefIndexAt(int index) {
        int[] refIndex = this.getNameAndTypeAt(index);
        int i = refIndex[0];
        return i;
    }

    public int getSignatureRefIndexAt(int index) {
        int[] refIndex = this.getNameAndTypeAt(index);
        return refIndex[1];
    }

    public int getMethodHandleRefKindAt(int i) {
        return extractLowShortFromInt(this.getIntAt((long) i));
    }

    public int getMethodTypeIndexAt(int i) {
        return this.getIntAt(i);
    }

    public int getMethodHandleIndexAt(int i) {
        return extractHighShortFromInt(this.getIntAt((long) i));
    }

    public Field getFieldRefAt(int which) {
        InstanceKlass klass = this.getFieldOrMethodKlassRefAt(which);
        if (klass == null) {
            return null;
        } else {
            Symbol name = this.getNameRefAt(which);
            Symbol sig = this.getSignatureRefAt(which);
            return klass.findField(name, sig);
        }
    }

    public InstanceKlass getFieldOrMethodKlassRefAt(int which) {
        int refIndex = this.getFieldOrMethodAt(which);
        int klassIndex = extractLowShortFromInt(refIndex);
        return (InstanceKlass) this.getKlassAt(klassIndex);
    }

    public Method getMethodRefAt(int which) {
        InstanceKlass klass = this.getFieldOrMethodKlassRefAt(which);
        if (klass == null) {
            return null;
        } else {
            Symbol name = this.getNameRefAt(which);
            Symbol sig = this.getSignatureRefAt(which);
            return klass.findMethod(name, sig);
        }
    }


    public static class CPSlot {
        private final long ptr;

        CPSlot(long ptr) {
            this.ptr = ptr;
        }

        CPSlot(miku.lib.jvm.hotspot.oops.Symbol sym) {
            this.ptr = sym.getAddress() | 1L;
        }

        public boolean isResolved() {
            return (this.ptr & 1L) == 0L;
        }

        public boolean isUnresolved() {
            return (this.ptr & 1L) == 1L;
        }

        public Symbol getSymbol() {
            if (!this.isUnresolved()) {
                throw new InternalError("not a symbol");
            } else {
                return new Symbol(ptr ^ 1L);
                //return Symbol.create(this.ptr.xorWithMask(1L));
            }
        }

        public Klass getKlass() {
            if (!this.isResolved()) {
                throw new InternalError("not klass");
            } else {
                return (Klass) Metadata.instantiateWrapperFor(this.ptr);
            }
        }
    }
}
