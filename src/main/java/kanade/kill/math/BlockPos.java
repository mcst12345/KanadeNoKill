package kanade.kill.math;

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Iterator;

public class BlockPos implements Serializable, Cloneable {
    public BlockPos(Entity entity) {
        this(entity.posX, entity.posY, entity.posZ);
    }

    private double x;
    private double y;
    private double z;

    @Override
    public int hashCode() {
        return (int) ((x * y + 114.514) * z % 1.4514);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos) {
            return ((BlockPos) obj).getX() == x &&
                    ((BlockPos) obj).getY() == y &&
                    ((BlockPos) obj).getZ() == z;
        }
        return false;
    }

    @Override
    public String toString() {
        return "X:" + x + " Y:" + y + " Z:" + z;
    }

    public BlockPos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public BlockPos() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public BlockPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos add(double x, double y, double z, boolean onThis) {
        if (x == 0.0D && y == 0.0D && z == 0.0D) {
            return onThis ? this : clone();
        }
        if (onThis) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }
        return new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    public BlockPos add(Vector vector, boolean onThis) {
        return add(vector.XLength(), vector.YLength(), vector.ZLength(), onThis);
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
        return getAllInBox(from, to, 1);
    }

    public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to, double step) {
        return getAllInBox(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()), step);
    }

    public static Iterable<BlockPos> getAllInBox(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2, final double step) {
        return new Iterable<BlockPos>() {
            @Nonnull
            public Iterator<BlockPos> iterator() {
                return new AbstractIterator<BlockPos>() {
                    private boolean first = true;
                    private double lastPosX;
                    private double lastPosY;
                    private double lastPosZ;

                    public BlockPos computeNext() {
                        if (this.first) {
                            this.first = false;
                            this.lastPosX = x1;
                            this.lastPosY = y1;
                            this.lastPosZ = z1;
                            return new BlockPos(x1, y1, z1);
                        } else if (this.lastPosX == x2 && this.lastPosY == y2 && this.lastPosZ == z2) {
                            return this.endOfData();
                        } else {
                            if (this.lastPosX < x2) {
                                lastPosX += step;
                            } else if (this.lastPosY < y2) {
                                this.lastPosX = x1;
                                lastPosY += step;
                            } else if (this.lastPosZ < z2) {
                                this.lastPosX = x1;
                                this.lastPosY = y1;
                                lastPosZ += step;
                            }

                            return new BlockPos(this.lastPosX, this.lastPosY, this.lastPosZ);
                        }
                    }
                };
            }
        };
    }

    @Override
    public BlockPos clone() {
        return new BlockPos(x, y, z);
    }
}
