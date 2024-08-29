package one.helfy.vmstruct.scope;

import one.helfy.JVM;

import java.util.Map;

import static one.helfy.vmstruct.scope.Location.Type.OOP;
import static one.helfy.vmstruct.scope.Location.Where.IN_REGISTER;
import static one.helfy.vmstruct.scope.Location.Where.ON_STACK;

/**
 * @author aleksei.gromov
 * @date 26.04.2018
 */
public class Location {
    private static final JVM jvm = JVM.getInstance();
    private static final int INT_SIZE = JVM.type("jint").size;
    private static final long _narrow_oop_base = JVM.getAddress(JVM.type("Universe").global("_narrow_oop._base"));
    private static final int _narrow_oop_shift = JVM.getInt(JVM.type("Universe").global("_narrow_oop._shift"));
    private static final int OFFSET_MASK = JVM.intConstant("Location::OFFSET_MASK");
    private static final int OFFSET_SHIFT = JVM.intConstant("Location::OFFSET_SHIFT");
    private static final int TYPE_MASK = JVM.intConstant("Location::TYPE_MASK");
    private static final int TYPE_SHIFT = JVM.intConstant("Location::TYPE_SHIFT");
    private static final int WHERE_MASK = JVM.intConstant("Location::WHERE_MASK");
    private static final int WHERE_SHIFT = JVM.intConstant("Location::WHERE_SHIFT");
    private static final int TYPE_NORMAL = JVM.intConstant("Location::normal");
    private static final int TYPE_OOP = JVM.intConstant("Location::oop");
    private static final int TYPE_NARROWOOP = JVM.intConstant("Location::narrowoop");
    private static final int TYPE_INT_IN_LONG = JVM.intConstant("Location::int_in_long");
    private static final int TYPE_LNG = JVM.intConstant("Location::lng");
    private static final int TYPE_FLOAT_IN_DBL = JVM.intConstant("Location::float_in_dbl");
    private static final int TYPE_DBL = JVM.intConstant("Location::dbl");
    private static final int TYPE_ADDR = JVM.intConstant("Location::addr");
    private static final int TYPE_INVALID = JVM.intConstant("Location::invalid");
    private static final int WHERE_ON_STACK = JVM.intConstant("Location::on_stack");
    private static final int WHERE_IN_REGISTER = JVM.intConstant("Location::in_register");
    private final int value;

    Location(int value) {
        this.value = value;
    }

    public Location.Where getWhere() {
        int where = (this.value & WHERE_MASK) >> WHERE_SHIFT;
        if (where == WHERE_ON_STACK) {
            return ON_STACK;
        } else if (where == WHERE_IN_REGISTER) {
            return Location.Where.IN_REGISTER;
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public boolean isRegister() {
        return ((this.value & WHERE_MASK) >> WHERE_SHIFT) == WHERE_IN_REGISTER;
    }

    public Location.Type getType() {
        int type = (this.value & TYPE_MASK) >> TYPE_SHIFT;
        if (type == TYPE_NORMAL) {
            return Location.Type.NORMAL;
        } else if (type == TYPE_OOP) {
            return OOP;
        } else if (type == TYPE_NARROWOOP) {
            return Location.Type.NARROWOOP;
        } else if (type == TYPE_INT_IN_LONG) {
            return Location.Type.INT_IN_LONG;
        } else if (type == TYPE_LNG) {
            return Location.Type.LNG;
        } else if (type == TYPE_FLOAT_IN_DBL) {
            return Location.Type.FLOAT_IN_DBL;
        } else if (type == TYPE_DBL) {
            return Location.Type.DBL;
        } else if (type == TYPE_ADDR) {
            return Location.Type.ADDR;
        } else if (type == TYPE_INVALID) {
            return Location.Type.INVALID;
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public short getOffset() {
        return (short) ((this.value & OFFSET_MASK) >> OFFSET_SHIFT);
    }

    public Object toObject(long unextendedSP, Map<Integer, Long> registers) {
        int type = (this.value & TYPE_MASK) >> TYPE_SHIFT;
        if (type == TYPE_INVALID) {
            return null;
        }
        Where where = getWhere();

        if (where == Where.ON_STACK) {
            long locationAddress = unextendedSP + (long) INT_SIZE * getOffset();
            Object normalVal = getObjectInternal(type, locationAddress);
            if (normalVal != null) {
                return normalVal;
            }
        } else {
            int regNum = getOffset();
            if (registers != null && registers.containsKey(regNum)) {
                Object normalVal = getObjectInternal(type, registers.get(regNum));
                if (normalVal != null) {
                    return normalVal;
                }
            }
        }
        return toString();
    }

    private Object getObjectInternal(int type, long locationAddress) {
        if (type == TYPE_OOP) {
            return JVM.Ptr2Obj.getFromPtr2Ptr(locationAddress);
        } else if (type == TYPE_NARROWOOP) {
            return JVM.Ptr2Obj.getFromPtr2NarrowPtr(locationAddress);
        } else if (type == TYPE_NORMAL) {
            // in 32-bit JVM normal also means half of double or half of long
            int normalVal = JVM.getInt(locationAddress);
            return normalVal;
        } else if (type == TYPE_DBL || type == TYPE_FLOAT_IN_DBL) {
            long dblBits = JVM.getLong(locationAddress);
            return Double.longBitsToDouble(dblBits);
        } else if (type == TYPE_LNG || type == TYPE_INT_IN_LONG) {
            return JVM.getLong(locationAddress);
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value + " (" + getType().toString() + ")"
                + (getWhere() == IN_REGISTER ? " in register " + getOffset() : "");
    }

    public static class Type {
        public static final Type NORMAL = new Type("normal");
        public static final Type OOP = new Type("oop");
        public static final Type NARROWOOP = new Type("narrowoop");
        public static final Type INT_IN_LONG = new Type("int_in_long");
        public static final Type LNG = new Type("lng");
        public static final Type FLOAT_IN_DBL = new Type("float_in_dbl");
        public static final Type DBL = new Type("dbl");
        public static final Type ADDR = new Type("addr");
        public static final Type INVALID = new Type("invalid");
        private final String value;

        private Type(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

        public int getValue() {
            if (this == NORMAL) {
                return TYPE_NORMAL;
            } else if (this == OOP) {
                return TYPE_OOP;
            } else if (this == NARROWOOP) {
                return TYPE_NARROWOOP;
            } else if (this == INT_IN_LONG) {
                return TYPE_INT_IN_LONG;
            } else if (this == LNG) {
                return TYPE_LNG;
            } else if (this == FLOAT_IN_DBL) {
                return Location.TYPE_FLOAT_IN_DBL;
            } else if (this == DBL) {
                return Location.TYPE_DBL;
            } else if (this == ADDR) {
                return Location.TYPE_ADDR;
            } else if (this == INVALID) {
                return Location.TYPE_INVALID;
            } else {
                throw new RuntimeException("should not reach here");
            }
        }
    }

    public static class Where {
        public static final Where ON_STACK = new Where("on_stack");
        public static final Where IN_REGISTER = new Where("in_register");
        private final String value;

        private Where(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

        public int getValue() {
            if (this == ON_STACK) {
                return WHERE_ON_STACK;
            } else if (this == IN_REGISTER) {
                return Location.WHERE_IN_REGISTER;
            } else {
                throw new RuntimeException("should not reach here");
            }
        }
    }
}
