//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kanade.kill.util;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class VertexFormat {
    private final List<VertexFormatElement> elements;
    private final List<Integer> offsets;
    private final List<Integer> uvOffsetsById;
    private int nextOffset;
    private int colorElementOffset;
    private int normalElementOffset;
    private int hashCode;

    public VertexFormat(VertexFormat vertexFormatIn) {
        this();

        for (int i = 0; i < vertexFormatIn.getElementCount(); ++i) {
            this.addElement(vertexFormatIn.getElement(i));
        }

        this.nextOffset = vertexFormatIn.getNextOffset();
    }

    public VertexFormat() {
        this.elements = Lists.newArrayList();
        this.offsets = Lists.newArrayList();
        this.colorElementOffset = -1;
        this.uvOffsetsById = Lists.newArrayList();
        this.normalElementOffset = -1;
    }

    public void clear() {
        this.elements.clear();
        this.offsets.clear();
        this.colorElementOffset = -1;
        this.uvOffsetsById.clear();
        this.normalElementOffset = -1;
        this.nextOffset = 0;
        this.hashCode = 0;
    }

    public VertexFormat addElement(VertexFormatElement element) {
        if (!element.isPositionElement() || !this.hasPosition()) {
            this.elements.add(element);
            this.offsets.add(this.nextOffset);
            switch (element.getUsage()) {
                case NORMAL:
                    this.normalElementOffset = this.nextOffset;
                    break;
                case COLOR:
                    this.colorElementOffset = this.nextOffset;
                    break;
                case UV:
                    this.uvOffsetsById.add(element.getIndex(), this.nextOffset);
            }

            this.nextOffset += element.getSize();
            this.hashCode = 0;
        }
        return this;
    }

    public boolean hasNormal() {
        return this.normalElementOffset >= 0;
    }

    public int getNormalOffset() {
        return this.normalElementOffset;
    }

    public boolean hasColor() {
        return this.colorElementOffset >= 0;
    }

    public int getColorOffset() {
        return this.colorElementOffset;
    }

    public boolean hasUvOffset(int id) {
        return this.uvOffsetsById.size() - 1 >= id;
    }

    public int getUvOffsetById(int id) {
        return this.uvOffsetsById.get(id);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("format: " + this.elements.size() + " elements: ");

        for (int i = 0; i < this.elements.size(); ++i) {
            s.append(this.elements.get(i).toString());
            if (i != this.elements.size() - 1) {
                s.append(" ");
            }
        }

        return s.toString();
    }

    private boolean hasPosition() {
        int i = 0;

        for (int j = this.elements.size(); i < j; ++i) {
            VertexFormatElement NorthestVertexFormatElement = this.elements.get(i);
            if (NorthestVertexFormatElement.isPositionElement()) {
                return true;
            }
        }

        return false;
    }

    public int getIntegerSize() {
        return this.getNextOffset() / 4;
    }

    public int getNextOffset() {
        return this.nextOffset;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public VertexFormatElement getElement(int index) {
        return this.elements.get(index);
    }

    public int getOffset(int index) {
        return this.offsets.get(index);
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            VertexFormat vertexformat = (VertexFormat) p_equals_1_;
            if (this.nextOffset != vertexformat.nextOffset) {
                return false;
            } else {
                return this.elements.equals(vertexformat.elements) && this.offsets.equals(vertexformat.offsets);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (this.hashCode != 0) {
            return this.hashCode;
        } else {
            int i = this.elements.hashCode();
            i = 31 * i + this.offsets.hashCode();
            i = 31 * i + this.nextOffset;
            this.hashCode = i;
            return i;
        }
    }
}
