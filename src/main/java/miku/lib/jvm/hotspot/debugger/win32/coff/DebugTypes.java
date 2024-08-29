package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugTypes {
    int IMAGE_DEBUG_TYPE_UNKNOWN = 0;
    int IMAGE_DEBUG_TYPE_COFF = 1;
    int IMAGE_DEBUG_TYPE_CODEVIEW = 2;
    int IMAGE_DEBUG_TYPE_FPO = 3;
    int IMAGE_DEBUG_TYPE_MISC = 4;
    int IMAGE_DEBUG_TYPE_EXCEPTION = 5;
    int IMAGE_DEBUG_TYPE_FIXUP = 6;
    int IMAGE_DEBUG_TYPE_OMAP_TO_SRC = 7;
    int IMAGE_DEBUG_TYPE_OMAP_FROM_SRC = 8;
    int IMAGE_DEBUG_TYPE_BORLAND = 9;
}
