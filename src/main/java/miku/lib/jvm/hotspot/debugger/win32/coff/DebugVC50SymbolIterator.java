package miku.lib.jvm.hotspot.debugger.win32.coff;

import java.util.NoSuchElementException;

public interface DebugVC50SymbolIterator extends DebugVC50SymbolTypes, DebugVC50SymbolEnums {
    boolean done();

    void next() throws NoSuchElementException;

    short getLength();

    int getType();

    int getOffset();

    byte getCompilerTargetProcessor();

    int getCompilerFlags();

    String getComplierVersion();

    int getRegisterSymbolType();

    short getRegisterEnum();

    String getRegisterSymbolName();

    int getConstantType();

    int getConstantValueAsInt() throws DebugVC50WrongNumericTypeException;

    long getConstantValueAsLong() throws DebugVC50WrongNumericTypeException;

    float getConstantValueAsFloat() throws DebugVC50WrongNumericTypeException;

    double getConstantValueAsDouble() throws DebugVC50WrongNumericTypeException;

    String getConstantName();

    int getUDTType();

    String getUDTName();

    int getSearchSymbolOffset();

    short getSearchSegment();

    int getObjectCodeViewSignature();

    String getObjectName();

    int getManyRegType();

    byte getManyRegCount();

    byte getManyRegRegister(int var1);

    String getManyRegName();

    short getReturnFlags();

    byte getReturnStyle();

    byte getReturnRegisterCount();

    byte getReturnRegister(int var1);

    void advanceToEntryThisSymbol();

    int getBPRelOffset();

    int getBPRelType();

    String getBPRelName();

    int getLGDataType();

    int getLGDataOffset();

    short getLGDataSegment();

    String getLGDataName();

    DebugVC50SymbolIterator getLGProcParent();

    int getLGProcParentOffset();

    DebugVC50SymbolIterator getLGProcEnd();

    int getLGProcEndOffset();

    DebugVC50SymbolIterator getLGProcNext();

    int getLGProcNextOffset();

    int getLGProcLength();

    int getLGProcDebugStart();

    int getLGProcDebugEnd();

    int getLGProcType();

    int getLGProcOffset();

    short getLGProcSegment();

    byte getLGProcFlags();

    String getLGProcName();

    DebugVC50SymbolIterator getThunkParent();

    int getThunkParentOffset();

    DebugVC50SymbolIterator getThunkEnd();

    int getThunkEndOffset();

    DebugVC50SymbolIterator getThunkNext();

    int getThunkNextOffset();

    int getThunkOffset();

    short getThunkSegment();

    short getThunkLength();

    byte getThunkType();

    String getThunkName();

    short getThunkAdjustorThisDelta();

    String getThunkAdjustorTargetName();

    short getThunkVCallDisplacement();

    int getThunkPCodeOffset();

    short getThunkPCodeSegment();

    DebugVC50SymbolIterator getBlockParent();

    int getBlockParentOffset();

    DebugVC50SymbolIterator getBlockEnd();

    int getBlockEndOffset();

    int getBlockLength();

    int getBlockOffset();

    short getBlockSegment();

    String getBlockName();

    int getLabelOffset();

    short getLabelSegment();

    byte getLabelFlags();

    String getLabelName();

    int getChangeOffset();

    short getChangeSegment();

    short getChangeModel();

    int getVTableRoot();

    int getVTablePath();

    int getVTableOffset();

    short getVTableSegment();

    int getRegRelOffset();

    int getRegRelType();

    short getRegRelRegister();

    String getRegRelName();

    int getLThreadType();

    int getLThreadOffset();

    short getLThreadSegment();

    String getLThreadName();
}
