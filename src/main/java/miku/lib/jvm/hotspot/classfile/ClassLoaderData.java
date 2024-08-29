package miku.lib.jvm.hotspot.classfile;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.oops.Oop;
import one.helfy.JVM;
import one.helfy.Type;

public class ClassLoaderData extends VMObject {
    private Oop _class_loader;
    private long _next;

    public ClassLoaderData(long address) {
        super(address);
        Type type = JVM.type("ClassLoaderData");
        long offset = type.offset("_class_loader");
        _class_loader = new Oop(unsafe.getAddress(address + offset));
        offset = type.offset("_next");
        _next = unsafe.getAddress(address + offset);
    }

    public ClassLoaderData next() {
        return new ClassLoaderData(_next);
    }

    public Oop getClassLoader() {
        return _class_loader;
    }
}
