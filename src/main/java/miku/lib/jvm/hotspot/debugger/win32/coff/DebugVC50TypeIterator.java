package miku.lib.jvm.hotspot.debugger.win32.coff;

import java.util.NoSuchElementException;

public interface DebugVC50TypeIterator {
    boolean done();

    void next() throws NoSuchElementException;

    short getLength();

    int getTypeIndex();

    int getNumTypes();

    boolean typeStringDone();

    void typeStringNext() throws NoSuchElementException;

    int typeStringLeaf();

    int typeStringOffset();

    int getModifierIndex();

    short getModifierAttribute();

    int getPointerType();

    int getPointerAttributes();

    int getPointerBasedOnTypeIndex();

    String getPointerBasedOnTypeName();

    int getPointerToMemberClass();

    short getPointerToMemberFormat();

    int getArrayElementType();

    int getArrayIndexType();

    int getArrayLength() throws
            DebugVC50WrongNumericTypeException;

    String getArrayName();

    short getClassCount();

    short getClassProperty();

    int getClassFieldList();


    DebugVC50TypeIterator getClassFieldListIterator();

    int getClassDerivationList();

    int getClassVShape();

    int getClassSize() throws
            DebugVC50WrongNumericTypeException;

    String getClassName();

    short getUnionCount();

    short getUnionProperty();

    int getUnionFieldList();


    DebugVC50TypeIterator getUnionFieldListIterator();

    int getUnionSize() throws
            DebugVC50WrongNumericTypeException;

    String getUnionName();

    short getEnumCount();

    short getEnumProperty();

    int getEnumType();

    int getEnumFieldList();


    DebugVC50TypeIterator getEnumFieldListIterator();

    String getEnumName();

    int getProcedureReturnType();

    byte getProcedureCallingConvention();

    short getProcedureNumberOfParameters();

    int getProcedureArgumentList();


    DebugVC50TypeIterator getProcedureArgumentListIterator();

    int getMFunctionReturnType();

    int getMFunctionContainingClass();

    int getMFunctionThis();

    byte getMFunctionCallingConvention();

    short getMFunctionNumberOfParameters();

    int getMFunctionArgumentList();


    DebugVC50TypeIterator getMFunctionArgumentListIterator();

    int getMFunctionThisAdjust();

    short getVTShapeCount();

    int getVTShapeDescriptor(int var1);

    int getBasicArrayType();

    short getLabelAddressMode();

    int getDimArrayType();

    int getDimArrayDimInfo();

    String getDimArrayName();

    int getVFTPathCount();

    int getVFTPathBase(int var1);

    int getSkipIndex();

    int getArgListCount();

    int getArgListType(int var1);

    int getDefaultArgType();

    String getDefaultArgExpression();

    int getDerivedCount();

    int getDerivedType(int var1);

    int getBitfieldFieldType();

    byte getBitfieldLength();

    byte getBitfieldPosition();

    short getMListAttribute();

    int getMListLength();

    int getMListType(int var1);

    boolean isMListIntroducingVirtual();

    int getMListVtabOffset();

    DebugVC50SymbolIterator getRefSym();

    short getBClassAttribute();

    int getBClassType();

    int getBClassOffset() throws
            DebugVC50WrongNumericTypeException;

    short getVBClassAttribute();

    int getVBClassBaseClassType();

    int getVBClassVirtualBaseClassType();

    int getVBClassVBPOff() throws
            DebugVC50WrongNumericTypeException;

    int getVBClassVBOff() throws
            DebugVC50WrongNumericTypeException;

    short getIVBClassAttribute();

    int getIVBClassBType();

    int getIVBClassVBPType();

    int getIVBClassVBPOff() throws
            DebugVC50WrongNumericTypeException;

    int getIVBClassVBOff() throws
            DebugVC50WrongNumericTypeException;

    short getEnumerateAttribute();

    long getEnumerateValue() throws
            DebugVC50WrongNumericTypeException;

    String getEnumerateName();

    int getFriendFcnType();

    String getFriendFcnName();

    int getIndexValue();


    DebugVC50TypeIterator getIndexIterator();

    short getMemberAttribute();

    int getMemberType();

    int getMemberOffset() throws
            DebugVC50WrongNumericTypeException;

    String getMemberName();

    short getStaticAttribute();

    int getStaticType();

    String getStaticName();

    short getMethodCount();

    int getMethodList();

    String getMethodName();

    int getNestedType();

    String getNestedName();

    int getVFuncTabType();

    int getFriendClsType();

    short getOneMethodAttribute();

    int getOneMethodType();

    boolean isOneMethodIntroducingVirtual();

    int getOneMethodVBaseOff();

    String getOneMethodName();

    int getVFuncOffType();

    int getVFuncOffOffset();

    short getNestedExAttribute();

    int getNestedExType();

    String getNestedExName();

    short getMemberModifyAttribute();

    int getMemberModifyType();

    String getMemberModifyName();

    short getNumericTypeAt(int var1);

    int getNumericLengthAt(int var1) throws
            DebugVC50WrongNumericTypeException;

    int getNumericIntAt(int var1) throws
            DebugVC50WrongNumericTypeException;

    long getNumericLongAt(int var1) throws
            DebugVC50WrongNumericTypeException;

    float getNumericFloatAt(int var1) throws
            DebugVC50WrongNumericTypeException;

    double getNumericDoubleAt(int var1) throws
            DebugVC50WrongNumericTypeException;

    byte[] getNumericDataAt(int var1) throws DebugVC50WrongNumericTypeException;
}
