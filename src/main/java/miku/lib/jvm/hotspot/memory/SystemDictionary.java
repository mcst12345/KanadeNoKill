package miku.lib.jvm.hotspot.memory;

import miku.lib.utils.InternalUtils;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Oop;
import one.helfy.JVM;
import one.helfy.Type;

public class SystemDictionary {
    private static long dictionaryField;
    private static long sharedDictionaryField;
    private static long placeholdersField;
    private static long loaderConstraintTableField;
    private static Oop javaSystemLoaderField;
    private static long objectKlassField;
    private static long classLoaderKlassField;
    private static long stringKlassField;
    private static long systemKlassField;
    private static long threadKlassField;
    private static long threadGroupKlassField;
    private static long methodHandleKlassField;
    private static Dictionary dictionary;

    static {
        Type type = JVM.type("SystemDictionary");
        dictionaryField = type.global("_dictionary");
        sharedDictionaryField = type.global("_shared_dictionary");
        placeholdersField = type.global("_placeholders");
        loaderConstraintTableField = type.global("_loader_constraints");
        javaSystemLoaderField = new Oop(InternalUtils.getUnsafe().getAddress(type.global("_java_system_loader")));
        objectKlassField = type.global(WK_KLASS("Object_klass"));
        classLoaderKlassField = type.global(WK_KLASS("ClassLoader_klass"));
        stringKlassField = type.global(WK_KLASS("String_klass"));
        systemKlassField = type.global(WK_KLASS("System_klass"));
        threadKlassField = type.global(WK_KLASS("Thread_klass"));
        threadGroupKlassField = type.global(WK_KLASS("ThreadGroup_klass"));
        methodHandleKlassField = type.global(WK_KLASS("MethodHandle_klass"));
    }

    private static String WK_KLASS(String name) {
        return "_well_known_klasses[SystemDictionary::" + WK_KLASS_ENUM_NAME(name) + "]";
    }

    private static String WK_KLASS_ENUM_NAME(String kname) {
        return kname + "_knum";
    }

    public static Dictionary getDictionary() {
        if (dictionary == null) {
            dictionary = new Dictionary(InternalUtils.getUnsafe().getAddress(dictionaryField));
        }
        return dictionary;
    }

    public interface ClassAndLoaderVisitor {
        void visit(Klass var1, Oop var2);
    }

    public interface ClassVisitor {
        void visit(Klass var1);
    }

}
