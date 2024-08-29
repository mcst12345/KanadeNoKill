package kanade.kill.util;

import kanade.kill.math.BlockPos;
import kanade.kill.math.Vector;
import kanade.kill.math.Vector2Don3D;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.Iterator;

public class ParticleUtil {
    public static void drawCircle(Entity entity, double r, EnumParticleTypes type) {
        double x = entity.X;
        double y = entity.Y;
        double z = entity.Z;
        World world = entity.WORLD;
        for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            double X = r * Math.cos(rad) + x;
            double Z = r * Math.sin(rad) + z;
            world.spawnParticle(type, X, y + 0.1, Z, 0, 0, 0);
        }
    }

    public static void drawWing(Entity entity) {
        BlockPos pos = new BlockPos(entity).add(0, 1, 0, true);
        Vector vector = new Vector(pos, 1, 0.4, 0);
        Vector left, right;

        left = vector.rotateXZ(entity.rotationYaw + 90, false);
        right = vector.rotateXZ(entity.rotationYaw - 90, false);
        for (Iterator<BlockPos> it = left.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
        for (Iterator<BlockPos> it = right.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
        vector = new Vector(pos, 1, 0.45, 0);
        left = vector.rotateXZ(entity.rotationYaw + 90, false);
        right = vector.rotateXZ(entity.rotationYaw - 90, false);
        for (Iterator<BlockPos> it = left.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
        for (Iterator<BlockPos> it = right.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
        vector = new Vector(pos, 1, 0.5, 0);
        left = vector.rotateXZ(entity.rotationYaw + 90, false);
        right = vector.rotateXZ(entity.rotationYaw - 90, false);
        for (Iterator<BlockPos> it = left.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
        for (Iterator<BlockPos> it = right.line(0.1); it.hasNext(); ) {
            BlockPos spot = it.next();
            entity.world.spawnParticle(EnumParticleTypes.FLAME, spot.getX(), spot.getY(), spot.getZ(), 0, 0, 0);
        }
    }
}
