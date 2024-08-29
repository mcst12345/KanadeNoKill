package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.ObjectMonitor;
import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.utilities.Bits;

public class Mark extends VMObject {
    private static final long ageBits;
    private static final long lockBits;
    private static final long biasedLockBits;
    private static final long maxHashBits;
    private static final long hashBits;
    private static final long lockShift;
    private static final long biasedLockShift;
    private static final long ageShift;
    private static final long hashShift;
    private static final long lockMask;
    private static final long lockMaskInPlace;
    private static final long biasedLockMask;
    private static final long biasedLockMaskInPlace;
    private static final long biasedLockBitInPlace;
    private static final long ageMask;
    private static final long ageMaskInPlace;
    private static final long hashMask;
    private static final long hashMaskInPlace;
    private static final long biasedLockAlignment;
    private static final long lockedValue;
    private static final long unlockedValue;
    private static final long monitorValue;
    private static final long markedValue;
    private static final long biasedLockPattern;
    private static final long noHash;
    private static final long noHashInPlace;
    private static final long noLockInPlace;
    private static final long maxAge;
    private static final long cmsShift;
    private static final long cmsMask;
    private static final long sizeShift;

    static {
        ageBits = jvm.longConstant("markOopDesc::age_bits");
        lockBits = jvm.longConstant("markOopDesc::lock_bits");
        biasedLockBits = jvm.longConstant("markOopDesc::biased_lock_bits");
        maxHashBits = jvm.longConstant("markOopDesc::max_hash_bits");
        hashBits = jvm.longConstant("markOopDesc::hash_bits");
        lockShift = jvm.longConstant("markOopDesc::lock_shift");
        biasedLockShift = jvm.longConstant("markOopDesc::biased_lock_shift");
        ageShift = jvm.longConstant("markOopDesc::age_shift");
        hashShift = jvm.longConstant("markOopDesc::hash_shift");
        lockMask = jvm.longConstant("markOopDesc::lock_mask");
        lockMaskInPlace = jvm.longConstant("markOopDesc::lock_mask_in_place");
        biasedLockMask = jvm.longConstant("markOopDesc::biased_lock_mask");
        biasedLockMaskInPlace = jvm.longConstant("markOopDesc::biased_lock_mask_in_place");
        biasedLockBitInPlace = jvm.longConstant("markOopDesc::biased_lock_bit_in_place");
        ageMask = jvm.longConstant("markOopDesc::age_mask");
        ageMaskInPlace = jvm.longConstant("markOopDesc::age_mask_in_place");
        hashMask = jvm.longConstant("markOopDesc::hash_mask");
        hashMaskInPlace = jvm.longConstant("markOopDesc::hash_mask_in_place");
        biasedLockAlignment = jvm.longConstant("markOopDesc::biased_lock_alignment");
        lockedValue = jvm.longConstant("markOopDesc::locked_value");
        unlockedValue = jvm.longConstant("markOopDesc::unlocked_value");
        monitorValue = jvm.longConstant("markOopDesc::monitor_value");
        markedValue = jvm.longConstant("markOopDesc::marked_value");
        biasedLockPattern = jvm.longConstant("markOopDesc::biased_lock_pattern");
        noHash = jvm.longConstant("markOopDesc::no_hash");
        noHashInPlace = jvm.longConstant("markOopDesc::no_hash_in_place");
        noLockInPlace = jvm.longConstant("markOopDesc::no_lock_in_place");
        maxAge = jvm.longConstant("markOopDesc::max_age");
        cmsShift = jvm.longConstant("markOopDesc::cms_shift");
        cmsMask = jvm.longConstant("markOopDesc::cms_mask");
        sizeShift = jvm.longConstant("markOopDesc::size_shift");
    }

    public Mark(long address) {
        super(address);
    }

    public long value() {
        return unsafe.getInt(getAddress() + jvm.type("oopDesc").offset("_mark"));
    }

    public boolean isCmsFreeChunk() {
        return this.isUnlocked() && (Bits.maskBitsLong(this.value() >> (int) cmsShift, cmsMask) & 1L) == 1L;
    }

    public long getSize() {
        return this.value() >> (int) sizeShift;
    }

    public int age() {
        return (int) Bits.maskBitsLong(this.value() >> (int) ageShift, ageMask);
    }

    public long hash() {
        return Bits.maskBitsLong(this.value() >> (int) hashShift, hashMask);
    }

    public boolean hasNoHash() {
        return this.hash() == noHash;
    }

    public boolean hasMonitor() {
        return (this.value() & monitorValue) != 0L;
    }

    public boolean isLocked() {
        return Bits.maskBitsLong(this.value(), lockMaskInPlace) != unlockedValue;
    }

    public boolean isUnlocked() {
        return Bits.maskBitsLong(this.value(), biasedLockMaskInPlace) == unlockedValue;
    }

    public boolean isMarked() {
        return Bits.maskBitsLong(this.value(), lockMaskInPlace) == markedValue;
    }

    public boolean isBeingInflated() {
        return this.value() == 0L;
    }

    public boolean mustBePreserved() {
        return !this.isUnlocked() || !this.hasNoHash();
    }

    public boolean hasLocker() {
        return (this.value() & lockMaskInPlace) == lockedValue;
    }

    public boolean hasDisplacedMarkHelper() {
        return (this.value() & unlockedValue) == 0L;
    }

    public Mark displacedMarkHelper() {
        if (hasDisplacedMarkHelper()) {
            throw new IllegalStateException();
        }

        return new Mark(getAddress() & (~monitorValue));
    }

    public ObjectMonitor monitor() {
        return new ObjectMonitor(getAddress() ^ monitorValue);
    }
}
