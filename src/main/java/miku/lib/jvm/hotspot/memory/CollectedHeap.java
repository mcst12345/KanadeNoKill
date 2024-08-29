package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.gc_interface.CollectedHeapName;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

import java.io.PrintStream;

public class CollectedHeap extends VMObject {

    private static final long _reserved_offset;

    static {
        Type type = JVM.type("CollectedHeap");
        _reserved_offset = type.offset("_reserved");
    }

    public CollectedHeap(long address) {
        super(address);
    }

    public long start() {
        return this.reservedRegion().start();
    }

    public long capacity() {
        return 0L;
    }

    public long used() {
        return 0L;
    }

    public MemRegion reservedRegion() {
        return new MemRegion(getAddress() + _reserved_offset);
    }


    public boolean isIn(long a) {
        return this.isInReserved(a);
    }

    public boolean isInReserved(long a) {
        return this.reservedRegion().contains(a);
    }

    public CollectedHeapName kind() {
        return CollectedHeapName.ABSTRACT;
    }

    public void print() {
        this.printOn(System.out);
    }

    public void printOn(PrintStream tty) {
        MemRegion mr = this.reservedRegion();
        tty.println("unknown subtype of CollectedHeap @ " + this.getAddress() + " (" + mr.start() + "," + mr.end() + ")");
    }

}
