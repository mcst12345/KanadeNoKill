package kanade.kill.asm.hooks;

import kanade.kill.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class Entity {
    public static void applyEntityCollision(net.minecraft.entity.Entity e, net.minecraft.entity.Entity p_70108_1_) {
        if (e instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e;
            if (EntityUtil.blackHolePlayers.contains(player.getUniqueID())) {
                EntityUtil.SafeKill(p_70108_1_, false);
                return;
            } else if (player.isPlayerSleeping()) {
                return;
            }
        }
        if (p_70108_1_.riddenByEntity != e && p_70108_1_.ridingEntity != e) {
            double d0 = p_70108_1_.posX - e.posX;
            double d1 = p_70108_1_.posZ - e.posZ;
            double d2 = MathHelper.abs_max(d0, d1);

            if (d2 >= 0.009999999776482582D) {
                d2 = MathHelper.sqrt_double(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                d0 *= 1.0F - e.entityCollisionReduction;
                d1 *= 1.0F - e.entityCollisionReduction;
                e.addVelocity(-d0, 0.0D, -d1);
                p_70108_1_.addVelocity(d0, 0.0D, d1);
            }
        }
    }
}
