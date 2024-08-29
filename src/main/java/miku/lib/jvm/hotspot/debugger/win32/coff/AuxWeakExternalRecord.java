package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface AuxWeakExternalRecord extends AuxSymbolRecord {
    int IMAGE_WEAK_EXTERN_SEARCH_NOLIBRARY = 1;
    int IMAGE_WEAK_EXTERN_SEARCH_LIBRARY = 2;
    int IMAGE_WEAK_EXTERN_SEARCH_ALIAS = 3;

    int getTagIndex();

    int getCharacteristics();
}
