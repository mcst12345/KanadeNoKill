package miku.lib.jvm.hotspot.code;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.runtime.VMObjectFactory;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;
import sun.jvm.hotspot.debugger.linux.LinuxAddress;

//StubQueue @ 48
//  address _stub_buffer @ 8
//  int _buffer_limit @ 20
//  int _queue_begin @ 24
//  int _queue_end @ 28
//  int _number_of_stubs @ 32

public class StubQueue extends VMObject {

    private static final long _stub_buffer_offset;
    private static final long _buffer_limit_offset;
    private static final long _queue_begin_offset;
    private static final long _queue_end_offset;
    private static final long _number_of_stubs_offset;

    static {
        Type type = JVM.type("StubQueue");
        _stub_buffer_offset = type.offset("_stub_buffer");
        _buffer_limit_offset = type.offset("_buffer_limit");
        _queue_begin_offset = type.offset("_queue_begin");
        _queue_end_offset = type.offset("_queue_end");
        _number_of_stubs_offset = type.offset("_number_of_stubs");
    }

    private Class<? extends VMObject> stubType;

    public StubQueue(long address, Class<? extends VMObject> stubType) {
        super(address);
        this.stubType = stubType;
    }


    public boolean contains(long pc) {
        if (pc == 0) {
            return false;
        } else {
            long offset = AddressCalculator.minus(pc, getStubBuffer());//pc.minus(this.getStubBuffer())
            return 0L <= offset && offset < this.getBufferLimit();
        }
    }

    public Stub getStubContaining(long pc) {
        if (this.contains(pc)) {
            int i = 0;

            for (Stub s = this.getFirst(); s != null; s = this.getNext(s)) {
                if (this.stubContains(s, pc)) {
                    return s;
                }
            }
        }

        return null;
    }

    public boolean stubContains(Stub s, long pc) {//s.codeBegin().lessThanOrEqual(pc)
        return AddressCalculator.lessThanOrEqual(s.codeBegin(), pc) && AddressCalculator.greaterThan(s.codeEnd(), pc);
    }

    public int getNumberOfStubs() {
        return unsafe.getInt(getAddress() + _number_of_stubs_offset);
    }

    public Stub getFirst() {
        return this.getNumberOfStubs() > 0 ? this.getStubAt(this.getQueueBegin()) : null;
    }

    public Stub getNext(Stub s) {
        long i = this.getIndexOf(s) + this.getStubSize(s);
        if (i == this.getBufferLimit()) {
            i = 0L;
        }

        return i == this.getQueueEnd() ? null : this.getStubAt(i);
    }

    public Stub getPrev(Stub s) {
        if (this.getIndexOf(s) == this.getQueueBegin()) {
            return null;
        } else {
            Stub temp = this.getFirst();

            Stub prev;
            for (prev = null; temp != null && this.getIndexOf(temp) != this.getIndexOf(s); temp = this.getNext(temp)) {
                prev = temp;
            }

            return prev;
        }
    }

    private int getQueueBegin() {
        return unsafe.getInt(getAddress() + _queue_begin_offset);
    }

    private int getQueueEnd() {
        return unsafe.getInt(getAddress() + _queue_end_offset);
    }

    private int getBufferLimit() {
        return unsafe.getInt(getAddress() + _buffer_limit_offset);
    }

    private long getStubBuffer() {
        return unsafe.getAddress(getAddress() + _stub_buffer_offset);
    }

    private Stub getStubAt(long offset) {//(Stub)VMObjectFactory.newObject(this.stubType, this.getStubBuffer().addOffsetTo(offset))
        return (Stub) VMObjectFactory.newObject(this.stubType, getStubBuffer() + offset);
    }

    private long getIndexOf(Stub s) {
        long i = s.getAddress() - this.getStubBuffer();
        return i;
    }

    private long getStubSize(Stub s) {
        return s.getSize();
    }

}
