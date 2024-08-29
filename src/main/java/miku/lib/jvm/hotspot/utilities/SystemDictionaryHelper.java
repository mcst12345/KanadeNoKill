package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.memory.Dictionary;
import miku.lib.jvm.hotspot.memory.SystemDictionary;

import java.lang.management.ManagementFactory;

public class SystemDictionaryHelper {
    public static void main(String[] args) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        Dictionary dictionary = SystemDictionary.getDictionary();
        dictionary.classesDo(var1 -> System.out.println(var1.getName()));
    }
}
