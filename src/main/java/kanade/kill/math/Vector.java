package kanade.kill.math;

import javax.vecmath.Vector3d;
import java.io.Serializable;
import java.util.Iterator;

public class Vector implements Cloneable, Serializable {
    protected Vector() {
    }

    @Override
    protected Vector clone() {
        return new Vector(start.clone(), end.clone());
    }

    protected BlockPos start;
    protected BlockPos end;

    @Override
    public String toString() {
        return "Start:\n" + start.toString() + "\nEnd:\n" + end.toString();
    }

    public Vector(BlockPos start, BlockPos end) {
        this.start = start;
        this.end = end;
    }

    public Vector(Vector3d source) {
        this(new BlockPos(), new BlockPos(source.x, source.y, source.z));
    }

    public Vector(net.minecraft.client.renderer.Vector3d source) {
        this(new BlockPos(), new BlockPos(source.x, source.y, source.z));
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public Vector(BlockPos start, double distance, double degreeXY, double degreeXZ) {
        this.start = start;
        double radXY = Math.toRadians(degreeXY);
        double radXZ = Math.toRadians(degreeXZ);
        double x = true ? distance * Math.sin(radXY) * Math.cos(radXZ) : distance * Math.sin(radXY);
        double z = distance * Math.sin(radXY) * Math.sin(radXZ);
        double y = distance * Math.cos(radXY);
        this.end = start.add(x, y, z, false);
    }

    public Iterator<BlockPos> line(double step) {
        boolean flag1 = end.getX() >= start.getX();
        boolean flag2 = end.getY() >= start.getY();
        boolean flag3 = end.getZ() >= start.getZ();
        boolean equalX = end.getX() == start.getX();
        boolean equalY = end.getY() == start.getY();
        boolean equalZ = end.getZ() == start.getZ();
        double slopeYX = Math.abs((end.getY() - start.getY()) / (end.getX() - start.getX()));
        double slopeZX = Math.abs((end.getZ() - start.getZ()) / (end.getX() - start.getX()));

        final BlockPos[] current = {start};

        return new Iterator<BlockPos>() {

            boolean first = true;

            @Override
            public boolean hasNext() {
                return (equalX || (flag1 ? current[0].getX() < end.getX() : current[0].getX() > end.getX())) &&
                        (equalY || (flag2 ? current[0].getY() < end.getY() : current[0].getY() > end.getY())) &&
                        (equalZ || (flag3 ? current[0].getZ() < end.getZ() : current[0].getZ() > end.getZ()));
            }

            @Override
            public BlockPos next() {
                if (first) {
                    first = false;
                    return current[0];
                }
                current[0] = new BlockPos(
                        current[0].getX() + (equalX ? 0 : (flag1 ? step : 0.0D - step)),
                        current[0].getY() + (equalY ? 0 : (flag2 ? step * slopeYX : (0.0D - step) * slopeYX)),
                        current[0].getZ() + (equalZ ? 0 : (flag3 ? step * slopeZX : (0.0D - step) * slopeZX)));
                return current[0];
            }
        };
    }

    public Vector rotateXY(double degree, boolean onThis) {
        if (degree == 0) {
            return onThis ? this : this.clone();
        }
        double X = XLength();
        double Y = YLength();
        double rad = Math.toRadians(degree);
        double x1 = X * Math.cos(rad) - Y * Math.sin(rad);
        double y1 = X * Math.sin(rad) + Y * Math.cos(rad);
        if (onThis) {
            end = new BlockPos(start.getX() + x1, start.getY() + y1, end.getZ());
            return this;
        }
        return new Vector(start, new BlockPos(start.getX() + x1, start.getY() + y1, end.getZ()));
    }

    public Vector rotateXZ(double degree, boolean onThis) {
        if (degree == 0) {
            return onThis ? this : this.clone();
        }
        double X = XLength();
        double Z = ZLength();
        double rad = Math.toRadians(degree);
        double x1 = X * Math.cos(rad) - Z * Math.sin(rad);
        double z1 = X * Math.sin(rad) + Z * Math.cos(rad);
        if (onThis) {
            end = new BlockPos(start.getX() + x1, end.getY(), start.getZ() + z1);
            return this;
        }
        return new Vector(start, new BlockPos(start.getX() + x1, end.getY(), start.getZ() + z1));
    }

    public Vector unit(boolean onThis) {
        double length = length();
        if (onThis) {
            end = start.add(XLength() / length, YLength() / length, ZLength() / length, false);
            return this;
        }
        return new Vector(start, start.add(XLength() / length, YLength() / length, ZLength() / length, false));
    }

    public double length() {
        return Math.sqrt(XLength() * XLength() + YLength() * YLength() + ZLength() * ZLength());
    }

    public double XLength() {
        return end.getX() - start.getX();
    }

    public double YLength() {
        return end.getY() - start.getY();
    }

    public double ZLength() {
        return end.getZ() - start.getZ();
    }

    public Vector append(double distance, boolean onThis) {
        Vector unit = unit(false);
        if (onThis) {
            end.add(unit.XLength() * distance, unit.YLength() * distance, unit.ZLength() * distance, true);
            return this;
        }
        return new Vector(start, end.add(unit.XLength() * distance, unit.YLength() * distance, unit.ZLength() * distance, false));
    }

    public Vector divide(double d, boolean onThis) {
        if (onThis) {
            this.end = start.add(XLength() / d, YLength() / d, ZLength() / d, false);
            return this;
        }
        return new Vector(start, start.add(XLength() / d, YLength() / d, ZLength() / d, false));
    }

    public Vector multiply(double d, boolean onThis) {
        if (onThis) {
            this.end = start.add(XLength() * d, YLength() * d, ZLength() * d, false);
            return this;
        }
        return new Vector(start, start.add(XLength() * d, YLength() * d, ZLength() * d, false));
    }
}