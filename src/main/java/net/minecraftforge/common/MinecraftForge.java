package net.minecraftforge.common;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.eventhandler.EventBus;

import java.util.Set;

public class MinecraftForge {
    public static EventBus EVENT_BUS;
    public static EventBus Event_bus;

    public static void preloadCrashClasses(ASMDataTable asmHarvestedData, String modId, Set<String> classList) {
    }

    public static void initialize() {

    }
}
