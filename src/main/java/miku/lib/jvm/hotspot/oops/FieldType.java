package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.BasicType;

public class FieldType {
    private Symbol signature;
    private char first;

    public FieldType(Symbol signature) {
        this.signature = signature;
        this.first = (char) signature.getByteAt(0L);
        switch (this.first) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'L':
            case 'S':
            case 'Z':
            case '[':
                break;
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
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            default:
                throw new IllegalArgumentException("\"Unknown char in field signature \\\"\" + signature.asString() + \"\\\": \" + this.first");
        }
    }


    public boolean isOop() {
        return this.isObject() || this.isArray();
    }

    public boolean isByte() {
        return this.first == 'B';
    }

    public boolean isChar() {
        return this.first == 'C';
    }

    public boolean isDouble() {
        return this.first == 'D';
    }

    public boolean isFloat() {
        return this.first == 'F';
    }

    public boolean isInt() {
        return this.first == 'I';
    }

    public boolean isLong() {
        return this.first == 'J';
    }

    public boolean isShort() {
        return this.first == 'S';
    }

    public boolean isBoolean() {
        return this.first == 'Z';
    }

    public boolean isObject() {
        return this.first == 'L';
    }

    public boolean isArray() {
        return this.first == '[';
    }

    public Symbol getSignature() {
        return this.signature;
    }

    public ArrayInfo getArrayInfo() {
        int index = 1;
        int dim = 1;
        index = this.skipOptionalSize(this.signature, index);

        while (this.signature.getByteAt((long) index) == 91) {
            ++index;
            ++dim;
            this.skipOptionalSize(this.signature, index);
        }

        int elementType = BasicType.charToType((char) this.signature.getByteAt((long) index));
        return new FieldType.ArrayInfo(dim, elementType);
    }


    private int skipOptionalSize(Symbol sig, int index) {
        for (byte c = sig.getByteAt(index); c >= 48 && c <= 57; c = sig.getByteAt((long) index)) {
            ++index;
        }

        return index;
    }

    public static class ArrayInfo {
        private int dimension;
        private int elementBasicType;

        public ArrayInfo(int dimension, int elementBasicType) {
            this.dimension = dimension;
            this.elementBasicType = elementBasicType;
        }

        public int dimension() {
            return this.dimension;
        }

        public int elementBasicType() {
            return this.elementBasicType;
        }
    }
}
