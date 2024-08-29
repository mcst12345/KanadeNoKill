package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.runtime.BasicType;
import miku.lib.jvm.hotspot.utilities.Bits;

public class Bytecodes {
    public static final int _illegal = -1;
    public static final int _nop = 0;
    public static final int _aconst_null = 1;
    public static final int _iconst_m1 = 2;
    public static final int _iconst_0 = 3;
    public static final int _iconst_1 = 4;
    public static final int _iconst_2 = 5;
    public static final int _iconst_3 = 6;
    public static final int _iconst_4 = 7;
    public static final int _iconst_5 = 8;
    public static final int _lconst_0 = 9;
    public static final int _lconst_1 = 10;
    public static final int _fconst_0 = 11;
    public static final int _fconst_1 = 12;
    public static final int _fconst_2 = 13;
    public static final int _dconst_0 = 14;
    public static final int _dconst_1 = 15;
    public static final int _bipush = 16;
    public static final int _sipush = 17;
    public static final int _ldc = 18;
    public static final int _ldc_w = 19;
    public static final int _ldc2_w = 20;
    public static final int _iload = 21;
    public static final int _lload = 22;
    public static final int _fload = 23;
    public static final int _dload = 24;
    public static final int _aload = 25;
    public static final int _iload_0 = 26;
    public static final int _iload_1 = 27;
    public static final int _iload_2 = 28;
    public static final int _iload_3 = 29;
    public static final int _lload_0 = 30;
    public static final int _lload_1 = 31;
    public static final int _lload_2 = 32;
    public static final int _lload_3 = 33;
    public static final int _fload_0 = 34;
    public static final int _fload_1 = 35;
    public static final int _fload_2 = 36;
    public static final int _fload_3 = 37;
    public static final int _dload_0 = 38;
    public static final int _dload_1 = 39;
    public static final int _dload_2 = 40;
    public static final int _dload_3 = 41;
    public static final int _aload_0 = 42;
    public static final int _aload_1 = 43;
    public static final int _aload_2 = 44;
    public static final int _aload_3 = 45;
    public static final int _iaload = 46;
    public static final int _laload = 47;
    public static final int _faload = 48;
    public static final int _daload = 49;
    public static final int _aaload = 50;
    public static final int _baload = 51;
    public static final int _caload = 52;
    public static final int _saload = 53;
    public static final int _istore = 54;
    public static final int _lstore = 55;
    public static final int _fstore = 56;
    public static final int _dstore = 57;
    public static final int _astore = 58;
    public static final int _istore_0 = 59;
    public static final int _istore_1 = 60;
    public static final int _istore_2 = 61;
    public static final int _istore_3 = 62;
    public static final int _lstore_0 = 63;
    public static final int _lstore_1 = 64;
    public static final int _lstore_2 = 65;
    public static final int _lstore_3 = 66;
    public static final int _fstore_0 = 67;
    public static final int _fstore_1 = 68;
    public static final int _fstore_2 = 69;
    public static final int _fstore_3 = 70;
    public static final int _dstore_0 = 71;
    public static final int _dstore_1 = 72;
    public static final int _dstore_2 = 73;
    public static final int _dstore_3 = 74;
    public static final int _astore_0 = 75;
    public static final int _astore_1 = 76;
    public static final int _astore_2 = 77;
    public static final int _astore_3 = 78;
    public static final int _iastore = 79;
    public static final int _lastore = 80;
    public static final int _fastore = 81;
    public static final int _dastore = 82;
    public static final int _aastore = 83;
    public static final int _bastore = 84;
    public static final int _castore = 85;
    public static final int _sastore = 86;
    public static final int _pop = 87;
    public static final int _pop2 = 88;
    public static final int _dup = 89;
    public static final int _dup_x1 = 90;
    public static final int _dup_x2 = 91;
    public static final int _dup2 = 92;
    public static final int _dup2_x1 = 93;
    public static final int _dup2_x2 = 94;
    public static final int _swap = 95;
    public static final int _iadd = 96;
    public static final int _ladd = 97;
    public static final int _fadd = 98;
    public static final int _dadd = 99;
    public static final int _isub = 100;
    public static final int _lsub = 101;
    public static final int _fsub = 102;
    public static final int _dsub = 103;
    public static final int _imul = 104;
    public static final int _lmul = 105;
    public static final int _fmul = 106;
    public static final int _dmul = 107;
    public static final int _idiv = 108;
    public static final int _ldiv = 109;
    public static final int _fdiv = 110;
    public static final int _ddiv = 111;
    public static final int _irem = 112;
    public static final int _lrem = 113;
    public static final int _frem = 114;
    public static final int _drem = 115;
    public static final int _ineg = 116;
    public static final int _lneg = 117;
    public static final int _fneg = 118;
    public static final int _dneg = 119;
    public static final int _ishl = 120;
    public static final int _lshl = 121;
    public static final int _ishr = 122;
    public static final int _lshr = 123;
    public static final int _iushr = 124;
    public static final int _lushr = 125;
    public static final int _iand = 126;
    public static final int _land = 127;
    public static final int _ior = 128;
    public static final int _lor = 129;
    public static final int _ixor = 130;
    public static final int _lxor = 131;
    public static final int _iinc = 132;
    public static final int _i2l = 133;
    public static final int _i2f = 134;
    public static final int _i2d = 135;
    public static final int _l2i = 136;
    public static final int _l2f = 137;
    public static final int _l2d = 138;
    public static final int _f2i = 139;
    public static final int _f2l = 140;
    public static final int _f2d = 141;
    public static final int _d2i = 142;
    public static final int _d2l = 143;
    public static final int _d2f = 144;
    public static final int _i2b = 145;
    public static final int _i2c = 146;
    public static final int _i2s = 147;
    public static final int _lcmp = 148;
    public static final int _fcmpl = 149;
    public static final int _fcmpg = 150;
    public static final int _dcmpl = 151;
    public static final int _dcmpg = 152;
    public static final int _ifeq = 153;
    public static final int _ifne = 154;
    public static final int _iflt = 155;
    public static final int _ifge = 156;
    public static final int _ifgt = 157;
    public static final int _ifle = 158;
    public static final int _if_icmpeq = 159;
    public static final int _if_icmpne = 160;
    public static final int _if_icmplt = 161;
    public static final int _if_icmpge = 162;
    public static final int _if_icmpgt = 163;
    public static final int _if_icmple = 164;
    public static final int _if_acmpeq = 165;
    public static final int _if_acmpne = 166;
    public static final int _goto = 167;
    public static final int _jsr = 168;
    public static final int _ret = 169;
    public static final int _tableswitch = 170;
    public static final int _lookupswitch = 171;
    public static final int _ireturn = 172;
    public static final int _lreturn = 173;
    public static final int _freturn = 174;
    public static final int _dreturn = 175;
    public static final int _areturn = 176;
    public static final int _return = 177;
    public static final int _getstatic = 178;
    public static final int _putstatic = 179;
    public static final int _getfield = 180;
    public static final int _putfield = 181;
    public static final int _invokevirtual = 182;
    public static final int _invokespecial = 183;
    public static final int _invokestatic = 184;
    public static final int _invokeinterface = 185;
    public static final int _invokedynamic = 186;
    public static final int _new = 187;
    public static final int _newarray = 188;
    public static final int _anewarray = 189;
    public static final int _arraylength = 190;
    public static final int _athrow = 191;
    public static final int _checkcast = 192;
    public static final int _instanceof = 193;
    public static final int _monitorenter = 194;
    public static final int _monitorexit = 195;
    public static final int _wide = 196;
    public static final int _multianewarray = 197;
    public static final int _ifnull = 198;
    public static final int _ifnonnull = 199;
    public static final int _goto_w = 200;
    public static final int _jsr_w = 201;
    public static final int _breakpoint = 202;
    public static final int number_of_java_codes = 203;
    public static final int _fast_agetfield = 203;
    public static final int _fast_bgetfield = 204;
    public static final int _fast_cgetfield = 205;
    public static final int _fast_dgetfield = 206;
    public static final int _fast_fgetfield = 207;
    public static final int _fast_igetfield = 208;
    public static final int _fast_lgetfield = 209;
    public static final int _fast_sgetfield = 210;
    public static final int _fast_aputfield = 211;
    public static final int _fast_bputfield = 212;
    public static final int _fast_zputfield = 213;
    public static final int _fast_cputfield = 214;
    public static final int _fast_dputfield = 215;
    public static final int _fast_fputfield = 216;
    public static final int _fast_iputfield = 217;
    public static final int _fast_lputfield = 218;
    public static final int _fast_sputfield = 219;
    public static final int _fast_aload_0 = 220;
    public static final int _fast_iaccess_0 = 221;
    public static final int _fast_aaccess_0 = 222;
    public static final int _fast_faccess_0 = 223;
    public static final int _fast_iload = 224;
    public static final int _fast_iload2 = 225;
    public static final int _fast_icaload = 226;
    public static final int _fast_invokevfinal = 227;
    public static final int _fast_linearswitch = 228;
    public static final int _fast_binaryswitch = 229;
    public static final int _fast_aldc = 230;
    public static final int _fast_aldc_w = 231;
    public static final int _return_register_finalizer = 232;
    public static final int _invokehandle = 233;
    public static final int _shouldnotreachhere = 234;
    public static final int number_of_codes = 235;
    static final int _bc_can_trap = 1;
    static final int _bc_can_rewrite = 2;
    static final int _fmt_has_c = 4;
    static final int _fmt_has_j = 8;
    static final int _fmt_has_k = 16;
    static final int _fmt_has_i = 32;
    static final int _fmt_has_o = 64;
    static final int _fmt_has_nbo = 128;
    static final int _fmt_has_u2 = 256;
    static final int _fmt_has_u4 = 512;
    static final int _fmt_not_variable = 1024;
    static final int _fmt_not_simple = 2048;
    static final int _all_fmt_bits = 4092;
    static final int _fmt_b = 1024;
    static final int _fmt_bc = 1028;
    static final int _fmt_bi = 1056;
    static final int _fmt_bkk = 1296;
    static final int _fmt_bJJ = 1416;
    static final int _fmt_bo2 = 1344;
    static final int _fmt_bo4 = 1600;
    private static String[] _name = new String[235];
    private static String[] _format = new String[235];
    private static String[] _wide_format = new String[235];
    private static int[] _result_type = new int[235];
    private static byte[] _depth = new byte[235];
    private static byte[] _lengths = new byte[235];
    private static int[] _java_code = new int[235];
    private static char[] _flags = new char[512];
    private static final int jintSize = 4;

    public Bytecodes() {
    }

    public static int specialLengthAt(Method method, int bci) {
        int code = codeAt(method, bci);
        int alignedBCI;
        int npairs;
        switch (code) {
            case 170:
                alignedBCI = Bits.roundTo(bci + 1, 4);
                npairs = method.getBytecodeIntArg(alignedBCI + 4);
                int hi = method.getBytecodeIntArg(alignedBCI + 8);
                return alignedBCI - bci + (3 + hi - npairs + 1) * 4;
            case 171:
            case 228:
            case 229:
                alignedBCI = Bits.roundTo(bci + 1, 4);
                npairs = method.getBytecodeIntArg(alignedBCI + 4);
                return alignedBCI - bci + (2 + 2 * npairs) * 4;
            case 196:
                return wideLengthFor(method.getBytecodeOrBPAt(bci + 1));
            default:
                throw new RuntimeException("should not reach here");
        }
    }

    public static void check(int code) {
        if (!isDefined(code)) {
            throw new IllegalStateException("illegal code " + code);
        }

    }

    public static void wideCheck(int code) {
        if (!wideIsDefined(code)) {
            throw new IllegalStateException("illegal code " + code);
        }
    }

    public static int codeAt(Method method, int bci) {
        int res = codeOrBPAt(method, bci);
        if (res == 202) {
            res = method.getOrigBytecodeAt(bci);
        }

        return res;
    }

    public static int codeOrBPAt(Method method, int bci) {
        return method.getBytecodeOrBPAt(bci);
    }

    public static boolean isActiveBreakpointAt(Method method, int bci) {
        return codeOrBPAt(method, bci) == 202;
    }

    public static boolean isDefined(int code) {
        return 0 <= code && code < 235 && flags(code, false) != 0;
    }

    public static boolean wideIsDefined(int code) {
        return isDefined(code) && flags(code, true) != 0;
    }

    public static String name(int code) {
        check(code);
        return _name[code];
    }

    public static String format(int code) {
        check(code);
        return _format[code];
    }

    public static String wideFormat(int code) {
        wideCheck(code);
        return _wide_format[code];
    }

    public static int resultType(int code) {
        check(code);
        return _result_type[code];
    }

    public static int depth(int code) {
        check(code);
        return _depth[code];
    }

    public static int lengthFor(int code) {
        check(code);
        return _lengths[code] & 15;
    }

    public static int wideLengthFor(int code) {
        check(code);
        return _lengths[code] >> 4;
    }

    public static boolean canTrap(int code) {
        check(code);
        return has_all_flags(code, 1, false);
    }

    public static int javaCode(int code) {
        check(code);
        return _java_code[code];
    }

    public static boolean canRewrite(int code) {
        check(code);
        return has_all_flags(code, 2, false);
    }

    public static boolean native_byte_order(int code) {
        check(code);
        return has_all_flags(code, 128, false);
    }

    public static boolean uses_cp_cache(int code) {
        check(code);
        return has_all_flags(code, 8, false);
    }

    public static int lengthAt(Method method, int bci) {
        int l = lengthFor(codeAt(method, bci));
        return l > 0 ? l : specialLengthAt(method, bci);
    }

    public static int javaLengthAt(Method method, int bci) {
        int l = lengthFor(javaCode(codeAt(method, bci)));
        return l > 0 ? l : specialLengthAt(method, bci);
    }

    public static boolean isJavaCode(int code) {
        return 0 <= code && code < 203;
    }

    public static boolean isFastCode(int code) {
        return 203 <= code && code < 235;
    }

    public static boolean isAload(int code) {
        return code == 25 || code == 42 || code == 43 || code == 44 || code == 45;
    }

    public static boolean isAstore(int code) {
        return code == 58 || code == 75 || code == 76 || code == 77 || code == 78;
    }

    public static boolean isZeroConst(int code) {
        return code == 1 || code == 3 || code == 11 || code == 14;
    }

    static int flags(int code, boolean is_wide) {
        assert code == (code & 255) : "must be a byte";

        return _flags[code + (is_wide ? 256 : 0)];
    }

    static int format_bits(int code, boolean is_wide) {
        return flags(code, is_wide) & 4092;
    }

    static boolean has_all_flags(int code, int test_flags, boolean is_wide) {
        return (flags(code, is_wide) & test_flags) == test_flags;
    }

    static char compute_flags(String format) {
        return compute_flags(format, 0);
    }

    static char compute_flags(String format, int more_flags) {
        if (format == null) {
            return '\u0000';
        } else {
            int flags = more_flags;
            int fp = 0;
            if (format.isEmpty()) {
                flags |= 2048;
            } else {
                switch (format.charAt(fp)) {
                    case 'b':
                        flags |= 1024;
                        ++fp;
                        break;
                    case 'w':
                        flags |= 3072;
                        ++fp;

                        assert format.charAt(fp) == 'b' : "wide format must start with 'wb'";

                        ++fp;
                }
            }

            boolean has_nbo = false;
            boolean has_jbo = false;
            int has_size = 0;

            while (true) {
                byte this_flag;
                char fc;
                label83:
                while (true) {
                    if (fp >= format.length()) {
                        assert flags == (char) flags : "change _format_flags";

                        return (char) flags;
                    }

                    this_flag = 0;
                    fc = format.charAt(fp++);
                    switch (fc) {
                        case 'C':
                            this_flag = 4;
                            has_nbo = true;
                            break label83;
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case '[':
                        case '\\':
                        case ']':
                        case '^':
                        case '`':
                        case 'a':
                        case 'b':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'l':
                        case 'm':
                        case 'n':
                        default:
                            assert false : "bad char in format";
                            break label83;
                        case 'I':
                            this_flag = 32;
                            has_nbo = true;
                            break label83;
                        case 'J':
                            this_flag = 8;
                            has_nbo = true;
                            break label83;
                        case 'K':
                            this_flag = 16;
                            has_nbo = true;
                            break label83;
                        case 'O':
                            this_flag = 64;
                            has_nbo = true;
                            break label83;
                        case '_':
                            break;
                        case 'c':
                            this_flag = 4;
                            has_jbo = true;
                            break label83;
                        case 'i':
                            this_flag = 32;
                            has_jbo = true;
                            break label83;
                        case 'j':
                            this_flag = 8;
                            has_jbo = true;
                            break label83;
                        case 'k':
                            this_flag = 16;
                            has_jbo = true;
                            break label83;
                        case 'o':
                            this_flag = 64;
                            has_jbo = true;
                            break label83;
                    }
                }

                flags |= this_flag;

                assert !has_jbo || !has_nbo : "mixed byte orders in format";

                if (has_nbo) {
                    flags |= 128;
                }

                int this_size = 1;
                if (fp < format.length() && format.charAt(fp) == fc) {
                    for (this_size = 2; fp + 1 < format.length(); ++this_size) {
                        ++fp;
                        if (format.charAt(fp) != fc) {
                            break;
                        }
                    }

                    switch (this_size) {
                        case 2:
                            flags |= 256;
                            break;
                        case 4:
                            flags |= 512;
                            break;
                        default:
                            assert false : "bad rep count in format";
                    }
                }

                assert has_size == 0 || this_size == has_size || this_size < has_size && fp == format.length() : "mixed field sizes in format";

                has_size = this_size;
            }
        }
    }

    private static void initialize() {
        def(0, "nop", "b", null, BasicType.getTVoid(), 0, false);
        def(1, "aconst_null", "b", null, BasicType.getTObject(), 1, false);
        def(2, "iconst_m1", "b", null, BasicType.getTInt(), 1, false);
        def(3, "iconst_0", "b", null, BasicType.getTInt(), 1, false);
        def(4, "iconst_1", "b", null, BasicType.getTInt(), 1, false);
        def(5, "iconst_2", "b", null, BasicType.getTInt(), 1, false);
        def(6, "iconst_3", "b", null, BasicType.getTInt(), 1, false);
        def(7, "iconst_4", "b", null, BasicType.getTInt(), 1, false);
        def(8, "iconst_5", "b", null, BasicType.getTInt(), 1, false);
        def(9, "lconst_0", "b", null, BasicType.getTLong(), 2, false);
        def(10, "lconst_1", "b", null, BasicType.getTLong(), 2, false);
        def(11, "fconst_0", "b", null, BasicType.getTFloat(), 1, false);
        def(12, "fconst_1", "b", null, BasicType.getTFloat(), 1, false);
        def(13, "fconst_2", "b", null, BasicType.getTFloat(), 1, false);
        def(14, "dconst_0", "b", null, BasicType.getTDouble(), 2, false);
        def(15, "dconst_1", "b", null, BasicType.getTDouble(), 2, false);
        def(16, "bipush", "bc", null, BasicType.getTInt(), 1, false);
        def(17, "sipush", "bcc", null, BasicType.getTInt(), 1, false);
        def(18, "ldc", "bk", null, BasicType.getTIllegal(), 1, true);
        def(19, "ldc_w", "bkk", null, BasicType.getTIllegal(), 1, true);
        def(20, "ldc2_w", "bkk", null, BasicType.getTIllegal(), 2, true);
        def(21, "iload", "bi", "wbii", BasicType.getTInt(), 1, false);
        def(22, "lload", "bi", "wbii", BasicType.getTLong(), 2, false);
        def(23, "fload", "bi", "wbii", BasicType.getTFloat(), 1, false);
        def(24, "dload", "bi", "wbii", BasicType.getTDouble(), 2, false);
        def(25, "aload", "bi", "wbii", BasicType.getTObject(), 1, false);
        def(26, "iload_0", "b", null, BasicType.getTInt(), 1, false);
        def(27, "iload_1", "b", null, BasicType.getTInt(), 1, false);
        def(28, "iload_2", "b", null, BasicType.getTInt(), 1, false);
        def(29, "iload_3", "b", null, BasicType.getTInt(), 1, false);
        def(30, "lload_0", "b", null, BasicType.getTLong(), 2, false);
        def(31, "lload_1", "b", null, BasicType.getTLong(), 2, false);
        def(32, "lload_2", "b", null, BasicType.getTLong(), 2, false);
        def(33, "lload_3", "b", null, BasicType.getTLong(), 2, false);
        def(34, "fload_0", "b", null, BasicType.getTFloat(), 1, false);
        def(35, "fload_1", "b", null, BasicType.getTFloat(), 1, false);
        def(36, "fload_2", "b", null, BasicType.getTFloat(), 1, false);
        def(37, "fload_3", "b", null, BasicType.getTFloat(), 1, false);
        def(38, "dload_0", "b", null, BasicType.getTDouble(), 2, false);
        def(39, "dload_1", "b", null, BasicType.getTDouble(), 2, false);
        def(40, "dload_2", "b", null, BasicType.getTDouble(), 2, false);
        def(41, "dload_3", "b", null, BasicType.getTDouble(), 2, false);
        def(42, "aload_0", "b", null, BasicType.getTObject(), 1, true);
        def(43, "aload_1", "b", null, BasicType.getTObject(), 1, false);
        def(44, "aload_2", "b", null, BasicType.getTObject(), 1, false);
        def(45, "aload_3", "b", null, BasicType.getTObject(), 1, false);
        def(46, "iaload", "b", null, BasicType.getTInt(), -1, true);
        def(47, "laload", "b", null, BasicType.getTLong(), 0, true);
        def(48, "faload", "b", null, BasicType.getTFloat(), -1, true);
        def(49, "daload", "b", null, BasicType.getTDouble(), 0, true);
        def(50, "aaload", "b", null, BasicType.getTObject(), -1, true);
        def(51, "baload", "b", null, BasicType.getTInt(), -1, true);
        def(52, "caload", "b", null, BasicType.getTInt(), -1, true);
        def(53, "saload", "b", null, BasicType.getTInt(), -1, true);
        def(54, "istore", "bi", "wbii", BasicType.getTVoid(), -1, false);
        def(55, "lstore", "bi", "wbii", BasicType.getTVoid(), -2, false);
        def(56, "fstore", "bi", "wbii", BasicType.getTVoid(), -1, false);
        def(57, "dstore", "bi", "wbii", BasicType.getTVoid(), -2, false);
        def(58, "astore", "bi", "wbii", BasicType.getTVoid(), -1, false);
        def(59, "istore_0", "b", null, BasicType.getTVoid(), -1, false);
        def(60, "istore_1", "b", null, BasicType.getTVoid(), -1, false);
        def(61, "istore_2", "b", null, BasicType.getTVoid(), -1, false);
        def(62, "istore_3", "b", null, BasicType.getTVoid(), -1, false);
        def(63, "lstore_0", "b", null, BasicType.getTVoid(), -2, false);
        def(64, "lstore_1", "b", null, BasicType.getTVoid(), -2, false);
        def(65, "lstore_2", "b", null, BasicType.getTVoid(), -2, false);
        def(66, "lstore_3", "b", null, BasicType.getTVoid(), -2, false);
        def(67, "fstore_0", "b", null, BasicType.getTVoid(), -1, false);
        def(68, "fstore_1", "b", null, BasicType.getTVoid(), -1, false);
        def(69, "fstore_2", "b", null, BasicType.getTVoid(), -1, false);
        def(70, "fstore_3", "b", null, BasicType.getTVoid(), -1, false);
        def(71, "dstore_0", "b", null, BasicType.getTVoid(), -2, false);
        def(72, "dstore_1", "b", null, BasicType.getTVoid(), -2, false);
        def(73, "dstore_2", "b", null, BasicType.getTVoid(), -2, false);
        def(74, "dstore_3", "b", null, BasicType.getTVoid(), -2, false);
        def(75, "astore_0", "b", null, BasicType.getTVoid(), -1, false);
        def(76, "astore_1", "b", null, BasicType.getTVoid(), -1, false);
        def(77, "astore_2", "b", null, BasicType.getTVoid(), -1, false);
        def(78, "astore_3", "b", null, BasicType.getTVoid(), -1, false);
        def(79, "iastore", "b", null, BasicType.getTVoid(), -3, true);
        def(80, "lastore", "b", null, BasicType.getTVoid(), -4, true);
        def(81, "fastore", "b", null, BasicType.getTVoid(), -3, true);
        def(82, "dastore", "b", null, BasicType.getTVoid(), -4, true);
        def(83, "aastore", "b", null, BasicType.getTVoid(), -3, true);
        def(84, "bastore", "b", null, BasicType.getTVoid(), -3, true);
        def(85, "castore", "b", null, BasicType.getTVoid(), -3, true);
        def(86, "sastore", "b", null, BasicType.getTVoid(), -3, true);
        def(87, "pop", "b", null, BasicType.getTVoid(), -1, false);
        def(88, "pop2", "b", null, BasicType.getTVoid(), -2, false);
        def(89, "dup", "b", null, BasicType.getTVoid(), 1, false);
        def(90, "dup_x1", "b", null, BasicType.getTVoid(), 1, false);
        def(91, "dup_x2", "b", null, BasicType.getTVoid(), 1, false);
        def(92, "dup2", "b", null, BasicType.getTVoid(), 2, false);
        def(93, "dup2_x1", "b", null, BasicType.getTVoid(), 2, false);
        def(94, "dup2_x2", "b", null, BasicType.getTVoid(), 2, false);
        def(95, "swap", "b", null, BasicType.getTVoid(), 0, false);
        def(96, "iadd", "b", null, BasicType.getTInt(), -1, false);
        def(97, "ladd", "b", null, BasicType.getTLong(), -2, false);
        def(98, "fadd", "b", null, BasicType.getTFloat(), -1, false);
        def(99, "dadd", "b", null, BasicType.getTDouble(), -2, false);
        def(100, "isub", "b", null, BasicType.getTInt(), -1, false);
        def(101, "lsub", "b", null, BasicType.getTLong(), -2, false);
        def(102, "fsub", "b", null, BasicType.getTFloat(), -1, false);
        def(103, "dsub", "b", null, BasicType.getTDouble(), -2, false);
        def(104, "imul", "b", null, BasicType.getTInt(), -1, false);
        def(105, "lmul", "b", null, BasicType.getTLong(), -2, false);
        def(106, "fmul", "b", null, BasicType.getTFloat(), -1, false);
        def(107, "dmul", "b", null, BasicType.getTDouble(), -2, false);
        def(108, "idiv", "b", null, BasicType.getTInt(), -1, true);
        def(109, "ldiv", "b", null, BasicType.getTLong(), -2, true);
        def(110, "fdiv", "b", null, BasicType.getTFloat(), -1, false);
        def(111, "ddiv", "b", null, BasicType.getTDouble(), -2, false);
        def(112, "irem", "b", null, BasicType.getTInt(), -1, true);
        def(113, "lrem", "b", null, BasicType.getTLong(), -2, true);
        def(114, "frem", "b", null, BasicType.getTFloat(), -1, false);
        def(115, "drem", "b", null, BasicType.getTDouble(), -2, false);
        def(116, "ineg", "b", null, BasicType.getTInt(), 0, false);
        def(117, "lneg", "b", null, BasicType.getTLong(), 0, false);
        def(118, "fneg", "b", null, BasicType.getTFloat(), 0, false);
        def(119, "dneg", "b", null, BasicType.getTDouble(), 0, false);
        def(120, "ishl", "b", null, BasicType.getTInt(), -1, false);
        def(121, "lshl", "b", null, BasicType.getTLong(), -1, false);
        def(122, "ishr", "b", null, BasicType.getTInt(), -1, false);
        def(123, "lshr", "b", null, BasicType.getTLong(), -1, false);
        def(124, "iushr", "b", null, BasicType.getTInt(), -1, false);
        def(125, "lushr", "b", null, BasicType.getTLong(), -1, false);
        def(126, "iand", "b", null, BasicType.getTInt(), -1, false);
        def(127, "land", "b", null, BasicType.getTLong(), -2, false);
        def(128, "ior", "b", null, BasicType.getTInt(), -1, false);
        def(129, "lor", "b", null, BasicType.getTLong(), -2, false);
        def(130, "ixor", "b", null, BasicType.getTInt(), -1, false);
        def(131, "lxor", "b", null, BasicType.getTLong(), -2, false);
        def(132, "iinc", "bic", "wbiicc", BasicType.getTVoid(), 0, false);
        def(133, "i2l", "b", null, BasicType.getTLong(), 1, false);
        def(134, "i2f", "b", null, BasicType.getTFloat(), 0, false);
        def(135, "i2d", "b", null, BasicType.getTDouble(), 1, false);
        def(136, "l2i", "b", null, BasicType.getTInt(), -1, false);
        def(137, "l2f", "b", null, BasicType.getTFloat(), -1, false);
        def(138, "l2d", "b", null, BasicType.getTDouble(), 0, false);
        def(139, "f2i", "b", null, BasicType.getTInt(), 0, false);
        def(140, "f2l", "b", null, BasicType.getTLong(), 1, false);
        def(141, "f2d", "b", null, BasicType.getTDouble(), 1, false);
        def(142, "d2i", "b", null, BasicType.getTInt(), -1, false);
        def(143, "d2l", "b", null, BasicType.getTLong(), 0, false);
        def(144, "d2f", "b", null, BasicType.getTFloat(), -1, false);
        def(145, "i2b", "b", null, BasicType.getTByte(), 0, false);
        def(146, "i2c", "b", null, BasicType.getTChar(), 0, false);
        def(147, "i2s", "b", null, BasicType.getTShort(), 0, false);
        def(148, "lcmp", "b", null, BasicType.getTVoid(), -3, false);
        def(149, "fcmpl", "b", null, BasicType.getTVoid(), -1, false);
        def(150, "fcmpg", "b", null, BasicType.getTVoid(), -1, false);
        def(151, "dcmpl", "b", null, BasicType.getTVoid(), -3, false);
        def(152, "dcmpg", "b", null, BasicType.getTVoid(), -3, false);
        def(153, "ifeq", "boo", null, BasicType.getTVoid(), -1, false);
        def(154, "ifne", "boo", null, BasicType.getTVoid(), -1, false);
        def(155, "iflt", "boo", null, BasicType.getTVoid(), -1, false);
        def(156, "ifge", "boo", null, BasicType.getTVoid(), -1, false);
        def(157, "ifgt", "boo", null, BasicType.getTVoid(), -1, false);
        def(158, "ifle", "boo", null, BasicType.getTVoid(), -1, false);
        def(159, "if_icmpeq", "boo", null, BasicType.getTVoid(), -2, false);
        def(160, "if_icmpne", "boo", null, BasicType.getTVoid(), -2, false);
        def(161, "if_icmplt", "boo", null, BasicType.getTVoid(), -2, false);
        def(162, "if_icmpge", "boo", null, BasicType.getTVoid(), -2, false);
        def(163, "if_icmpgt", "boo", null, BasicType.getTVoid(), -2, false);
        def(164, "if_icmple", "boo", null, BasicType.getTVoid(), -2, false);
        def(165, "if_acmpeq", "boo", null, BasicType.getTVoid(), -2, false);
        def(166, "if_acmpne", "boo", null, BasicType.getTVoid(), -2, false);
        def(167, "goto", "boo", null, BasicType.getTVoid(), 0, false);
        def(168, "jsr", "boo", null, BasicType.getTInt(), 0, false);
        def(169, "ret", "bi", "wbii", BasicType.getTVoid(), 0, false);
        def(170, "tableswitch", "", null, BasicType.getTVoid(), -1, false);
        def(171, "lookupswitch", "", null, BasicType.getTVoid(), -1, false);
        def(172, "ireturn", "b", null, BasicType.getTInt(), -1, true);
        def(173, "lreturn", "b", null, BasicType.getTLong(), -2, true);
        def(174, "freturn", "b", null, BasicType.getTFloat(), -1, true);
        def(175, "dreturn", "b", null, BasicType.getTDouble(), -2, true);
        def(176, "areturn", "b", null, BasicType.getTObject(), -1, true);
        def(177, "return", "b", null, BasicType.getTVoid(), 0, true);
        def(178, "getstatic", "bJJ", null, BasicType.getTIllegal(), 1, true);
        def(179, "putstatic", "bJJ", null, BasicType.getTIllegal(), -1, true);
        def(180, "getfield", "bJJ", null, BasicType.getTIllegal(), 0, true);
        def(181, "putfield", "bJJ", null, BasicType.getTIllegal(), -2, true);
        def(182, "invokevirtual", "bJJ", null, BasicType.getTIllegal(), -1, true);
        def(183, "invokespecial", "bJJ", null, BasicType.getTIllegal(), -1, true);
        def(184, "invokestatic", "bJJ", null, BasicType.getTIllegal(), 0, true);
        def(185, "invokeinterface", "bJJ__", null, BasicType.getTIllegal(), -1, true);
        def(186, "invokedynamic", "bJJJJ", null, BasicType.getTIllegal(), 0, true);
        def(187, "new", "bkk", null, BasicType.getTObject(), 1, true);
        def(188, "newarray", "bc", null, BasicType.getTObject(), 0, true);
        def(189, "anewarray", "bkk", null, BasicType.getTObject(), 0, true);
        def(190, "arraylength", "b", null, BasicType.getTVoid(), 0, true);
        def(191, "athrow", "b", null, BasicType.getTVoid(), -1, true);
        def(192, "checkcast", "bkk", null, BasicType.getTObject(), 0, true);
        def(193, "instanceof", "bkk", null, BasicType.getTInt(), 0, true);
        def(194, "monitorenter", "b", null, BasicType.getTVoid(), -1, true);
        def(195, "monitorexit", "b", null, BasicType.getTVoid(), -1, true);
        def(196, "wide", "", null, BasicType.getTVoid(), 0, false);
        def(197, "multianewarray", "bkkc", null, BasicType.getTObject(), 1, true);
        def(198, "ifnull", "boo", null, BasicType.getTVoid(), -1, false);
        def(199, "ifnonnull", "boo", null, BasicType.getTVoid(), -1, false);
        def(200, "goto_w", "boooo", null, BasicType.getTVoid(), 0, false);
        def(201, "jsr_w", "boooo", null, BasicType.getTInt(), 0, false);
        def(202, "breakpoint", "", null, BasicType.getTVoid(), 0, true);
        def(203, "fast_agetfield", "bJJ", null, BasicType.getTObject(), 0, true, 180);
        def(204, "fast_bgetfield", "bJJ", null, BasicType.getTInt(), 0, true, 180);
        def(205, "fast_cgetfield", "bJJ", null, BasicType.getTChar(), 0, true, 180);
        def(206, "fast_dgetfield", "bJJ", null, BasicType.getTDouble(), 0, true, 180);
        def(207, "fast_fgetfield", "bJJ", null, BasicType.getTFloat(), 0, true, 180);
        def(208, "fast_igetfield", "bJJ", null, BasicType.getTInt(), 0, true, 180);
        def(209, "fast_lgetfield", "bJJ", null, BasicType.getTLong(), 0, true, 180);
        def(210, "fast_sgetfield", "bJJ", null, BasicType.getTShort(), 0, true, 180);
        def(211, "fast_aputfield", "bJJ", null, BasicType.getTObject(), 0, true, 181);
        def(212, "fast_bputfield", "bJJ", null, BasicType.getTInt(), 0, true, 181);
        def(213, "fast_zputfield", "bJJ", null, BasicType.getTInt(), 0, true, 181);
        def(214, "fast_cputfield", "bJJ", null, BasicType.getTChar(), 0, true, 181);
        def(215, "fast_dputfield", "bJJ", null, BasicType.getTDouble(), 0, true, 181);
        def(216, "fast_fputfield", "bJJ", null, BasicType.getTFloat(), 0, true, 181);
        def(217, "fast_iputfield", "bJJ", null, BasicType.getTInt(), 0, true, 181);
        def(218, "fast_lputfield", "bJJ", null, BasicType.getTLong(), 0, true, 181);
        def(219, "fast_sputfield", "bJJ", null, BasicType.getTShort(), 0, true, 181);
        def(220, "fast_aload_0", "b", null, BasicType.getTObject(), 1, true, 42);
        def(221, "fast_iaccess_0", "b_JJ", null, BasicType.getTInt(), 1, true, 42);
        def(222, "fast_aaccess_0", "b_JJ", null, BasicType.getTObject(), 1, true, 42);
        def(223, "fast_faccess_0", "b_JJ", null, BasicType.getTObject(), 1, true, 42);
        def(224, "fast_iload", "bi", null, BasicType.getTInt(), 1, false, 21);
        def(225, "fast_iload2", "bi_i", null, BasicType.getTInt(), 2, false, 21);
        def(226, "fast_icaload", "bi_", null, BasicType.getTInt(), 0, false, 21);
        def(227, "fast_invokevfinal", "bJJ", null, BasicType.getTIllegal(), -1, true, 182);
        def(228, "fast_linearswitch", "", null, BasicType.getTVoid(), -1, false, 171);
        def(229, "fast_binaryswitch", "", null, BasicType.getTVoid(), -1, false, 171);
        def(230, "fast_aldc", "bj", null, BasicType.getTObject(), 1, true, 18);
        def(231, "fast_aldc_w", "bJJ", null, BasicType.getTObject(), 1, true, 19);
        def(232, "return_register_finalizer", "b", null, BasicType.getTVoid(), 0, true, 177);
        def(233, "invokehandle", "bJJ", null, BasicType.getTIllegal(), -1, true, 182);
        def(234, "_shouldnotreachhere", "b", null, BasicType.getTVoid(), 0, false);

    }

    private static void def(int code, String name, String format, String wide_format, int result_type, int depth, boolean can_trap) {
        def(code, name, format, wide_format, result_type, depth, can_trap, code);
    }

    private static void def(int code, String name, String format, String wide_format, int result_type, int depth, boolean can_trap, int java_code) {
        if (!(wide_format == null || format != null)) {
            throw new IllegalStateException("short form must exist if there's a wide form");
        }

        int len = format != null ? format.length() : 0;
        int wlen = wide_format != null ? wide_format.length() : 0;
        _name[code] = name;
        _result_type[code] = result_type;
        _depth[code] = (byte) depth;
        _lengths[code] = (byte) (wlen << 4 | len & 15);
        _java_code[code] = java_code;
        _format[code] = format;
        _wide_format[code] = wide_format;
        int bc_flags = 0;
        if (can_trap) {
            bc_flags |= 1;
        }

        if (java_code != code) {
            bc_flags |= 2;
        }

        _flags[code] = compute_flags(format, bc_flags);
        _flags[code + 256] = compute_flags(wide_format, bc_flags);
    }

    static {
        initialize();
    }
}
