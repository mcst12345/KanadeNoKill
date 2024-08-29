package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFSymbol {
    int getOffset();

    String getName();

    int getValue();

    short getSectionNumber();

    short getType();

    byte getStorageClass();

    byte getNumberOfAuxSymbols();

    boolean isFunctionDefinition();

    AuxFunctionDefinitionRecord getAuxFunctionDefinitionRecord();

    boolean isBfOrEfSymbol();

    AuxBfEfRecord getAuxBfEfRecord();

    boolean isWeakExternal();

    AuxWeakExternalRecord getAuxWeakExternalRecord();

    boolean isFile();

    AuxFileRecord getAuxFileRecord();

    boolean isSectionDefinition();

    AuxSectionDefinitionsRecord getAuxSectionDefinitionsRecord();
}
