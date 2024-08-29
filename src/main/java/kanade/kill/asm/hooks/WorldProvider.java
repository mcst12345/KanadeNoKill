package kanade.kill.asm.hooks;

import kanade.kill.Config;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class WorldProvider {
    @SideOnly(Side.CLIENT)
    public static Vec3d getSkyColor(net.minecraft.world.WorldProvider provider, net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        if (Config.skyColor) {
            return new Vec3d(new Random().nextDouble(), new Random().nextDouble(), new Random().nextDouble());
        }
        return provider.world.getSkyColorBody(cameraEntity, partialTicks);
    }
}
