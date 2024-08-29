package miku.lib.jvm.hotspot.oops;

import miku.lib.utils.ObjectUtils;
import miku.lib.jvm.hotspot.runtime.ObjectSynchronizer;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public class Oop extends VMObject {

    private final Object object;
    private Klass klass;

    private static final int headerSize;

    static {
        Type type = JVM.type("oopDesc");
        headerSize = type.size;
    }

    public static int getHeaderSize() {
        return headerSize;
    }

    public Oop(long address) {
        super(address);
        object = JVM.Ptr2Obj.getFromPtr(address);
    }

    public static Oop getOop(Object obj) {
        return new Oop(ObjectUtils.location(obj));
    }

    public Mark getMark() {
        return new Mark(getAddress());
    }

    public long identityHash() {
        Mark mark = this.getMark();
        if (mark.isUnlocked() && !mark.hasNoHash()) {
            return (int) mark.hash();
        } else {
            return mark.isMarked() ? (long) ((int) mark.hash()) : this.slowIdentityHash();
        }
    }

    public long slowIdentityHash() {
        return ObjectSynchronizer.identityHashValueFor(this);
    }

    public Klass getKlass() {
        if (klass == null) {
            klass = Klass.getKlass(object.getClass());
        }
        return klass;
    }

    public void setKlass(Klass k) {
        klass = k;
        //unsafe.ensureClassInitialized(k.getMirror());
        unsafe.putIntVolatile(object, 8L, JVM.Ptr2Obj.narrowKlassAddress(k.getAddress()));
    }

    public Object getObject() {
        return object;
    }

    public static long alignObjectSize(long size) {
        return VM.alignUp(size, VM.minObjAlignmentInBytes);
    }

    public static long alignObjectOffset(long offset) {
        return VM.alignUp(offset, VM.bytesPerLong);
    }

    public boolean isInstance() {
        return false;
    }

    public boolean isInstanceRef() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isObjArray() {
        return false;
    }

    public boolean isTypeArray() {
        return false;
    }

    public boolean isThread() {
        return false;
    }
}
