package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface AuxFunctionDefinitionRecord extends AuxSymbolRecord {
    int getTagIndex();

    int getTotalSize();

    int getPointerToLineNumber();

    int getPointerToNextFunction();
}
