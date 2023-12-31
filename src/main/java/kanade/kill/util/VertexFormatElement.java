//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kanade.kill.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GLHelper;
import org.lwjgl.opengl.GLOffsets;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class VertexFormatElement {
    private final EnumType type;
    private final EnumUsage usage;
    private final int index;
    private final int elementCount;

    public VertexFormatElement(int indexIn, EnumType typeIn, EnumUsage usageIn, int count) {
        if (this.isFirstOrUV(indexIn, usageIn)) {
            this.usage = usageIn;
        } else {
            this.usage = VertexFormatElement.EnumUsage.UV;
        }

        this.type = typeIn;
        this.index = indexIn;
        this.elementCount = count;
    }

    private final boolean isFirstOrUV(int p_177372_1_, EnumUsage p_177372_2_) {
        return p_177372_1_ == 0 || p_177372_2_ == VertexFormatElement.EnumUsage.UV;
    }

    public final EnumType getType() {
        return this.type;
    }

    public final EnumUsage getUsage() {
        return this.usage;
    }

    public final int getElementCount() {
        return this.elementCount;
    }

    public final int getIndex() {
        return this.index;
    }

    public String toString() {
        return this.elementCount + "," + this.usage.getDisplayName() + "," + this.type.getDisplayName();
    }

    public final int getSize() {
        return this.type.getSize() * this.elementCount;
    }

    public final boolean isPositionElement() {
        return this.usage == VertexFormatElement.EnumUsage.POSITION;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            VertexFormatElement vertexformatelement = (VertexFormatElement) p_equals_1_;
            if (this.elementCount != vertexformatelement.elementCount) {
                return false;
            } else if (this.index != vertexformatelement.index) {
                return false;
            } else if (this.type != vertexformatelement.type) {
                return false;
            } else {
                return this.usage == vertexformatelement.usage;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = this.type.hashCode();
        i = 31 * i + this.usage.hashCode();
        i = 31 * i + this.index;
        i = 31 * i + this.elementCount;
        return i;
    }

    @SideOnly(Side.CLIENT)
    public enum EnumUsage {
        POSITION("Position"),
        NORMAL("Normal"),
        COLOR("Vertex Color"),
        UV("UV"),
        /**
         * @deprecated
         */
        @Deprecated
        MATRIX("Bone Matrix"),
        /**
         * @deprecated
         */
        @Deprecated
        BLEND_WEIGHT("Blend Weight"),
        PADDING("Padding"),
        GENERIC("Generic");

        private final String displayName;

        EnumUsage(String displayNameIn) {
            this.displayName = displayNameIn;
        }

        public void preDraw(VertexFormat format, int element, int stride, ByteBuffer buffer) {
            VertexFormatElement attr = format.getElement(element);
            int count = attr.getElementCount();
            int constant = attr.getType().getGlConstant();
            buffer.position(format.getOffset(element));
            switch (this) {
                case POSITION:
                    GLHelper.nglVertexPointer(count, constant, stride, MemoryUtil.getAddress0(buffer) + (long) buffer.position(), GLOffsets.glVertexPointer);
                    GLHelper.nglEnableClientState(32884, GLOffsets.glEnableClientState);
                    break;
                case NORMAL:
                    if (count != 3) {
                        throw new IllegalArgumentException("Normal attribute should have the size 3: " + attr);
                    }

                    GLHelper.nglNormalPointer(constant, stride, MemoryUtil.getAddress0(buffer) + (long) buffer.position(), GLOffsets.glNormalPointer);
                    GLHelper.nglEnableClientState(32885, GLOffsets.glEnableClientState);
                    break;
                case COLOR:
                    GLHelper.nglColorPointer(count, constant, stride, MemoryUtil.getAddress0(buffer) + (long) buffer.position(), GLOffsets.glColorPointer);
                    GLHelper.nglEnableClientState(32886, GLOffsets.glEnableClientState);
                    break;
                case UV:
                    GLHelper.nglClientActiveTexture('蓀' + attr.getIndex(), GLOffsets.glClientActiveTexture);
                    GLHelper.nglTexCoordPointer(count, constant, stride, MemoryUtil.getAddress0(buffer) + (long) buffer.position(), GLOffsets.glTexCoordPointer);
                    GLHelper.nglEnableClientState(32888, GLOffsets.glEnableClientState);
                    GLHelper.nglClientActiveTexture(33984, GLOffsets.glClientActiveTexture);
                case PADDING:
                    break;
                case GENERIC:
                    GLHelper.nglEnableVertexAttribArray(attr.getIndex(), GLOffsets.glEnableVertexAttribArray);
                    GLHelper.nglVertexAttribPointer(attr.getIndex(), count, constant, false, stride, MemoryUtil.getAddress0(buffer) + (long) buffer.position(), GLOffsets.glVertexAttribPointer);
                    break;
                default:
                    throw new RuntimeException("Unimplemented vanilla attribute upload: " + this.getDisplayName());
            }

        }

        public void postDraw(VertexFormat format, int element, int stride, ByteBuffer buffer) {
            VertexFormatElement attr = format.getElement(element);
            switch (this) {
                case POSITION:
                    GLHelper.nglDisableClientState(32884, GLOffsets.glDisableClientState);
                    break;
                case NORMAL:
                    GLHelper.nglDisableClientState(32885, GLOffsets.glDisableClientState);
                    break;
                case COLOR:
                    GLHelper.nglDisableClientState(32886, GLOffsets.glDisableClientState);
                    break;
                case UV:
                    GLHelper.nglClientActiveTexture('蓀' + attr.getIndex(), GLOffsets.glClientActiveTexture);
                    GLHelper.nglDisableClientState(32888, GLOffsets.glDisableClientState);
                    GLHelper.nglClientActiveTexture(33984, GLOffsets.glClientActiveTexture);
                case PADDING:
                    break;
                case GENERIC:
                    GLHelper.nglDisableVertexAttribArray(attr.getIndex(), GLOffsets.glDisableVertexAttribArray);
                    break;
                default:
                    throw new RuntimeException("Unimplemented vanilla attribute upload: " + this.getDisplayName());
            }

        }

        public String getDisplayName() {
            return this.displayName;
        }
    }

    @SideOnly(Side.CLIENT)
    public enum EnumType {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String displayName;
        private final int glConstant;

        EnumType(int sizeIn, String displayNameIn, int glConstantIn) {
            this.size = sizeIn;
            this.displayName = displayNameIn;
            this.glConstant = glConstantIn;
        }

        public int getSize() {
            return this.size;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public int getGlConstant() {
            return this.glConstant;
        }
    }
}