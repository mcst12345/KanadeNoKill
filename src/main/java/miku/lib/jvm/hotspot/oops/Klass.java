package miku.lib.jvm.hotspot.oops;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

public class Klass extends Metadata {


    private static final Long2ObjectOpenHashMap<Klass> cachedKlass = new Long2ObjectOpenHashMap<>();
    private static final long _layout_helper_offset;
    private static final long _name_offset;
    private static final long _access_flags_offset;
    private static final long _modifier_flags_offset;
    private static final long _java_mirror_offset;
    private static final long _super_offset;
    private Symbol name;
    private AccessFlags _access_flags;
    private Oop _java_mirror;

    static {
        Type type = JVM.type("Klass");
        _layout_helper_offset = type.offset("_layout_helper");
        _name_offset = type.offset("_name");
        _access_flags_offset = type.offset("_access_flags");
        _modifier_flags_offset = type.offset("_modifier_flags");
        _java_mirror_offset = type.offset("_java_mirror");
        _super_offset = type.offset("_super");
    }

    public Klass(long address) {
        super(address);
        name = new Symbol(unsafe.getAddress(address + _name_offset));
        _access_flags = new AccessFlags(address + _access_flags_offset);
        _java_mirror = new Oop(unsafe.getAddress(address + _java_mirror_offset));
    }

    Klass(Class<?> clazz) {
        this(JVM.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
    }

    public int getModifierFlags() {
        return unsafe.getInt(getAddress() + _modifier_flags_offset);
    }

    public int getLayoutHelper() {
        return unsafe.getInt(getAddress() + _layout_helper_offset);
    }

    public static Klass getKlass(long address) {
        if (cachedKlass.containsKey(address)) {
            return cachedKlass.get(address);
        }

        Klass klass = (Klass) Metadata.instantiateWrapperFor(address);
        cachedKlass.put(address, klass);
        return klass;
    }

    public static Klass getKlass(Class<?> clazz) {
        long addr = JVM.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) unsafe.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL;
        return getKlass(addr);
    }

    public Oop getMirror() {
        return _java_mirror;
    }

    public Klass getSuper() {
        long address = unsafe.getAddress(getAddress() + _super_offset);
        return address == 0 ? null : getKlass(address);
    }

    public void setSuper(Klass klass) {
        unsafe.putAddress(getAddress() + _super_offset, klass.getAddress());
    }

    public void setSuper(Class<?> clazz) {
        setSuper(getKlass(clazz));
    }

    public AccessFlags getAccessFlags() {
        return _access_flags;
    }

    public void setAccessFlags(int flags) {
        long _modifier_flags_offset = JVM.type("Klass").offset("_modifier_flags");
        unsafe.putInt(getAddress() + _modifier_flags_offset, flags);
        _access_flags.setFlags(flags);
    }


    public String getName() {
        return name.toString();
    }

    public Symbol getSymbol() {
        return name;
    }

    public void setName(String str) {
        Unsafe unsafe = InternalUtils.getUnsafe();
        Symbol symbol = new Symbol(str);
        unsafe.putAddress(getAddress() + _name_offset, symbol.getAddress());
        this.name = symbol;
    }

}
