package one.helfy;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.xdark.shell.JVMUtil;
import me.xdark.shell.NativeLibrary;
import miku.lib.HSDB.HSDB;
import miku.lib.HSDB.SaJDI;
import miku.lib.utils.InternalUtils;
import sun.misc.Unsafe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public final class JVM {
    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    private static final NativeLibrary JVM;
    private static final boolean linux = System.getProperty("os.name").trim().toLowerCase().contains("linux");
    private static JVM jvm;

    public static void InitVtableCaches() {
        if (!linux) {
            return;
        }
        SaJDI.appendJar();
        Type[] targets = new Type[]{type("MethodData"), type("InstanceRefKlass"), type("InstanceMirrorKlass"), type("ConstantPool"), type("Method"), type("ObjArrayKlass"), type("ArrayKlass"), type("TypeArrayKlass"), type("Metadata"), type("InstanceKlass"), type("Klass"), type("InstanceClassLoaderKlass")};
        HSDB.getMultiply("TempFile", targets);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile("TempFile", "rw")) {
            for (Type type : targets) {
                long vtbl = randomAccessFile.readLong();
                System.out.println(type.name);
                System.out.println(vtbl);
                vtbl = useGCC32ABI ? vtbl + 2L * unsafe.addressSize() : vtbl;
                typeToVtbl.put(type, vtbl);
                typeToVtblMap.put(type, vtbl);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            JVM = JVMUtil.findJvm();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private static final Map<Type, Long> typeToVtblMap = new HashMap<>();
    private static final Object2LongOpenHashMap<Type> typeToVtbl = new Object2LongOpenHashMap<>();
    private static final Map<String, Type> types = new LinkedHashMap<>();
    private static final Map<String, Number> constants = new LinkedHashMap<>();
    private static String vt;

    private static final boolean useGCC32ABI;

    public static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

    static {
        readVmTypes(readVmStructs());
        readVmIntConstants();
        readVmLongConstants();
        if (!WINDOWS) {
            useGCC32ABI = (HSDB.getSymbol("__vt_10JavaThread") == 0L);
        } else {
            useGCC32ABI = false;
        }
    }

    private JVM() {
        if (linux) {
            vt = useGCC32ABI ? "_ZTV" : "__vt_";
        }
    }

    public static JVM getInstance() {
        if (jvm == null) {
            jvm = new JVM();
        }
        return jvm;
    }

    public static String vtblSymbolForType(Type type) {
        if (linux) {
            return vt + type.name.length() + type.name;
        } else {
            return "??_7" + type.name + "@@6B@";
        }
    }

    public static long getVtblForType(Type type) {
        if (type == null) {
            return 0;
        } else {
            if (typeToVtblMap.containsKey(type)) {
                return typeToVtblMap.get(type);
            } else {
                if (linux) {
                    long raw = HSDB.getSymbol(type);
                    return useGCC32ABI ? raw + 2L * unsafe.addressSize() : raw;
                } else {
                    String vtblSymbol = vtblSymbolForType(type);
                    try {
                        long addr = getSymbol(vtblSymbol);
                        typeToVtblMap.put(type, addr);
                        return addr;
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                        typeToVtblMap.put(type, 0L);
                        return 0;
                    }
                }
            }
        }
    }

    public static long vtblForType(Type type) {
        if (!typeToVtbl.containsKey(type)) {
            long vtblAddr = typeToVtbl.getLong(type);
            if (vtblAddr == 0) {
                vtblAddr = getVtblForType(type);
                if (vtblAddr != 0) {
                    typeToVtbl.put(type, vtblAddr);
                }
            }

            return vtblAddr;
        }
        return typeToVtbl.getLong(type);
    }

    public static Type findDynamicTypeForAddress(long addr, Type baseType) {
        if (vtblForType(baseType) == 0) {
            throw new InternalError(baseType + " does not appear to be polymorphic");
        } else {
            long loc1 = unsafe.getAddress(addr);
            long loc2 = 0;
            long loc3 = 0;
            long offset2 = baseType.size;
            offset2 = offset2 - offset2 % unsafe.pageSize() - unsafe.pageSize();
            if (offset2 > 0L) {
                loc2 = unsafe.getAddress(addr + offset2);
            }
            long offset3 = offset2 - unsafe.pageSize();
            if (offset3 > 0L) {
                loc3 = unsafe.getAddress(addr + offset3);
            }


            Type loc2Match = null;
            Type loc3Match = null;
            Iterator<Type> iter = getTypes();

            while (iter.hasNext()) {
                Type type = iter.next();
                Type superClass;
                for (superClass = type; !Objects.equals(superClass, baseType) && superClass != null; superClass = type(superClass.superName)) {
                }
                if (superClass != null) {
                    long vtblAddr = vtblForType(type);
                    if (vtblAddr != 0) {
                        if (vtblAddr == loc1) {
                            return type;
                        }

                        if (loc2 != 0 && loc2Match == null && vtblAddr == (loc2)) {
                            loc2Match = type;
                        }

                        if (loc3 != 0 && loc3Match == null && vtblAddr == (loc3)) {
                            loc3Match = type;
                        }
                    }
                }
            }

            if (loc2Match != null) {
                return loc2Match;
            } else return loc3Match;
        }
    }

    public static Iterator<Type> getTypes() {
        return types.values().iterator();
    }

    public static void print(boolean Types, boolean Constants, boolean fields) {
        if (Types) {
            types.forEach((s, v) -> {
                System.out.println(s);
                System.out.println("Super:" + v.superName);
                if (fields) {
                    System.out.println("Fields:" + Arrays.toString(v.fields));
                }
                System.out.println("vtbl" + vtblForType(v));
                System.out.println();
            });
        }
        if (Constants) {
            constants.forEach((s, v) -> {
                System.out.println(s);
                System.out.println();
            });
        }
    }

    public static <T> T getObject(T obj, long addr) {
        return (T) unsafe.getObject(obj, addr);
    }

    public static long fieldOffset(java.lang.reflect.Field field) {
        return unsafe.objectFieldOffset(field);
    }

    public static Type findDynamicTypeForAddress(long addr, String type) {
        return findDynamicTypeForAddress(addr, type(type));
    }

    private static Map<String, Set<Field>> readVmStructs() {
        long entry = getSymbol("gHotSpotVMStructs");
        long typeNameOffset = getSymbol("gHotSpotVMStructEntryTypeNameOffset");
        long fieldNameOffset = getSymbol("gHotSpotVMStructEntryFieldNameOffset");
        long typeStringOffset = getSymbol("gHotSpotVMStructEntryTypeStringOffset");
        long isStaticOffset = getSymbol("gHotSpotVMStructEntryIsStaticOffset");
        long offsetOffset = getSymbol("gHotSpotVMStructEntryOffsetOffset");
        long addressOffset = getSymbol("gHotSpotVMStructEntryAddressOffset");
        long arrayStride = getSymbol("gHotSpotVMStructEntryArrayStride");

        Map<String, Set<Field>> structs = new HashMap<>();

        for (; ; entry += arrayStride) {
            String typeName = getStringRef(entry + typeNameOffset);
            String fieldName = getStringRef(entry + fieldNameOffset);
            if (fieldName == null) break;

            String typeString = getStringRef(entry + typeStringOffset);
            boolean isStatic = getInt(entry + isStaticOffset) != 0;
            long offset = getLong(entry + (isStatic ? addressOffset : offsetOffset));

            Set<Field> fields = structs.computeIfAbsent(typeName, k -> new TreeSet<>());
            fields.add(new Field(fieldName, typeString, offset, isStatic));
        }

        return structs;
    }

    private static void readVmTypes(Map<String, Set<Field>> structs) {
        long entry = getSymbol("gHotSpotVMTypes");
        long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
        long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
        long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
        long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
        long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
        long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
        long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");

        for (; ; entry += arrayStride) {
            String typeName = getStringRef(entry + typeNameOffset);
            if (typeName == null) break;

            String superclassName = getStringRef(entry + superclassNameOffset);
            boolean isOop = getInt(entry + isOopTypeOffset) != 0;
            boolean isInt = getInt(entry + isIntegerTypeOffset) != 0;
            boolean isUnsigned = getInt(entry + isUnsignedOffset) != 0;
            int size = getInt(entry + sizeOffset);

            Set<Field> fields = structs.get(typeName);
            types.put(typeName, new Type(typeName, superclassName, size, isOop, isInt, isUnsigned, fields));
        }
    }

    private static void readVmIntConstants() {
        long entry = getSymbol("gHotSpotVMIntConstants");
        long nameOffset = getSymbol("gHotSpotVMIntConstantEntryNameOffset");
        long valueOffset = getSymbol("gHotSpotVMIntConstantEntryValueOffset");
        long arrayStride = getSymbol("gHotSpotVMIntConstantEntryArrayStride");

        for (; ; entry += arrayStride) {
            String name = getStringRef(entry + nameOffset);
            if (name == null) break;

            int value = getInt(entry + valueOffset);
            constants.put(name, value);
        }
    }

    private static void readVmLongConstants() {
        long entry = getSymbol("gHotSpotVMLongConstants");
        long nameOffset = getSymbol("gHotSpotVMLongConstantEntryNameOffset");
        long valueOffset = getSymbol("gHotSpotVMLongConstantEntryValueOffset");
        long arrayStride = getSymbol("gHotSpotVMLongConstantEntryArrayStride");

        for (; ; entry += arrayStride) {
            String name = getStringRef(entry + nameOffset);
            if (name == null) break;

            long value = getLong(entry + valueOffset);
            constants.put(name, value);
        }
    }

    public static byte getByte(long addr) {
        return unsafe.getByte(addr);
    }

    public void putByte(long addr, byte val) {
        unsafe.putByte(addr, val);
    }

    public static short getShort(long addr) {
        return unsafe.getShort(addr);
    }

    public void putShort(long addr, short val) {
        unsafe.putShort(addr, val);
    }

    public static int getInt(long addr) {
        return unsafe.getInt(addr);
    }

    public void putInt(long addr, int val) {
        unsafe.putInt(addr, val);
    }

    public static long getLong(long addr) {
        return unsafe.getLong(addr);
    }

    public void putLong(long addr, long val) {
        unsafe.putLong(addr, val);
    }

    public static long getAddress(long addr) {
        return unsafe.getAddress(addr);
    }

    public static void putAddress(long addr, long val) {
        unsafe.putAddress(addr, val);
    }

    public static String getString(long addr) {
        if (addr == 0) {
            return null;
        }

        char[] chars = new char[40];
        int offset = 0;
        for (byte b; (b = getByte(addr + offset)) != 0; ) {
            if (offset >= chars.length) chars = Arrays.copyOf(chars, offset * 2);
            chars[offset++] = (char) b;
        }
        return new String(chars, 0, offset);
    }

    public static String getStringRef(long addr) {
        return getString(getAddress(addr));
    }


    public static long getSymbol(String name) {
        long address = JVM.findEntry(name);
        if (address == 0) {
            if (!WINDOWS) {
                address = HSDB.getSymbol(name);
            }
            if (address == 0) {
                throw new NoSuchElementException("No such symbol: " + name);
            }
            return address;
        }
        if (name.startsWith("??_7") && name.endsWith("@@6B@")) {
            return address;
        }
        return getLong(address);
    }

    public static Type type(String name) {
        //throw new NoSuchElementException("No such type: " + name);
        return types.get(name);
    }

    public static Number constant(String name) {
        Number constant = constants.get(name);
        if (constant == null) {
            throw new NoSuchElementException("No such constant: " + name);
        }
        return constant;
    }

    public static int intConstant(String name) {
        return constant(name).intValue();
    }

    public static long longConstant(String name) {
        return constant(name).longValue();
    }

    /*
     * This class was tested under permanent System.gc calls and doesn't seem to crash JVM due to object relocations
     */
    public static class Ptr2Obj {
        private static final long _narrow_oop_base = getAddress(type("Universe").global("_narrow_oop._base"));
        private static final int _narrow_oop_shift = getInt(type("Universe").global("_narrow_oop._shift"));
        private static final long objFieldOffset;

        static {
            try {
                java.lang.reflect.Field objField = Ptr2Obj.class.getDeclaredField("obj");
                objFieldOffset = fieldOffset(objField);
            } catch (NoSuchFieldException e) {
                throw new JVMException("Couldn't obtain obj field of own class");
            }
        }

        private volatile Object obj;

        public static Object getFromPtr2Ptr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) ((getAddress(address) - _narrow_oop_base) >> _narrow_oop_shift));
            return ptr2Obj.obj;
        }

        public static Object getFromPtr2NarrowPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) getAddress(address));
            return ptr2Obj.obj;
        }

        public static int narrowKlassAddress(long address) {
            return (int) ((address - _narrow_oop_base) >> _narrow_oop_shift);
        }

        public static Object getFromPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) ((address - _narrow_oop_base) >> _narrow_oop_shift));
            return ptr2Obj.obj;
        }

        public static Object getFromNarrowPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) address);
            return ptr2Obj.obj;
        }
    }
}
