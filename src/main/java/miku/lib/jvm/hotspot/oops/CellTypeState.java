package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.utilities.Bits;

import java.io.PrintStream;

public class CellTypeState {
    private int _state;
    private static final int info_mask = Bits.rightNBits(28);
    private static final int bits_mask;
    private static final int uninit_bit;
    private static final int ref_bit;
    private static final int val_bit;
    private static final int addr_bit;
    private static final int live_bits_mask;
    private static final int top_info_bit;
    private static final int not_bottom_info_bit;
    private static final int info_data_mask;
    private static final int info_conflict;
    private static final int ref_not_lock_bit;
    private static final int ref_slot_bit;
    private static final int ref_data_mask;
    private static final int bottom_value = 0;
    private static final int uninit_value;
    private static final int ref_value;
    private static final int ref_conflict;
    private static final int val_value;
    private static final int addr_value;
    private static final int addr_conflict;
    public static CellTypeState bottom;
    public static CellTypeState uninit;
    public static CellTypeState ref;
    public static CellTypeState value;
    public static CellTypeState refUninit;
    public static CellTypeState top;
    public static CellTypeState addr;

    private CellTypeState() {
    }

    private CellTypeState(int state) {
        this._state = state;
    }

    public CellTypeState copy() {
        return new CellTypeState(this._state);
    }

    public static CellTypeState makeAny(int state) {
        return new CellTypeState(state);
    }

    public static CellTypeState makeBottom() {
        return makeAny(0);
    }

    public static CellTypeState makeTop() {
        return makeAny(-1);
    }

    public static CellTypeState makeAddr(int bci) {
        return makeAny(addr_bit | not_bottom_info_bit | bci & info_data_mask);
    }

    public static CellTypeState makeSlotRef(int slot_num) {
        return makeAny(ref_bit | not_bottom_info_bit | ref_not_lock_bit | ref_slot_bit | slot_num & ref_data_mask);
    }

    public static CellTypeState makeLineRef(int bci) {
        return makeAny(ref_bit | not_bottom_info_bit | ref_not_lock_bit | bci & ref_data_mask);
    }

    public static CellTypeState makeLockRef(int bci) {
        return makeAny(ref_bit | not_bottom_info_bit | bci & ref_data_mask);
    }

    public boolean isBottom() {
        return this._state == 0;
    }

    public boolean isLive() {
        return (this._state & live_bits_mask) != 0;
    }

    public boolean isValidState() {
        if ((this.canBeUninit() || this.canBeValue()) && !this.isInfoTop()) {
            return false;
        } else if (this.isInfoTop() && (this._state & info_mask) != info_mask) {
            return false;
        } else {
            return !this.isInfoBottom() || (this._state & info_mask) == 0;
        }
    }

    public boolean isAddress() {
        return (this._state & bits_mask) == addr_bit;
    }

    public boolean isReference() {
        return (this._state & bits_mask) == ref_bit;
    }

    public boolean isValue() {
        return (this._state & bits_mask) == val_bit;
    }

    public boolean isUninit() {
        return (this._state & bits_mask) == uninit_bit;
    }

    public boolean canBeAddress() {
        return (this._state & addr_bit) != 0;
    }

    public boolean canBeReference() {
        return (this._state & ref_bit) != 0;
    }

    public boolean canBeValue() {
        return (this._state & val_bit) != 0;
    }

    public boolean canBeUninit() {
        return (this._state & uninit_bit) != 0;
    }

    public boolean isInfoBottom() {
        return (this._state & not_bottom_info_bit) == 0;
    }

    public boolean isInfoTop() {
        return (this._state & top_info_bit) != 0;
    }

    public int getInfo() {
        return this._state & info_data_mask;
    }

    public int getMonitorSource() {
        return this.getInfo();
    }

    public boolean isGoodAddress() {
        return this.isAddress() && !this.isInfoTop();
    }

    public boolean isLockReference() {
        return (this._state & (bits_mask | top_info_bit | ref_not_lock_bit)) == ref_bit;
    }

    public boolean isNonlockReference() {
        return (this._state & (bits_mask | top_info_bit | ref_not_lock_bit)) == (ref_bit | ref_not_lock_bit);
    }

    public boolean equal(CellTypeState a) {
        return this._state == a._state;
    }

    public boolean equalKind(CellTypeState a) {
        return (this._state & bits_mask) == (a._state & bits_mask);
    }

    public char toChar() {
        if (this.canBeReference()) {
            return !this.canBeValue() && !this.canBeAddress() ? 'r' : '#';
        } else if (this.canBeValue()) {
            return 'v';
        } else if (this.canBeAddress()) {
            return 'p';
        } else {
            return this.canBeUninit() ? ' ' : '@';
        }
    }

    public void set(CellTypeState cts) {
        this._state = cts._state;
    }

    public CellTypeState merge(CellTypeState cts, int slot) {
        CellTypeState result = new CellTypeState();
        result._state = this._state | cts._state;
        if (!result.isInfoTop()) {
            if (!this.equal(cts)) {
                if (result.isReference()) {
                    result = makeSlotRef(slot);
                } else {
                    result._state |= info_conflict;
                }
            }
        }

        return result;
    }

    public void print(PrintStream tty) {
        if (this.canBeAddress()) {
            tty.print("(p");
        } else {
            tty.print("( ");
        }

        if (this.canBeReference()) {
            tty.print("r");
        } else {
            tty.print(" ");
        }

        if (this.canBeValue()) {
            tty.print("v");
        } else {
            tty.print(" ");
        }

        if (this.canBeUninit()) {
            tty.print("u|");
        } else {
            tty.print(" |");
        }

        if (this.isInfoTop()) {
            tty.print("Top)");
        } else if (this.isInfoBottom()) {
            tty.print("Bot)");
        } else if (this.isReference()) {
            int info = this.getInfo();
            int data = info & ~(ref_not_lock_bit | ref_slot_bit);
            if ((info & ref_not_lock_bit) != 0) {
                if ((info & ref_slot_bit) != 0) {
                    tty.print("slot" + data + ")");
                } else {
                    tty.print("line" + data + ")");
                }
            } else {
                tty.print("lock" + data + ")");
            }
        } else {
            tty.print(this.getInfo() + ")");
        }

    }

    static {
        bits_mask = ~info_mask;
        uninit_bit = Bits.nthBit(31);
        ref_bit = Bits.nthBit(30);
        val_bit = Bits.nthBit(29);
        addr_bit = Bits.nthBit(28);
        live_bits_mask = bits_mask & ~uninit_bit;
        top_info_bit = Bits.nthBit(27);
        not_bottom_info_bit = Bits.nthBit(26);
        info_data_mask = Bits.rightNBits(26);
        info_conflict = info_mask;
        ref_not_lock_bit = Bits.nthBit(25);
        ref_slot_bit = Bits.nthBit(24);
        ref_data_mask = Bits.rightNBits(24);
        uninit_value = uninit_bit | info_conflict;
        ref_value = ref_bit;
        ref_conflict = ref_bit | info_conflict;
        val_value = val_bit | info_conflict;
        addr_value = addr_bit;
        addr_conflict = addr_bit | info_conflict;
        bottom = makeBottom();
        uninit = makeAny(uninit_value);
        ref = makeAny(ref_conflict);
        value = makeAny(val_value);
        refUninit = makeAny(ref_conflict | uninit_value);
        top = makeTop();
        addr = makeAny(addr_conflict);
    }
}
