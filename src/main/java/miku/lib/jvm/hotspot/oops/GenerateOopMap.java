package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.interpreter.*;
import miku.lib.jvm.hotspot.runtime.BasicType;
import miku.lib.jvm.hotspot.runtime.SignatureIterator;
import miku.lib.jvm.hotspot.utilities.BitMap;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GenerateOopMap {
    private static final boolean DEBUG = false;
    private static final int MAXARGSIZE = 256;
    private static final int MAX_LOCAL_VARS = 65536;
    private static final boolean TraceMonitorMismatch = true;
    private static final boolean TraceOopMapRewrites = true;
    static CellTypeState[] epsilonCTS;
    static CellTypeState refCTS;
    static CellTypeState valCTS;
    static CellTypeState[] vCTS;
    static CellTypeState[] rCTS;
    static CellTypeState[] rrCTS;
    static CellTypeState[] vrCTS;
    static CellTypeState[] vvCTS;
    static CellTypeState[] rvrCTS;
    static CellTypeState[] vvrCTS;
    static CellTypeState[] vvvCTS;
    static CellTypeState[] vvvrCTS;
    static CellTypeState[] vvvvCTS;
    protected static final int bad_monitors = -1;
    Method _method;
    RetTable _rt;
    int _max_locals;
    int _max_stack;
    int _max_monitors;
    boolean _has_exceptions;
    boolean _got_error;
    String _error_msg;
    boolean _monitor_safe;
    int _state_len;
    CellTypeStateList _state;
    char[] _state_vec_buf;
    int _stack_top;
    int _monitor_top;
    int _report_for_exit_bci;
    int _matching_enter_bci;
    BasicBlock[] _basic_blocks;
    int _gc_points;
    int _bb_count;
    BitMap _bb_hdr_bits;
    boolean _report_result;
    boolean _report_result_for_send;
    BytecodeStream _itr_send;
    List _init_vars;
    boolean _conflict;
    int _nof_refval_conflicts;
    int[] _new_var_map;

    void initState() {
        this._state_len = this._max_locals + this._max_stack + this._max_monitors;
        this._state = new CellTypeStateList(this._state_len);
        this._state_vec_buf = new char[Math.max(this._max_locals, Math.max(this._max_stack, Math.max(this._max_monitors, 1)))];
    }

    void makeContextUninitialized() {
        CellTypeStateList vs = this.vars();

        for (int i = 0; i < this._max_locals; ++i) {
            vs.get(i).set(CellTypeState.uninit);
        }

        this._stack_top = 0;
        this._monitor_top = 0;
    }

    int methodsigToEffect(Symbol signature, boolean isStatic, CellTypeStateList effect) {
        ComputeEntryStack ces = new ComputeEntryStack(signature);
        return ces.computeForParameters(isStatic, effect);
    }

    boolean mergeStateVectors(CellTypeStateList cts, CellTypeStateList bbts) {
        int len = this._max_locals + this._stack_top;
        boolean change = false;

        int i;
        for (i = len - 1; i >= 0; --i) {
            CellTypeState v = cts.get(i).merge(bbts.get(i), i);
            change = change || !v.equal(bbts.get(i));
            bbts.get(i).set(v);
        }

        if (this._max_monitors > 0 && this._monitor_top != -1) {
            int base = this._max_locals + this._max_stack;
            len = base + this._monitor_top;

            for (i = len - 1; i >= base; --i) {
                CellTypeState v = cts.get(i).merge(bbts.get(i), i);
                change = change || !v.equal(bbts.get(i));
                bbts.get(i).set(v);
            }
        }

        return change;
    }

    void copyState(CellTypeStateList dst, CellTypeStateList src) {
        int len = this._max_locals + this._stack_top;

        int base;
        for (base = 0; base < len; ++base) {
            if (src.get(base).isNonlockReference()) {
                dst.get(base).set(CellTypeState.makeSlotRef(base));
            } else {
                dst.get(base).set(src.get(base));
            }
        }

        if (this._max_monitors > 0 && this._monitor_top != -1) {
            base = this._max_locals + this._max_stack;
            len = base + this._monitor_top;

            for (int i = base; i < len; ++i) {
                dst.get(i).set(src.get(i));
            }
        }

    }

    void mergeStateIntoBB(BasicBlock bb) {
        if (this._stack_top == bb._stack_top) {
            if (this._monitor_top == bb._monitor_top) {
                if (this.mergeStateVectors(this._state, bb._state)) {
                    bb.setChanged(true);
                }
            } else {
                bb._monitor_top = -1;
                bb.setChanged(true);
                this._monitor_safe = false;
            }
        } else {
            if (bb.isReachable()) {
                throw new RuntimeException("stack height conflict: " + this._stack_top + " vs. " + bb._stack_top);
            }

            this.copyState(bb._state, this._state);
            bb._stack_top = this._stack_top;
            bb._monitor_top = this._monitor_top;
            bb.setChanged(true);
        }

    }

    void mergeState(int bci, int[] data) {
        this.mergeStateIntoBB(this.getBasicBlockAt(bci));
    }

    void setVar(int localNo, CellTypeState cts) {
        if (localNo >= 0 && localNo <= this._max_locals) {
            this.vars().get(localNo).set(cts);
        } else {
            throw new RuntimeException("variable write error: r" + localNo);
        }
    }

    CellTypeState getVar(int localNo) {
        if (localNo >= 0 && localNo <= this._max_locals) {
            return this.vars().get(localNo).copy();
        } else {
            throw new RuntimeException("variable read error: r" + localNo);
        }
    }

    CellTypeState pop() {
        if (this._stack_top <= 0) {
            throw new RuntimeException("stack underflow");
        } else {
            return this.stack().get(--this._stack_top).copy();
        }
    }

    void push(CellTypeState cts) {
        if (this._stack_top >= this._max_stack) {
            throw new RuntimeException("stack overflow");
        } else {
            this.stack().get(this._stack_top++).set(cts);
        }
    }

    CellTypeState monitorPop() {
        if (this._monitor_top == 0) {
            this._monitor_safe = false;
            this._monitor_top = -1;
            return CellTypeState.ref;
        } else {
            return this.monitors().get(--this._monitor_top).copy();
        }
    }

    void monitorPush(CellTypeState cts) {
        if (this._monitor_top >= this._max_monitors) {
            this._monitor_safe = false;
            this._monitor_top = -1;
        } else {
            this.monitors().get(this._monitor_top++).set(cts);
        }
    }

    CellTypeStateList vars() {
        return this._state;
    }

    CellTypeStateList stack() {
        return this._state.subList(this._max_locals, this._state.size());
    }

    CellTypeStateList monitors() {
        return this._state.subList(this._max_locals + this._max_stack, this._state.size());
    }

    void replaceAllCTSMatches(CellTypeState match, CellTypeState replace) {
        int len = this._max_locals + this._stack_top;
        boolean change = false;

        int i;
        for (i = len - 1; i >= 0; --i) {
            if (match.equal(this._state.get(i))) {
                this._state.get(i).set(replace);
            }
        }

        if (this._monitor_top > 0) {
            int base = this._max_locals + this._max_stack;
            len = base + this._monitor_top;

            for (i = len - 1; i >= base; --i) {
                if (match.equal(this._state.get(i))) {
                    this._state.get(i).set(replace);
                }
            }
        }

    }

    void printStates(PrintStream tty, CellTypeStateList vector, int num) {
        for (int i = 0; i < num; ++i) {
            vector.get(i).print(tty);
        }

    }

    void printCurrentState(PrintStream tty, BytecodeStream currentBC, boolean detailed) {
        int idx;
        if (detailed) {
            tty.print("     " + currentBC.bci() + " vars     = ");
            this.printStates(tty, this.vars(), this._max_locals);
            tty.print("    " + Bytecodes.name(currentBC.code()));
            switch (currentBC.code()) {
                case 182:
                case 183:
                case 184:
                case 185:
                case 186:
                    idx = currentBC.hasIndexU4() ? currentBC.getIndexU4() : currentBC.getIndexU2();
                    tty.print(" idx " + idx);
                default:
                    tty.println();
                    tty.print("          stack    = ");
                    this.printStates(tty, this.stack(), this._stack_top);
                    tty.println();
                    if (this._monitor_top != -1) {
                        tty.print("          monitors = ");
                        this.printStates(tty, this.monitors(), this._monitor_top);
                    } else {
                        tty.print("          [bad monitor stack]");
                    }

                    tty.println();
            }
        } else {
            tty.print("    " + currentBC.bci() + "  vars = '" + this.stateVecToString(this.vars(), this._max_locals) + "' ");
            tty.print("     stack = '" + this.stateVecToString(this.stack(), this._stack_top) + "' ");
            if (this._monitor_top != -1) {
                tty.print("  monitors = '" + this.stateVecToString(this.monitors(), this._monitor_top) + "'  \t" + Bytecodes.name(currentBC.code()));
            } else {
                tty.print("  [bad monitor stack]");
            }

            switch (currentBC.code()) {
                case 182:
                case 183:
                case 184:
                case 185:
                case 186:
                    idx = currentBC.hasIndexU4() ? currentBC.getIndexU4() : currentBC.getIndexU2();
                    tty.print(" idx " + idx);
                default:
                    tty.println();
            }
        }

    }

    void initializeBB() {
        this._gc_points = 0;
        this._bb_count = 0;
        this._bb_hdr_bits = new BitMap(this._method.getCodeSize());
    }

    void markBBHeadersAndCountGCPoints() {
        this.initializeBB();
        boolean fellThrough = false;
        int bytecode;
        if (this.method().hasExceptionTable()) {
            ExceptionTableElement[] excps = this.method().getExceptionTable();

            for (bytecode = 0; bytecode < excps.length; ++bytecode) {
                this.markBB(excps[bytecode].getHandlerPC(), null);
            }
        }

        BytecodeStream bcs = new BytecodeStream(this._method);

        while ((bytecode = bcs.next()) >= 0) {
            int bci = bcs.bci();
            if (!fellThrough) {
                this.markBB(bci, null);
            }

            fellThrough = this.jumpTargetsDo(bcs, GenerateOopMap::markBB, null);
            switch (bytecode) {
                case 168:
                    this.markBB(bci + Bytecodes.lengthFor(bytecode), null);
                    break;
                case 201:
                    this.markBB(bci + Bytecodes.lengthFor(bytecode), null);
            }

            if (this.possibleGCPoint(bcs)) {
                ++this._gc_points;
            }
        }

    }

    boolean isBBHeader(int bci) {
        return this._bb_hdr_bits.at(bci);
    }

    int gcPoints() {
        return this._gc_points;
    }

    int bbCount() {
        return this._bb_count;
    }

    void setBBMarkBit(int bci) {
        this._bb_hdr_bits.atPut(bci, true);
    }

    void clear_bbmark_bit(int bci) {
        this._bb_hdr_bits.atPut(bci, false);
    }

    BasicBlock getBasicBlockAt(int bci) {
        return this.getBasicBlockContaining(bci);
    }

    BasicBlock getBasicBlockContaining(int bci) {
        BasicBlock[] bbs = this._basic_blocks;
        int lo = 0;
        int hi = this._bb_count - 1;

        while (lo <= hi) {
            int m = (lo + hi) / 2;
            int mbci = bbs[m]._bci;
            if (m == this._bb_count - 1) {
                return bbs[m];
            }

            int nbci = bbs[m + 1]._bci;
            if (mbci <= bci && bci < nbci) {
                return bbs[m];
            }

            if (mbci < bci) {
                lo = m + 1;
            } else {
                hi = m - 1;
            }
        }

        throw new RuntimeException("should have found BB");
    }

    void interpBB(BasicBlock bb) {
        this.restoreState(bb);
        BytecodeStream itr = new BytecodeStream(this._method);
        int lim_bci = this.nextBBStartPC(bb);
        itr.setInterval(bb._bci, lim_bci);
        itr.next();

        while (itr.nextBCI() < lim_bci && !this._got_error) {
            if (this._has_exceptions || this._monitor_top != 0) {
                this.doExceptionEdge(itr);
            }

            this.interp1(itr);
            itr.next();
        }

        if (!this._got_error) {
            if (this._has_exceptions || this._monitor_top != 0) {
                this.doExceptionEdge(itr);
            }

            this.interp1(itr);
            boolean fall_through = this.jumpTargetsDo(itr, GenerateOopMap::mergeState, null);
            if (this._got_error) {
                return;
            }

            if (itr.code() == 169) {
                this.retJumpTargetsDo(itr, GenerateOopMap::mergeState, itr.getIndex(), null);
            } else if (fall_through) {
                this.mergeStateIntoBB(this._basic_blocks[this.bbIndex(bb) + 1]);
            }
        }

    }

    void restoreState(BasicBlock bb) {
        for (int i = 0; i < this._state_len; ++i) {
            this._state.get(i).set(bb._state.get(i));
        }

        this._stack_top = bb._stack_top;
        this._monitor_top = bb._monitor_top;
    }

    int nextBBStartPC(BasicBlock bb) {
        int bbNum = this.bbIndex(bb) + 1;
        return bbNum == this._bb_count ? (int) this.method().getCodeSize() : this._basic_blocks[bbNum]._bci;
    }

    void updateBasicBlocks(int bci, int delta) {
        BitMap bbBits = new BitMap((int) (this._method.getCodeSize() + (long) delta));

        for (int k = 0; k < this._bb_count; ++k) {
            if (this._basic_blocks[k]._bci > bci) {
                BasicBlock var10000 = this._basic_blocks[k];
                var10000._bci += delta;
                var10000 = this._basic_blocks[k];
                var10000._end_bci += delta;
            }

            bbBits.atPut(this._basic_blocks[k]._bci, true);
        }

        this._bb_hdr_bits = bbBits;
    }

    void markBB(int bci, int[] data) {
        if (!this.isBBHeader(bci)) {
            this.setBBMarkBit(bci);
            ++this._bb_count;
        }
    }

    void markReachableCode() {
        int[] change = new int[]{1};
        this._basic_blocks[0].markAsAlive();
        int i;
        BasicBlock bb;
        if (this.method().hasExceptionTable()) {
            ExceptionTableElement[] excps = this.method().getExceptionTable();

            for (i = 0; i < excps.length; ++i) {
                bb = this.getBasicBlockAt(excps[i].getHandlerPC());
                if (bb.isDead()) {
                    bb.markAsAlive();
                }
            }
        }

        BytecodeStream bcs = new BytecodeStream(this._method);

        while (change[0] != 0) {
            change[0] = 0;

            for (i = 0; i < this._bb_count; ++i) {
                bb = this._basic_blocks[i];
                if (bb.isAlive()) {
                    bcs.setStart(bb._end_bci);
                    bcs.next();
                    int bytecode = bcs.code();
                    int bci = bcs.bci();
                    boolean fell_through = this.jumpTargetsDo(bcs, GenerateOopMap::reachableBasicblock, change);
                    switch (bytecode) {
                        case 168:
                        case 201:
                            this.reachableBasicblock(bci + Bytecodes.lengthFor(bytecode), change);
                    }

                    if (fell_through && this._basic_blocks[i + 1].isDead()) {
                        this._basic_blocks[i + 1].markAsAlive();
                        change[0] = 1;
                    }
                }
            }
        }

    }

    void reachableBasicblock(int bci, int[] data) {
        BasicBlock bb = this.getBasicBlockAt(bci);
        if (bb.isDead()) {
            bb.markAsAlive();
            data[0] = 1;
        }

    }

    void doInterpretation() {

        do {
            this._conflict = false;
            this._monitor_safe = true;
            if (!this._got_error) {
                this.initBasicBlocks();
            }

            if (!this._got_error) {
                this.setupMethodEntryState();
            }

            if (!this._got_error) {
                this.interpAll();
            }

            if (!this._got_error) {
                this.rewriteRefvalConflicts();
            }

        } while (this._conflict && !this._got_error);

    }

    void initBasicBlocks() {
        this._basic_blocks = new BasicBlock[this._bb_count];

        for (int i = 0; i < this._bb_count; ++i) {
            this._basic_blocks[i] = new BasicBlock();
        }

        BytecodeStream j = new BytecodeStream(this._method);
        int bbNo = 0;
        int monitor_count = 0;

        int prev_bci;
        int bci;
        BasicBlock bb;
        for (prev_bci = -1; j.next() >= 0; prev_bci = bci) {
            if (j.code() == 194) {
                ++monitor_count;
            }

            bci = j.bci();
            if (this.isBBHeader(bci)) {
                bb = this._basic_blocks[bbNo];
                bb._bci = bci;
                bb._max_locals = this._max_locals;
                bb._max_stack = this._max_stack;
                bb.setChanged(false);
                bb._stack_top = -2;
                bb._monitor_top = -1;
                if (bbNo > 0) {
                    this._basic_blocks[bbNo - 1]._end_bci = prev_bci;
                }

                ++bbNo;
            }
        }

        this._basic_blocks[bbNo - 1]._end_bci = prev_bci;
        this._max_monitors = monitor_count;
        this.initState();
        CellTypeStateList basicBlockState = new CellTypeStateList(bbNo * this._state_len);

        for (int blockNum = 0; blockNum < bbNo; ++blockNum) {
            bb = this._basic_blocks[blockNum];
            bb._state = basicBlockState.subList(blockNum * this._state_len, (blockNum + 1) * this._state_len);
        }

        if (bbNo != this._bb_count) {
            if (bbNo < this._bb_count) {
                throw new RuntimeException("jump into the middle of instruction?");
            } else {
                throw new RuntimeException("extra basic blocks - should not happen?");
            }
        } else {
            this.markReachableCode();
        }
    }

    void setupMethodEntryState() {
        this.makeContextUninitialized();
        this.methodsigToEffect(this.method().getSignature(), this.method().isStatic(), this.vars());
        this.initializeVars();
        this.mergeStateIntoBB(this._basic_blocks[0]);
    }

    void interpAll() {
        boolean change = true;

        while (change && !this._got_error) {
            change = false;

            for (int i = 0; i < this._bb_count && !this._got_error; ++i) {
                BasicBlock bb = this._basic_blocks[i];
                if (bb.changed()) {
                    if (this._got_error) {
                        return;
                    }

                    change = true;
                    bb.setChanged(false);
                    this.interpBB(bb);
                }
            }
        }

    }

    void interp1(BytecodeStream itr) {
        if (this._report_result) {
            switch (itr.code()) {
                case 182:
                case 183:
                case 184:
                case 185:
                case 186:
                    this._itr_send = itr;
                    this._report_result_for_send = true;
                    break;
                default:
                    this.fillStackmapForOpcodes(itr, this.vars(), this.stack(), this._stack_top);
            }
        }

        switch (itr.code()) {
            case 0:
            case 132:
            case 167:
            case 169:
            case 200:
                break;
            case 1:
            case 187:
                this.ppush1(CellTypeState.makeLineRef(itr.bci()));
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 11:
            case 12:
            case 13:
            case 16:
            case 17:
                this.ppush1(valCTS);
                break;
            case 9:
            case 10:
            case 14:
            case 15:
                this.ppush(vvCTS);
                break;
            case 18:
                this.doLdc(itr.bci());
                break;
            case 19:
                this.doLdc(itr.bci());
                break;
            case 20:
                this.ppush(vvCTS);
                break;
            case 21:
            case 23:
                this.ppload(vCTS, itr.getIndex());
                break;
            case 22:
            case 24:
                this.ppload(vvCTS, itr.getIndex());
                break;
            case 25:
                this.ppload(rCTS, itr.getIndex());
                break;
            case 26:
            case 34:
                this.ppload(vCTS, 0);
                break;
            case 27:
            case 35:
                this.ppload(vCTS, 1);
                break;
            case 28:
            case 36:
                this.ppload(vCTS, 2);
                break;
            case 29:
            case 37:
                this.ppload(vCTS, 3);
                break;
            case 30:
            case 38:
                this.ppload(vvCTS, 0);
                break;
            case 31:
            case 39:
                this.ppload(vvCTS, 1);
                break;
            case 32:
            case 40:
                this.ppload(vvCTS, 2);
                break;
            case 33:
            case 41:
                this.ppload(vvCTS, 3);
                break;
            case 42:
                this.ppload(rCTS, 0);
                break;
            case 43:
                this.ppload(rCTS, 1);
                break;
            case 44:
                this.ppload(rCTS, 2);
                break;
            case 45:
                this.ppload(rCTS, 3);
                break;
            case 46:
            case 48:
            case 51:
            case 52:
            case 53:
                this.pp(vrCTS, vCTS);
                break;
            case 47:
                this.pp(vrCTS, vvCTS);
                break;
            case 49:
                this.pp(vrCTS, vvCTS);
                break;
            case 50:
                this.ppNewRef(vrCTS, itr.bci());
                break;
            case 54:
            case 56:
                this.ppstore(vCTS, itr.getIndex());
                break;
            case 55:
            case 57:
                this.ppstore(vvCTS, itr.getIndex());
                break;
            case 58:
                this.doAstore(itr.getIndex());
                break;
            case 59:
            case 67:
                this.ppstore(vCTS, 0);
                break;
            case 60:
            case 68:
                this.ppstore(vCTS, 1);
                break;
            case 61:
            case 69:
                this.ppstore(vCTS, 2);
                break;
            case 62:
            case 70:
                this.ppstore(vCTS, 3);
                break;
            case 63:
            case 71:
                this.ppstore(vvCTS, 0);
                break;
            case 64:
            case 72:
                this.ppstore(vvCTS, 1);
                break;
            case 65:
            case 73:
                this.ppstore(vvCTS, 2);
                break;
            case 66:
            case 74:
                this.ppstore(vvCTS, 3);
                break;
            case 75:
                this.doAstore(0);
                break;
            case 76:
                this.doAstore(1);
                break;
            case 77:
                this.doAstore(2);
                break;
            case 78:
                this.doAstore(3);
                break;
            case 79:
            case 81:
            case 84:
            case 85:
            case 86:
                this.ppop(vvrCTS);
                break;
            case 80:
            case 82:
                this.ppop(vvvrCTS);
                break;
            case 83:
                this.ppop(rvrCTS);
                break;
            case 87:
                this.ppopAny(1);
                break;
            case 88:
                this.ppopAny(2);
                break;
            case 89:
                this.ppdupswap(1, "11");
                break;
            case 90:
                this.ppdupswap(2, "121");
                break;
            case 91:
                this.ppdupswap(3, "1321");
                break;
            case 92:
                this.ppdupswap(2, "2121");
                break;
            case 93:
                this.ppdupswap(3, "21321");
                break;
            case 94:
                this.ppdupswap(4, "214321");
                break;
            case 95:
                this.ppdupswap(2, "12");
                break;
            case 96:
            case 98:
            case 100:
            case 102:
            case 104:
            case 106:
            case 108:
            case 110:
            case 112:
            case 114:
            case 120:
            case 122:
            case 124:
            case 126:
            case 128:
            case 130:
            case 136:
            case 137:
            case 142:
            case 144:
            case 149:
            case 150:
                this.pp(vvCTS, vCTS);
                break;
            case 97:
            case 99:
            case 101:
            case 103:
            case 105:
            case 107:
            case 109:
            case 111:
            case 113:
            case 115:
            case 127:
            case 129:
            case 131:
                this.pp(vvvvCTS, vvCTS);
                break;
            case 116:
            case 118:
            case 134:
            case 139:
            case 145:
            case 146:
            case 147:
                this.pp(vCTS, vCTS);
                break;
            case 117:
            case 119:
            case 138:
            case 143:
                this.pp(vvCTS, vvCTS);
                break;
            case 121:
            case 123:
            case 125:
                this.pp(vvvCTS, vvCTS);
                break;
            case 133:
            case 135:
            case 140:
            case 141:
                this.pp(vCTS, vvCTS);
                break;
            case 148:
                this.pp(vvvvCTS, vCTS);
                break;
            case 151:
            case 152:
                this.pp(vvvvCTS, vCTS);
                break;
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 170:
                this.ppop1(valCTS);
                break;
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
                this.ppop(vvCTS);
                break;
            case 165:
            case 166:
                this.ppop(rrCTS);
                break;
            case 168:
                this.doJsr(itr.dest());
                break;
            case 171:
            case 228:
            case 229:
                this.ppop1(valCTS);
                break;
            case 172:
            case 174:
                this.doReturnMonitorCheck();
                this.ppop1(valCTS);
                break;
            case 173:
                this.doReturnMonitorCheck();
                this.ppop(vvCTS);
                break;
            case 175:
                this.doReturnMonitorCheck();
                this.ppop(vvCTS);
                break;
            case 176:
                this.doReturnMonitorCheck();
                this.ppop1(refCTS);
                break;
            case 177:
                this.doReturnMonitorCheck();
                break;
            case 178:
                this.doField(true, true, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 179:
                this.doField(false, true, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 180:
                this.doField(true, false, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 181:
                this.doField(false, false, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 182:
            case 183:
                this.doMethod(false, false, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 184:
                this.doMethod(true, false, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 185:
                this.doMethod(false, true, itr.getIndexU2Cpcache(), itr.bci());
                break;
            case 186:
                this.doMethod(true, false, itr.getIndexU4(), itr.bci());
                break;
            case 188:
            case 189:
                this.ppNewRef(vCTS, itr.bci());
                break;
            case 190:
            case 193:
                this.pp(rCTS, vCTS);
                break;
            case 191:
                if (!this._has_exceptions && this._monitor_top > 0) {
                    this._monitor_safe = false;
                }
                break;
            case 192:
                this.doCheckcast();
                break;
            case 194:
                this.doMonitorenter(itr.bci());
                break;
            case 195:
                this.doMonitorexit(itr.bci());
                break;
            case 196:
                throw new RuntimeException("Iterator should skip this bytecode");
            case 197:
                this.doMultianewarray(itr.codeAt(itr.bci() + 3), itr.bci());
                break;
            case 198:
            case 199:
                this.ppop1(refCTS);
                break;
            case 201:
                this.doJsr(itr.dest_w());
                break;
            case 202:
            case 204:
            case 205:
            case 206:
            case 207:
            case 209:
            case 210:
            case 211:
            case 212:
            case 213:
            case 214:
            case 215:
            case 216:
            case 217:
            case 218:
            case 219:
            case 223:
            case 224:
            case 225:
            case 226:
            case 227:
            default:
                throw new RuntimeException("unexpected opcode: " + itr.code());
            case 203:
                this.ppNewRef(rCTS, itr.bci());
                break;
            case 208:
                this.pp(rCTS, vCTS);
                break;
            case 220:
                this.ppload(rCTS, 0);
                break;
            case 221:
                this.ppush1(valCTS);
                break;
            case 222:
                this.ppNewRef(rCTS, itr.bci());
        }

    }

    void doExceptionEdge(BytecodeStream itr) {
        if (Bytecodes.canTrap(itr.code())) {
            switch (itr.code()) {
                case 42:
                case 220:
                    return;
                case 172:
                case 173:
                case 174:
                case 175:
                case 176:
                case 177:
                    if (this._monitor_top == 0) {
                        return;
                    }
                    break;
                case 195:
                    if (this._monitor_top != -1 && this._monitor_top != 0) {
                        return;
                    }
            }

            if (this._has_exceptions) {
                int bci = itr.bci();
                ExceptionTableElement[] exct = this.method().getExceptionTable();

                for (ExceptionTableElement exceptionTableElement : exct) {
                    int start_pc = exceptionTableElement.getStartPC();
                    int end_pc = exceptionTableElement.getEndPC();
                    int handler_pc = exceptionTableElement.getHandlerPC();
                    int catch_type = exceptionTableElement.getCatchTypeIndex();
                    if (start_pc <= bci && bci < end_pc) {
                        BasicBlock excBB = this.getBasicBlockAt(handler_pc);
                        CellTypeStateList cOpStck = this.stack();
                        CellTypeState cOpStck_0 = cOpStck.get(0).copy();
                        int cOpStackTop = this._stack_top;

                        cOpStck.get(0).set(CellTypeState.makeSlotRef(this._max_locals));
                        this._stack_top = 1;
                        this.mergeStateIntoBB(excBB);
                        cOpStck.get(0).set(cOpStck_0);
                        this._stack_top = cOpStackTop;
                        if (catch_type == 0) {
                            return;
                        }
                    }
                }
            }

            if (this._monitor_top != 0) {
                this._monitor_safe = false;
            }
        }
    }

    void checkType(CellTypeState expected, CellTypeState actual) {
        if (!expected.equalKind(actual)) {
            throw new RuntimeException("wrong type on stack (found: " + actual.toChar() + " expected: " + expected.toChar() + ")");
        }
    }

    void ppstore(CellTypeState[] in, int loc_no) {
        for (int i = 0; i < in.length && !in[i].equal(CellTypeState.bottom); ++i) {
            CellTypeState expected = in[i];
            CellTypeState actual = this.pop();
            this.checkType(expected, actual);


            this.setVar(loc_no++, actual);
        }

    }

    void ppload(CellTypeState[] out, int loc_no) {
        for (int i = 0; i < out.length && !out[i].equal(CellTypeState.bottom); ++i) {
            CellTypeState out1 = out[i];
            CellTypeState vcts = this.getVar(loc_no);
            if (out1.isReference()) {

                if (!vcts.isReference()) {
                    this._conflict = true;
                    if (vcts.canBeUninit()) {
                        this.addToRefInitSet(loc_no);
                    } else {
                        this.recordRefvalConflict(loc_no);
                    }

                    this.push(out1);
                } else {
                    this.push(vcts);
                }
            } else {
                this.push(out1);
            }

            ++loc_no;
        }

    }

    void ppush1(CellTypeState in) {
        this.push(in);
    }

    void ppush(CellTypeState[] in) {
        for (int i = 0; i < in.length && !in[i].equal(CellTypeState.bottom); ++i) {
            this.ppush1(in[i]);
        }

    }

    void ppush(CellTypeStateList in) {
        for (int i = 0; i < in.size() && !in.get(i).equal(CellTypeState.bottom); ++i) {
            this.ppush1(in.get(i));
        }

    }

    void ppop1(CellTypeState out) {
        CellTypeState actual = this.pop();
        this.checkType(out, actual);
    }

    void ppop(CellTypeState[] out) {
        for (int i = 0; i < out.length && !out[i].equal(CellTypeState.bottom); ++i) {
            this.ppop1(out[i]);
        }

    }

    void ppopAny(int poplen) {
        if (this._stack_top >= poplen) {
            this._stack_top -= poplen;
        } else {
            throw new RuntimeException("stack underflow");
        }
    }

    void pp(CellTypeState[] in, CellTypeState[] out) {
        this.ppop(in);
        this.ppush(out);
    }

    void ppNewRef(CellTypeState[] in, int bci) {
        this.ppop(in);
        this.ppush1(CellTypeState.makeLineRef(bci));
    }

    void ppdupswap(int poplen, String out) {
        CellTypeState[] actual = new CellTypeState[5];
        int i;
        for (i = 0; i < poplen; ++i) {
            actual[i] = this.pop();
        }

        for (i = 0; i < out.length(); ++i) {
            char push_ch = out.charAt(i);
            int idx = push_ch - 49;

            this.push(actual[idx]);
        }

    }

    void doLdc(int bci) {
        BytecodeLoadConstant ldc = BytecodeLoadConstant.at(this._method, bci);
        BasicType bt = ldc.resultType();
        CellTypeState cts = bt == BasicType.T_OBJECT ? CellTypeState.makeLineRef(bci) : valCTS;
        this.ppush1(cts);
    }

    void doAstore(int idx) {
        CellTypeState r_or_p = this.pop();
        if (!r_or_p.isAddress() && !r_or_p.isReference()) {
            throw new RuntimeException("wrong type on stack (found: " + r_or_p.toChar() + ", expected: {pr})");
        } else {
            this.setVar(idx, r_or_p);
        }
    }

    void doJsr(int targBCI) {
        this.push(CellTypeState.makeAddr(targBCI));
    }

    void doField(boolean is_get, boolean is_static, int idx, int bci) {
        ConstantPool cp = this.method().getConstants();
        int nameAndTypeIdx = cp.getNameAndTypeRefIndexAt(idx);
        int signatureIdx = cp.getSignatureRefIndexAt(nameAndTypeIdx);
        Symbol signature = cp.getSymbolAt(signatureIdx);
        char sigch = (char) signature.getByteAt(0L);
        CellTypeState[] temp = new CellTypeState[4];
        CellTypeState[] eff = this.sigcharToEffect(sigch, bci, temp);
        CellTypeState[] in = new CellTypeState[4];
        int i = 0;
        CellTypeState[] out;
        if (is_get) {
            out = eff;
        } else {
            out = epsilonCTS;
            i = this.copyCTS(in, eff);
        }

        if (!is_static) {
            in[i++] = CellTypeState.ref;
        }

        in[i] = CellTypeState.bottom;

        this.pp(in, out);
    }

    void doMethod(boolean is_static, boolean is_interface, int idx, int bci) {
        ConstantPool cp = this._method.getConstants();
        Symbol signature = cp.getSignatureRefAt(idx);
        CellTypeStateList out = new CellTypeStateList(4);
        CellTypeStateList in = new CellTypeStateList(257);
        ComputeCallStack cse = new ComputeCallStack(signature);
        if (out.get(0).equal(CellTypeState.ref) && out.get(1).equal(CellTypeState.bottom)) {
            out.get(0).set(CellTypeState.makeLineRef(bci));
        }

        int arg_length = cse.computeForParameters(is_static, in);

        for (int i = arg_length - 1; i >= 0; --i) {
            this.ppop1(in.get(i));
        }

        if (this._report_result_for_send) {
            this.fillStackmapForOpcodes(this._itr_send, this.vars(), this.stack(), this._stack_top);
            this._report_result_for_send = false;
        }

        this.ppush(out);
    }

    void doMultianewarray(int dims, int bci) {
        for (int i = dims - 1; i >= 0; --i) {
            this.ppop1(valCTS);
        }

        this.ppush1(CellTypeState.makeLineRef(bci));
    }

    void doMonitorenter(int bci) {
        CellTypeState actual = this.pop();
        if (this._monitor_top != -1) {
            if (actual.isLockReference()) {
                this._monitor_top = -1;
                this._monitor_safe = false;
            } else {
                CellTypeState lock = CellTypeState.makeLockRef(bci);
                this.checkType(refCTS, actual);
                if (!actual.isInfoTop()) {
                    this.replaceAllCTSMatches(actual, lock);
                    this.monitorPush(lock);
                }

            }
        }
    }

    void doMonitorexit(int bci) {
        CellTypeState actual = this.pop();
        if (this._monitor_top != -1) {
            this.checkType(refCTS, actual);
            CellTypeState expected = this.monitorPop();
            if (actual.isLockReference() && expected.equal(actual)) {
                this.replaceAllCTSMatches(actual, CellTypeState.makeLineRef(bci));
            } else {
                this._monitor_top = -1;
                this._monitor_safe = false;
                BasicBlock bb = this.getBasicBlockContaining(bci);
                bb.setChanged(true);
                bb._monitor_top = -1;
            }

            if (this._report_for_exit_bci == bci) {
                this._matching_enter_bci = expected.getMonitorSource();
            }

        }
    }

    void doReturnMonitorCheck() {
        if (this._monitor_top > 0) {
            this._monitor_safe = false;
        }

    }

    void doCheckcast() {
        CellTypeState actual = this.pop();
        this.checkType(refCTS, actual);
        this.push(actual);
    }

    CellTypeState[] sigcharToEffect(char sigch, int bci, CellTypeState[] out) {
        if (sigch != 'L' && sigch != '[') {
            if (sigch != 'J' && sigch != 'D') {
                return sigch == 'V' ? epsilonCTS : vCTS;
            } else {
                return vvCTS;
            }
        } else {
            out[0] = CellTypeState.makeLineRef(bci);
            out[1] = CellTypeState.bottom;
            return out;
        }
    }

    int copyCTS(CellTypeState[] dst, CellTypeState[] src) {
        int idx;
        for (idx = 0; idx < src.length && !src[idx].isBottom(); ++idx) {
            dst[idx] = src[idx];
        }

        return idx;
    }

    void reportResult() {
        this._report_result = true;
        this.fillStackmapProlog(this._gc_points);

        for (int i = 0; i < this._bb_count; ++i) {
            if (this._basic_blocks[i].isReachable()) {
                this._basic_blocks[i].setChanged(true);
                this.interpBB(this._basic_blocks[i]);
            }
        }

        this.fillStackmapEpilog();
        this.fillInitVars(this._init_vars);
        this._report_result = false;
    }

    void initializeVars() {
        for (int k = 0; k < this._init_vars.size(); ++k) {
            this._state.get((Integer) this._init_vars.get(k)).set(CellTypeState.makeSlotRef(k));
        }

    }

    void addToRefInitSet(int localNo) {
        Integer local = localNo;
        if (!this._init_vars.contains(local)) {
            this._init_vars.add(local);
        }
    }

    void recordRefvalConflict(int varNo) {
        System.err.println("### Conflict detected (local no: " + varNo + ")");
        if (this._new_var_map == null) {
            this._new_var_map = new int[this._max_locals];

            for (int k = 0; k < this._max_locals; this._new_var_map[k] = k++) {
            }
        }

        if (this._new_var_map[varNo] == varNo) {
            if (this._max_locals + this._nof_refval_conflicts >= 65536) {
                throw new RuntimeException("Rewriting exceeded local variable limit");
            }

            this._new_var_map[varNo] = this._max_locals + this._nof_refval_conflicts;
            ++this._nof_refval_conflicts;
        }

    }

    void rewriteRefvalConflicts() {
        if (this._nof_refval_conflicts > 0) {
            throw new RuntimeException("Method rewriting not yet implemented in Java");
        }
    }

    String stateVecToString(CellTypeStateList vec, int len) {
        for (int i = 0; i < len; ++i) {
            this._state_vec_buf[i] = vec.get(i).toChar();
        }

        return new String(this._state_vec_buf, 0, len);
    }

    void retJumpTargetsDo(BytecodeStream bcs, JumpClosure closure, int varNo, int[] data) {
        CellTypeState ra = this.vars().get(varNo);
        if (!ra.isGoodAddress()) {
            throw new RuntimeException("ret returns from two jsr subroutines?");
        } else {
            int target = ra.getInfo();
            RetTableEntry rtEnt = this._rt.findJsrsForTarget(target);
            int bci = bcs.bci();

            for (int i = 0; i < rtEnt.nofJsrs(); ++i) {
                int target_bci = rtEnt.jsrs(i);
                BasicBlock jsr_bb = this.getBasicBlockContaining(target_bci - 1);

                boolean alive = jsr_bb.isAlive();
                if (alive) {
                    closure.process(this, target_bci, data);
                }
            }

        }
    }

    boolean jumpTargetsDo(BytecodeStream bcs, JumpClosure closure, int[] data) {
        int bci = bcs.bci();
        int npairs;
        switch (bcs.code()) {
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 198:
            case 199:
                closure.process(this, bcs.dest(), data);
                closure.process(this, bci + 3, data);
                break;
            case 167:
                closure.process(this, bcs.dest(), data);
                break;
            case 168:
                closure.process(this, bcs.dest(), data);
            case 169:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 191:
                break;
            case 170:
                BytecodeTableswitch tableswitch = BytecodeTableswitch.at(bcs);
                npairs = tableswitch.length();
                closure.process(this, bci + tableswitch.defaultOffset(), data);

                while (true) {
                    --npairs;
                    if (npairs < 0) {
                        return false;
                    }

                    closure.process(this, bci + tableswitch.destOffsetAt(npairs), data);
                }
            case 171:
            case 228:
            case 229:
                BytecodeLookupswitch lookupswitch = BytecodeLookupswitch.at(bcs);
                npairs = lookupswitch.numberOfPairs();
                closure.process(this, bci + lookupswitch.defaultOffset(), data);

                while (true) {
                    --npairs;
                    if (npairs < 0) {
                        return false;
                    }

                    LookupswitchPair pair = lookupswitch.pairAt(npairs);
                    closure.process(this, bci + pair.offset(), data);
                }
            case 178:
            case 179:
            case 180:
            case 181:
            case 182:
            case 183:
            case 184:
            case 185:
            case 186:
            case 187:
            case 188:
            case 189:
            case 190:
            case 192:
            case 193:
            case 194:
            case 195:
            case 197:
            case 202:
            case 203:
            case 204:
            case 205:
            case 206:
            case 207:
            case 208:
            case 209:
            case 210:
            case 211:
            case 212:
            case 213:
            case 214:
            case 215:
            case 216:
            case 217:
            case 218:
            case 219:
            case 220:
            case 221:
            case 222:
            case 223:
            case 224:
            case 225:
            case 226:
            case 227:
            default:
                return true;
            case 196:
                throw new RuntimeException("Should not reach here");
            case 200:
                closure.process(this, bcs.dest_w(), data);
                break;
            case 201:
                closure.process(this, bcs.dest_w(), data);
        }

        return false;
    }

    public GenerateOopMap(Method method) {
        this._method = method;
        this._max_locals = 0;
        this._init_vars = null;
        this._rt = new RetTable();
    }

    public void computeMap() {
        this._got_error = false;
        this._conflict = false;
        this._max_locals = this.method().getMaxLocals();
        this._max_stack = this.method().getMaxStack();
        this._has_exceptions = this.method().hasExceptionTable();
        this._nof_refval_conflicts = 0;
        this._init_vars = new ArrayList(5);
        this._report_result = false;
        this._report_result_for_send = false;
        this._report_for_exit_bci = -1;
        this._new_var_map = null;
        if (this.method().getCodeSize() != 0L && (long) this._max_locals + this.method().getMaxStack() != 0L) {
            if (!this._got_error) {
                this._rt.computeRetTable(this._method);
            }

            if (!this._got_error) {
                this.markBBHeadersAndCountGCPoints();
            }

            if (!this._got_error) {
                this.doInterpretation();
            }

            if (!this._got_error && this.reportResults()) {
                this.reportResult();
            }

            if (this._got_error) {
                throw new RuntimeException("Illegal bytecode sequence encountered while generating interpreter pointer maps - method should be rejected by verifier.");
            }
        } else {
            this.fillStackmapProlog(0);
            this.fillStackmapEpilog();
        }
    }

    public void resultForBasicblock(int bci) {
        this._report_result = true;
        BasicBlock bb = this.getBasicBlockContaining(bci);

        bb.setChanged(true);
        this.interpBB(bb);
    }

    public int maxLocals() {
        return this._max_locals;
    }

    public Method method() {
        return this._method;
    }

    public boolean monitorSafe() {
        return this._monitor_safe;
    }

    public int getMonitorMatch(int bci) {

        this._report_for_exit_bci = bci;
        this._matching_enter_bci = -1;
        BasicBlock bb = this.getBasicBlockContaining(bci);
        if (bb.isReachable()) {
            bb.setChanged(true);
            this.interpBB(bb);
            this._report_for_exit_bci = -1;
        }

        return this._matching_enter_bci;
    }

    private int bbIndex(BasicBlock bb) {
        for (int i = 0; i < this._basic_blocks.length; ++i) {
            if (this._basic_blocks[i] == bb) {
                return i;
            }
        }

        throw new RuntimeException("Should have found block");
    }

    public boolean allowRewrites() {
        return false;
    }

    public boolean reportResults() {
        return true;
    }

    public boolean reportInitVars() {
        return true;
    }

    public boolean possibleGCPoint(BytecodeStream bcs) {
        throw new RuntimeException("ShouldNotReachHere");
    }

    public void fillStackmapProlog(int nofGCPoints) {
        throw new RuntimeException("ShouldNotReachHere");
    }

    public void fillStackmapEpilog() {
        throw new RuntimeException("ShouldNotReachHere");
    }

    public void fillStackmapForOpcodes(BytecodeStream bcs, CellTypeStateList vars, CellTypeStateList stack, int stackTop) {
        throw new RuntimeException("ShouldNotReachHere");
    }

    public void fillInitVars(List init_vars) {
        throw new RuntimeException("ShouldNotReachHere");
    }

    static {
        epsilonCTS = new CellTypeState[]{CellTypeState.bottom};
        refCTS = CellTypeState.ref;
        valCTS = CellTypeState.value;
        vCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.bottom};
        rCTS = new CellTypeState[]{CellTypeState.ref, CellTypeState.bottom};
        rrCTS = new CellTypeState[]{CellTypeState.ref, CellTypeState.ref, CellTypeState.bottom};
        vrCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.ref, CellTypeState.bottom};
        vvCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.value, CellTypeState.bottom};
        rvrCTS = new CellTypeState[]{CellTypeState.ref, CellTypeState.value, CellTypeState.ref, CellTypeState.bottom};
        vvrCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.value, CellTypeState.ref, CellTypeState.bottom};
        vvvCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.bottom};
        vvvrCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.ref, CellTypeState.bottom};
        vvvvCTS = new CellTypeState[]{CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.value, CellTypeState.bottom};
    }


    static class BasicBlock {
        private boolean _changed;
        static final int _dead_basic_block = -2;
        static final int _unreached = -1;
        int _bci;
        int _end_bci;
        int _max_locals;
        int _max_stack;
        CellTypeStateList _state;
        int _stack_top;
        int _monitor_top;

        BasicBlock() {
        }

        CellTypeStateList vars() {
            return this._state;
        }

        CellTypeStateList stack() {
            return this._state.subList(this._max_locals, this._state.size());
        }

        boolean changed() {
            return this._changed;
        }

        void setChanged(boolean s) {
            this._changed = s;
        }

        boolean isReachable() {
            return this._stack_top >= 0;
        }

        boolean isDead() {
            return this._stack_top == -2;
        }

        boolean isAlive() {
            return this._stack_top != -2;
        }

        void markAsAlive() {
            this._stack_top = -1;
        }
    }

    static class RetTable {
        private RetTableEntry _first;
        private static int _init_nof_entries;

        private void addJsr(int return_bci, int target_bci) {
            RetTableEntry entry;
            for (entry = this._first; entry != null && entry.targetBci() != target_bci; entry = entry.next()) {
            }

            if (entry == null) {
                entry = new RetTableEntry(target_bci, this._first);
                this._first = entry;
            }

            entry.addJsr(return_bci);
        }

        RetTable() {
        }

        void computeRetTable(Method method) {
            BytecodeStream i = new BytecodeStream(method);

            int bytecode;
            while ((bytecode = i.next()) >= 0) {
                switch (bytecode) {
                    case 168:
                        this.addJsr(i.nextBCI(), i.dest());
                        break;
                    case 201:
                        this.addJsr(i.nextBCI(), i.dest_w());
                }
            }

        }

        void updateRetTable(int bci, int delta) {
            for (RetTableEntry cur = this._first; cur != null; cur = cur.next()) {
                cur.addDelta(bci, delta);
            }

        }

        RetTableEntry findJsrsForTarget(int targBci) {
            for (RetTableEntry cur = this._first; cur != null; cur = cur.next()) {

                if (cur.targetBci() == targBci) {
                    return cur;
                }
            }

            throw new RuntimeException("Should not reach here");
        }
    }

    static class RetTableEntry {
        private static int _init_nof_jsrs;
        private int _target_bci;
        private final List<Integer> _jsrs;
        private final RetTableEntry _next;

        RetTableEntry(int target, RetTableEntry next) {
            this._target_bci = target;
            this._jsrs = new ArrayList<>(_init_nof_jsrs);
            this._next = next;
        }

        int targetBci() {
            return this._target_bci;
        }

        int nofJsrs() {
            return this._jsrs.size();
        }

        int jsrs(int i) {
            return this._jsrs.get(i);
        }

        void addJsr(int return_bci) {
            this._jsrs.add(return_bci);
        }

        void addDelta(int bci, int delta) {
            if (this._target_bci > bci) {
                this._target_bci += delta;
            }

            for (int k = 0; k < this.nofJsrs(); ++k) {
                int jsr = this.jsrs(k);
                if (jsr > bci) {
                    this._jsrs.set(k, jsr + delta);
                }
            }

        }

        RetTableEntry next() {
            return this._next;
        }
    }

    static class ComputeEntryStack extends SignatureIterator {
        CellTypeStateList _effect;
        int _idx;

        void set(CellTypeState state) {
            this._effect.get(this._idx++).set(state);
        }

        int length() {
            return this._idx;
        }

        public void doBool() {
            this.set(CellTypeState.value);
        }

        public void doChar() {
            this.set(CellTypeState.value);
        }

        public void doFloat() {
            this.set(CellTypeState.value);
        }

        public void doByte() {
            this.set(CellTypeState.value);
        }

        public void doShort() {
            this.set(CellTypeState.value);
        }

        public void doInt() {
            this.set(CellTypeState.value);
        }

        public void doVoid() {
            this.set(CellTypeState.bottom);
        }

        public void doObject(int begin, int end) {
            this.set(CellTypeState.makeSlotRef(this._idx));
        }

        public void doArray(int begin, int end) {
            this.set(CellTypeState.makeSlotRef(this._idx));
        }

        public void doDouble() {
            this.set(CellTypeState.value);
            this.set(CellTypeState.value);
        }

        public void doLong() {
            this.set(CellTypeState.value);
            this.set(CellTypeState.value);
        }

        ComputeEntryStack(Symbol signature) {
            super(signature);
        }

        int computeForParameters(boolean is_static, CellTypeStateList effect) {
            this._idx = 0;
            this._effect = effect;
            if (!is_static) {
                effect.get(this._idx++).set(CellTypeState.makeSlotRef(0));
            }

            this.iterateParameters();
            return this.length();
        }

        int computeForReturntype(CellTypeStateList effect) {
            this._idx = 0;
            this._effect = effect;
            this.iterateReturntype();
            this.set(CellTypeState.bottom);
            return this.length();
        }
    }

    static class ComputeCallStack extends SignatureIterator {
        CellTypeStateList _effect;
        int _idx;

        void set(CellTypeState state) {
            this._effect.get(this._idx++).set(state);
        }

        int length() {
            return this._idx;
        }

        public void doBool() {
            this.set(CellTypeState.value);
        }

        public void doChar() {
            this.set(CellTypeState.value);
        }

        public void doFloat() {
            this.set(CellTypeState.value);
        }

        public void doByte() {
            this.set(CellTypeState.value);
        }

        public void doShort() {
            this.set(CellTypeState.value);
        }

        public void doInt() {
            this.set(CellTypeState.value);
        }

        public void doVoid() {
            this.set(CellTypeState.bottom);
        }

        public void doObject(int begin, int end) {
            this.set(CellTypeState.ref);
        }

        public void doArray(int begin, int end) {
            this.set(CellTypeState.ref);
        }

        public void doDouble() {
            this.set(CellTypeState.value);
            this.set(CellTypeState.value);
        }

        public void doLong() {
            this.set(CellTypeState.value);
            this.set(CellTypeState.value);
        }

        ComputeCallStack(Symbol signature) {
            super(signature);
        }

        int computeForParameters(boolean is_static, CellTypeStateList effect) {
            this._idx = 0;
            this._effect = effect;
            if (!is_static) {
                effect.get(this._idx++).set(CellTypeState.ref);
            }

            this.iterateParameters();
            return this.length();
        }

        int computeForReturntype(CellTypeStateList effect) {
            this._idx = 0;
            this._effect = effect;
            this.iterateReturntype();
            this.set(CellTypeState.bottom);
            return this.length();
        }
    }

    interface JumpClosure {
        void process(GenerateOopMap var1, int var2, int[] var3);
    }
}
