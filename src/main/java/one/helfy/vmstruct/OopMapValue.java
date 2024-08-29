package one.helfy.vmstruct;

import one.helfy.JVM;

/**
 * @author aleksei.gromov
 * @date 17.05.2018
 */
public class OopMapValue {
    private static final JVM jvm = JVM.getInstance();
    public static final int TYPE_BITS = JVM.intConstant("OopMapValue::type_bits");
    public static final int REGISTER_BITS = JVM.intConstant("OopMapValue::register_bits");
    public static final int TYPE_SHIFT = JVM.intConstant("OopMapValue::type_shift");
    public static final int REGISTER_SHIFT = JVM.intConstant("OopMapValue::register_shift");
    public static final int TYPE_MASK = JVM.intConstant("OopMapValue::type_mask");
    public static final int TYPE_MASK_IN_PLACE = JVM.intConstant("OopMapValue::type_mask_in_place");
    public static final int REGISTER_MASK = JVM.intConstant("OopMapValue::register_mask");
    public static final int REGISTER_MASK_IN_PLACE = JVM.intConstant("OopMapValue::register_mask_in_place");
    public static final int UNUSED_VALUE = JVM.intConstant("OopMapValue::unused_value");
    public static final int OOP_VALUE = JVM.intConstant("OopMapValue::oop_value");
    public static final int VALUE_VALUE = JVM.intConstant("OopMapValue::value_value");
    public static final int NARROWOOP_VALUE = JVM.intConstant("OopMapValue::narrowoop_value");
    public static final int DERIVED_OOP_VALUE = JVM.intConstant("OopMapValue::derived_oop_value");
    public static final int CALLEE_SAVED_VALUE = JVM.intConstant("OopMapValue::callee_saved_value");

    private final short val;
    private final int regNum;
    private int contentRegNum;
    private final int type;

    public OopMapValue(CompressedReadStream crs) {
        val = (short) crs.readInt();
        type = val & TYPE_MASK_IN_PLACE;
        if (type == CALLEE_SAVED_VALUE || type == DERIVED_OOP_VALUE) {
            contentRegNum = crs.readInt();
        }
        regNum = (val & REGISTER_MASK_IN_PLACE) >> REGISTER_SHIFT;
    }

    public short getVal() {
        return val;
    }

    public int getRegNum() {
        return regNum;
    }

    public int getContentRegNum() {
        return contentRegNum;
    }

    public int getType() {
        return type;
    }
}
