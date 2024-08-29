package miku.lib.jvm.hotspot.tools.jcore;

import miku.lib.jvm.hotspot.oops.*;
import miku.lib.jvm.hotspot.runtime.ClassConstants;
import miku.lib.jvm.hotspot.utilities.KlassArray;
import miku.lib.jvm.hotspot.utilities.MethodArray;
import miku.lib.jvm.hotspot.utilities.U1Array;
import miku.lib.jvm.hotspot.utilities.U2Array;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassWriter implements ClassConstants {
    protected InstanceKlass klass;
    protected DataOutputStream dos;
    protected ConstantPool cpool;

    protected Map<String, Short> classToIndex = new HashMap<>();
    protected Map<String, Short> utf8ToIndex = new HashMap<>();
    protected short _sourceFileIndex;
    protected short _innerClassesIndex;
    protected short _syntheticIndex;
    protected short _deprecatedIndex;
    protected short _constantValueIndex;
    protected short _codeIndex;
    protected short _exceptionsIndex;
    protected short _stackMapTableIndex;
    protected short _lineNumberTableIndex;
    protected short _localVariableTableIndex;
    protected short _signatureIndex;
    protected short _bootstrapMethodsIndex;

    protected static int extractHighShortFromInt(int val) {
        return val >> 16 & '\uffff';
    }

    protected static int extractLowShortFromInt(int val) {
        return val & '\uffff';
    }

    public ClassWriter(InstanceKlass kls, OutputStream os) {
        this.klass = kls;
        this.dos = new DataOutputStream(os);
        this.cpool = this.klass.getConstants();
    }

    protected void writeVersion() throws IOException {
        this.dos.writeShort(this.klass.minorVersion());
        this.dos.writeShort(this.klass.majorVersion());
    }

    protected void writeIndex(int index) throws IOException {
        if (index == 0) {
            throw new InternalError();
        } else {
            this.dos.writeShort(index);
        }
    }

    protected void writeConstantPool() throws IOException {
        U1Array tags = this.cpool.getTags();
        long len = tags.length();
        this.dos.writeShort((short) len);
        int ci;
        for (ci = 1; (long) ci < len; ++ci) {
            int cpConstType = tags.at(ci);
            if (cpConstType == 1) {
                Symbol sym = this.cpool.getSymbolAt(ci);
                this.utf8ToIndex.put(sym.toString(), (short) ci);
            } else if (cpConstType == 5 || cpConstType == 6) {
                ++ci;
            }
        }

        Short sourceFileIndex = this.utf8ToIndex.get("SourceFile");
        this._sourceFileIndex = sourceFileIndex != null ? sourceFileIndex : 0;
        Short innerClassesIndex = this.utf8ToIndex.get("InnerClasses");
        this._innerClassesIndex = innerClassesIndex != null ? innerClassesIndex : 0;
        Short bootstrapMethodsIndex = utf8ToIndex.get("BootstrapMethods");
        _bootstrapMethodsIndex = (bootstrapMethodsIndex != null) ? bootstrapMethodsIndex : 0;
        Short constantValueIndex = this.utf8ToIndex.get("ConstantValue");
        this._constantValueIndex = constantValueIndex != null ? constantValueIndex : 0;
        Short syntheticIndex = this.utf8ToIndex.get("Synthetic");
        this._syntheticIndex = syntheticIndex != null ? syntheticIndex : 0;
        Short deprecatedIndex = this.utf8ToIndex.get("Deprecated");
        this._deprecatedIndex = deprecatedIndex != null ? deprecatedIndex : 0;
        Short codeIndex = this.utf8ToIndex.get("Code");
        this._codeIndex = codeIndex != null ? codeIndex : 0;
        Short exceptionsIndex = this.utf8ToIndex.get("Exceptions");
        this._exceptionsIndex = exceptionsIndex != null ? exceptionsIndex : 0;
        Short stackMapTableIndex = utf8ToIndex.get("StackMapTable");
        _stackMapTableIndex = (stackMapTableIndex != null) ? stackMapTableIndex : 0;
        Short lineNumberTableIndex = this.utf8ToIndex.get("LineNumberTable");
        this._lineNumberTableIndex = lineNumberTableIndex != null ? lineNumberTableIndex : 0;
        Short localVariableTableIndex = this.utf8ToIndex.get("LocalVariableTable");
        this._localVariableTableIndex = localVariableTableIndex != null ? localVariableTableIndex : 0;
        Short signatureIdx = this.utf8ToIndex.get("Signature");
        this._signatureIndex = signatureIdx != null ? signatureIdx : 0;

        for (ci = 1; (long) ci < len; ++ci) {
            int cpConstType = tags.at(ci);
            int value;
            short bsmIndex;
            short nameAndTypeIndex;
            String str;
            short s;
            switch (cpConstType) {
                case 1:
                    this.dos.writeByte(cpConstType);
                    Symbol sym = this.cpool.getSymbolAt(ci);
                    this.dos.writeShort((short) sym.length());
                    this.dos.write(sym.asByteArray());
                    break;
                case 2:
                    throw new IllegalArgumentException("Unicode constant!");
                case 3:
                    this.dos.writeByte(cpConstType);
                    this.dos.writeInt(this.cpool.getIntAt(ci));
                    break;
                case 4:
                    this.dos.writeByte(cpConstType);
                    this.dos.writeFloat(this.cpool.getFloatAt(ci));
                    break;
                case 5:
                    this.dos.writeByte(cpConstType);
                    long l = this.cpool.getLongAt(ci);
                    ++ci;
                    this.dos.writeLong(l);
                    break;
                case 6:
                    this.dos.writeByte(cpConstType);
                    this.dos.writeDouble(this.cpool.getDoubleAt(ci));
                    ++ci;
                    break;
                case 7:
                case 100:
                case 103:
                    this.dos.writeByte(7);
                    str = this.cpool.getKlassNameAt(ci).toString();
                    s = this.utf8ToIndex.get(str);
                    this.classToIndex.put(str, (short) ci);
                    this.dos.writeShort(s);
                    break;
                case 8:
                    this.dos.writeByte(cpConstType);
                    str = this.cpool.getUnresolvedStringAt(ci).toString();
                    s = this.utf8ToIndex.get(str);
                    this.dos.writeShort(s);
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 18:
                    this.dos.writeByte(cpConstType);
                    value = this.cpool.getIntAt(ci);
                    bsmIndex = (short) extractLowShortFromInt(value);
                    nameAndTypeIndex = (short) extractHighShortFromInt(value);
                    this.dos.writeShort(bsmIndex);
                    this.dos.writeShort(nameAndTypeIndex);
                    break;
                case 15:
                    this.dos.writeByte(cpConstType);
                    value = this.cpool.getIntAt(ci);
                    byte refKind = (byte) extractLowShortFromInt(value);
                    nameAndTypeIndex = (short) extractHighShortFromInt(value);
                    this.dos.writeByte(refKind);
                    this.dos.writeShort(nameAndTypeIndex);
                    break;
                case 16:
                    this.dos.writeByte(cpConstType);
                    value = this.cpool.getIntAt(ci);
                    bsmIndex = (short) value;
                    this.dos.writeShort(bsmIndex);
                    break;
                default:
                    throw new InternalError("Unknown tag: " + cpConstType);
            }
        }
    }

    protected void writeClassAccessFlags() throws IOException {
        int flags = this.klass.getAccessFlags().getFlags() & 30257;
        this.dos.writeShort((short) flags);
    }

    protected void writeThisClass() throws IOException {
        String klassName = this.klass.getName();
        short index = this.classToIndex.get(klassName);
        this.dos.writeShort(index);
    }

    protected void writeSuperClass() throws IOException {
        Klass superKlass = this.klass.getSuper();
        if (superKlass != null) {
            String superName = superKlass.getName();
            short index = this.classToIndex.get(superName);
            this.dos.writeShort(index);
        } else {
            this.dos.writeShort(0);
        }
    }

    protected void writeInterfaces() throws IOException {
        KlassArray interfaces = this.klass.getLocalInterfaces();
        int len = interfaces.length();
        this.dos.writeShort((short) len);

        for (int i = 0; i < len; ++i) {
            Klass k = interfaces.getAt(i);
            short index = this.classToIndex.get(k.getName());
            this.dos.writeShort(index);
        }
    }

    protected void writeFields() throws IOException {
        int javaFieldsCount = this.klass.getJavaFieldsCount();
        this.dos.writeShort((short) javaFieldsCount);

        for (int index = 0; index < javaFieldsCount; ++index) {
            short accessFlags = this.klass.getFieldAccessFlags(index);
            this.dos.writeShort(accessFlags & 20703);
            short nameIndex = this.klass.getFieldNameIndex(index);
            this.dos.writeShort(nameIndex);
            short signatureIndex = this.klass.getFieldSignatureIndex(index);
            this.dos.writeShort(signatureIndex);
            short fieldAttributeCount = 0;
            boolean hasSyn = this.hasSyntheticAttribute(accessFlags);
            if (hasSyn) {
                ++fieldAttributeCount;
            }

            short initvalIndex = this.klass.getFieldInitialValueIndex(index);
            if (initvalIndex != 0) {
                ++fieldAttributeCount;
            }

            short genSigIndex = this.klass.getFieldGenericSignatureIndex(index);
            if (genSigIndex != 0) {
                ++fieldAttributeCount;
            }

            this.dos.writeShort(fieldAttributeCount);
            if (hasSyn) {
                this.writeSynthetic();
            }

            if (initvalIndex != 0) {
                this.writeIndex(this._constantValueIndex);
                this.dos.writeInt(2);
                this.dos.writeShort(initvalIndex);
            }

            if (genSigIndex != 0) {
                this.writeIndex(this._signatureIndex);
                this.dos.writeInt(2);
                this.dos.writeShort(genSigIndex);
            }
        }

    }

    protected void writeMethod(Method m) throws IOException {
        int accessFlags = m.getAccessFlags().getFlags();
        this.dos.writeShort((short) (accessFlags & 7679));
        this.dos.writeShort(m.getNameIndex());
        this.dos.writeShort(m.getSignatureIndex());
        boolean isNative = (accessFlags & 256) != 0;
        boolean isAbstract = (accessFlags & 1024) != 0;
        short methodAttributeCount = 0;
        boolean hasSyn = this.hasSyntheticAttribute((short) accessFlags);
        if (hasSyn) {
            ++methodAttributeCount;
        }

        boolean hasCheckedExceptions = m.hasCheckedExceptions();
        if (hasCheckedExceptions) {
            ++methodAttributeCount;
        }

        boolean isCodeAvailable = !isNative && !isAbstract;
        if (isCodeAvailable) {
            ++methodAttributeCount;
        }

        boolean isGeneric = m.getGenericSignature() != null;
        if (isGeneric) {
            ++methodAttributeCount;
        }

        this.dos.writeShort(methodAttributeCount);
        if (hasSyn) {
            this.writeSynthetic();
        }

        int codeAttrCount;
        int codeSize;
        if (isCodeAvailable) {
            byte[] code = m.getByteCode();
            codeAttrCount = 0;
            codeSize = 8 + code.length + 2 + 2;
            boolean hasExceptionTable = m.hasExceptionTable();
            ExceptionTableElement[] exceptionTable = null;
            int exceptionTableLen = 0;
            if (hasExceptionTable) {
                exceptionTable = m.getExceptionTable();
                exceptionTableLen = exceptionTable.length;
                codeSize += exceptionTableLen * 8;
            }

            boolean hasStackMapTable = m.hasStackMapTable();
            U1Array stackMapData = null;
            int stackMapAttrLen = 0;

            if (hasStackMapTable) {
                stackMapData = m.getStackMapData();

                stackMapAttrLen = stackMapData.length();

                codeSize += 2 /* stack map table attr index */ +
                        4 /* stack map table attr length */ +
                        stackMapAttrLen;

                codeAttrCount++;
            }

            boolean hasLineNumberTable = m.hasLineNumberTable();
            LineNumberTableElement[] lineNumberTable = null;
            int lineNumberAttrLen = 0;
            if (hasLineNumberTable) {
                lineNumberTable = m.getLineNumberTable();
                lineNumberAttrLen = 2 + lineNumberTable.length * 4;
                codeSize += 6 + lineNumberAttrLen;
                codeAttrCount = (short) (codeAttrCount + 1);
            }

            boolean hasLocalVariableTable = m.hasLocalVariableTable();
            LocalVariableTableElement[] localVariableTable = null;
            int localVarAttrLen = 0;
            if (hasLocalVariableTable) {
                localVariableTable = m.getLocalVariableTable();
                localVarAttrLen = 2 + localVariableTable.length * 10;
                codeSize += 6 + localVarAttrLen;
                codeAttrCount = (short) (codeAttrCount + 1);
            }

            this.rewriteByteCode(m, code);
            this.writeIndex(this._codeIndex);
            this.dos.writeInt(codeSize);
            this.dos.writeShort(m.getMaxStack());
            this.dos.writeShort(m.getMaxLocals());
            this.dos.writeInt(code.length);
            this.dos.write(code);
            this.dos.writeShort((short) exceptionTableLen);
            int l;
            if (exceptionTableLen != 0) {
                for (l = 0; l < exceptionTableLen; ++l) {
                    this.dos.writeShort((short) exceptionTable[l].getStartPC());
                    this.dos.writeShort((short) exceptionTable[l].getEndPC());
                    this.dos.writeShort((short) exceptionTable[l].getHandlerPC());
                    this.dos.writeShort((short) exceptionTable[l].getCatchTypeIndex());
                }
            }
            this.dos.writeShort(codeAttrCount);

            if (hasStackMapTable) {
                writeIndex(_stackMapTableIndex);
                dos.writeInt(stackMapAttrLen);
                // We write bytes directly as stackMapData is
                // raw data (#entries + entries)
                for (int i = 0; i < stackMapData.length(); i++) {
                    dos.writeByte(stackMapData.at(i));
                }
            }


            if (hasLineNumberTable) {
                this.writeIndex(this._lineNumberTableIndex);
                this.dos.writeInt(lineNumberAttrLen);
                this.dos.writeShort((short) lineNumberTable.length);

                for (l = 0; l < lineNumberTable.length; ++l) {
                    this.dos.writeShort((short) lineNumberTable[l].getStartBCI());
                    this.dos.writeShort((short) lineNumberTable[l].getLineNumber());
                }
            }

            if (hasLocalVariableTable) {
                this.writeIndex(this._localVariableTableIndex);
                this.dos.writeInt(localVarAttrLen);
                this.dos.writeShort((short) localVariableTable.length);

                for (l = 0; l < localVariableTable.length; ++l) {
                    this.dos.writeShort((short) localVariableTable[l].getStartBCI());
                    this.dos.writeShort((short) localVariableTable[l].getLength());
                    this.dos.writeShort((short) localVariableTable[l].getNameCPIndex());
                    this.dos.writeShort((short) localVariableTable[l].getDescriptorCPIndex());
                    this.dos.writeShort((short) localVariableTable[l].getSlot());
                }
            }
        }

        if (hasCheckedExceptions) {
            CheckedExceptionElement[] exceptions = m.getCheckedExceptions();
            this.writeIndex(this._exceptionsIndex);
            codeAttrCount = 2 + exceptions.length * 2;
            this.dos.writeInt(codeAttrCount);
            this.dos.writeShort(exceptions.length);

            for (codeSize = 0; codeSize < exceptions.length; ++codeSize) {
                short cpIndex = (short) exceptions[codeSize].getClassCPIndex();
                this.dos.writeShort(cpIndex);
            }

        }


        if (isGeneric) {
            this.writeGenericSignature(m.getGenericSignature().toString());
        }

    }

    protected void writeSynthetic() throws IOException {
        this.writeIndex(this._syntheticIndex);
        this.dos.writeInt(0);
    }

    protected boolean isSynthetic(short accessFlags) {
        return (accessFlags & 4096) != 0;
    }

    protected boolean hasSyntheticAttribute(short accessFlags) {
        return this.isSynthetic(accessFlags) && this._syntheticIndex != 0;
    }

    protected void rewriteByteCode(Method m, byte[] code) {
        ByteCodeRewriter r = new ByteCodeRewriter(m, this.cpool, code);
        r.rewrite();
    }

    protected void writeGenericSignature(String signature) throws IOException {
        this.writeIndex(this._signatureIndex);
        this.dos.writeInt(2);
        Short index = this.utf8ToIndex.get(signature);
        this.dos.writeShort(index);
    }

    protected void writeMethods() throws IOException {
        MethodArray methods = this.klass.getMethods();
        int len = methods.length();
        this.dos.writeShort((short) len);

        for (int m = 0; m < len; ++m) {
            this.writeMethod(methods.at(m));
        }

    }

    protected void writeClassAttributes() throws IOException {

        final int flags = klass.getAccessFlags().getFlags();
        final boolean hasSyn = hasSyntheticAttribute((short) flags);

        // check for source file
        short classAttributeCount = 0;

        if (hasSyn)
            classAttributeCount++;

        Symbol sourceFileName = klass.getSourceFileName();
        if (sourceFileName != null)
            classAttributeCount++;

        Symbol genericSignature = klass.getGenericSignature();
        if (genericSignature != null)
            classAttributeCount++;

        U2Array innerClasses = klass.getInnerClasses();
        final int numInnerClasses = innerClasses.length() / 4;
        if (numInnerClasses != 0) {
            classAttributeCount++;
        }

        int bsmCount = klass.getConstants().getBootstrapMethodsCount();
        if (bsmCount != 0) {
            classAttributeCount++;
        }

        dos.writeShort(classAttributeCount);

        if (hasSyn)
            writeSynthetic();

        // write SourceFile, if any
        if (sourceFileName != null) {
            writeIndex(_sourceFileIndex);
            dos.writeInt(2);
            Short index = utf8ToIndex.get(sourceFileName.toString());
            dos.writeShort(index);
        }

        // write Signature, if any
        if (genericSignature != null) {
            writeGenericSignature(genericSignature.toString());
        }

        // write inner classes, if any
        if (numInnerClasses != 0) {
            writeIndex(_innerClassesIndex);
            final int innerAttrLen = 2 /* number_of_inner_classes */ +
                    numInnerClasses * (
                            2 /* inner_class_info_index */ +
                                    2 /* outer_class_info_index */ +
                                    2 /* inner_class_name_index */ +
                                    2 /* inner_class_access_flags */);
            dos.writeInt(innerAttrLen);

            dos.writeShort(numInnerClasses);

            for (int index = 0; index < numInnerClasses * 4; index++) {
                dos.writeShort(innerClasses.at(index));
            }
        }

        if (bsmCount != 0) {
            ConstantPool cpool = klass.getConstants();
            writeIndex(_bootstrapMethodsIndex);
            int attrLen = 2; // num_bootstrap_methods
            for (int index = 0; index < bsmCount; index++) {
                int bsmArgsCount = cpool.getBootstrapMethodArgsCount(index);
                attrLen += 2 // bootstrap_method_ref
                        + 2 // num_bootstrap_arguments
                        + bsmArgsCount * 2;
            }
            dos.writeInt(attrLen);
            dos.writeShort(bsmCount);
            for (int index = 0; index < bsmCount; index++) {
                short[] value = cpool.getBootstrapMethodAt(index);
                for (short item : value) {
                    dos.writeShort(item);
                }
            }
        }
    }

    public void write() throws IOException {
        this.dos.writeInt(-889275714);
        this.writeVersion();
        this.writeConstantPool();
        this.writeClassAccessFlags();
        this.writeThisClass();
        this.writeSuperClass();
        this.writeInterfaces();
        this.writeFields();
        this.writeMethods();
        this.writeClassAttributes();
        this.dos.flush();
    }
}
