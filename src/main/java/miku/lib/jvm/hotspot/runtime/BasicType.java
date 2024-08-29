package miku.lib.jvm.hotspot.runtime;

public class BasicType {
    public static final int tBoolean = 4;
    public static final int tChar = 5;
    public static final int tFloat = 6;
    public static final int tDouble = 7;
    public static final int tByte = 8;
    public static final int tShort = 9;
    public static final int tInt = 10;
    public static final int tLong = 11;
    public static final int tObject = 12;
    public static final int tArray = 13;
    public static final int tVoid = 14;
    public static final int tAddress = 15;
    public static final int tConflict = 16;
    public static final int tIllegal = 99;
    public static final BasicType T_BOOLEAN = new BasicType(4);
    public static final BasicType T_CHAR = new BasicType(5);
    public static final BasicType T_FLOAT = new BasicType(6);
    public static final BasicType T_DOUBLE = new BasicType(7);
    public static final BasicType T_BYTE = new BasicType(8);
    public static final BasicType T_SHORT = new BasicType(9);
    public static final BasicType T_INT = new BasicType(10);
    public static final BasicType T_LONG = new BasicType(11);
    public static final BasicType T_OBJECT = new BasicType(12);
    public static final BasicType T_ARRAY = new BasicType(13);
    public static final BasicType T_VOID = new BasicType(14);
    public static final BasicType T_ADDRESS = new BasicType(15);
    public static final BasicType T_CONFLICT = new BasicType(16);
    public static final BasicType T_ILLEGAL = new BasicType(99);
    private int type;

    private BasicType(int type) {
        this.type = type;
    }

    public static int getTBoolean() {
        return 4;
    }

    public static int getTChar() {
        return 5;
    }

    public static int getTFloat() {
        return 6;
    }

    public static int getTDouble() {
        return 7;
    }

    public static int getTByte() {
        return 8;
    }

    public static int getTShort() {
        return 9;
    }

    public static int getTInt() {
        return 10;
    }

    public static int getTLong() {
        return 11;
    }

    public static int getTObject() {
        return 12;
    }

    public static int getTArray() {
        return 13;
    }

    public static int getTVoid() {
        return 14;
    }

    public static int getTAddress() {
        return 15;
    }

    public static int getTConflict() {
        return 16;
    }

    public static int getTIllegal() {
        return 99;
    }

    public static BasicType charToBasicType(char c) {
        switch (c) {
            case 'B':
                return T_BYTE;
            case 'C':
                return T_CHAR;
            case 'D':
                return T_DOUBLE;
            case 'E':
            case 'G':
            case 'H':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'W':
            case 'X':
            case 'Y':
            default:
                return T_ILLEGAL;
            case 'F':
                return T_FLOAT;
            case 'I':
                return T_INT;
            case 'J':
                return T_LONG;
            case 'L':
                return T_OBJECT;
            case 'S':
                return T_SHORT;
            case 'V':
                return T_VOID;
            case 'Z':
                return T_BOOLEAN;
            case '[':
                return T_ARRAY;
        }
    }

    public static int charToType(char c) {
        return charToBasicType(c).getType();
    }

    public int getType() {
        return this.type;
    }
}
