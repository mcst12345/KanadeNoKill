package kanade.kill.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

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
}
