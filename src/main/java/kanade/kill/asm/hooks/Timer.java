package kanade.kill.asm.hooks;

import kanade.kill.timemanagement.TimeStop;
import net.minecraft.client.Minecraft;

public class Timer {
    public static void updateTimer(net.minecraft.util.Timer timer) {
        if (TimeStop.isTimeStop()) {
            timer.renderPartialTicks = 0;
            return;
        }
        long i = Minecraft.getSystemTime();
        long j = i - timer.lastSyncSysClock;
        long k = System.nanoTime() / 1000000L;
        double d0 = (double) k / 1000.0D;

        if (j <= 1000L && j >= 0L) {
            timer.field_74285_i += j;

            if (timer.field_74285_i > 1000L) {
                long l = k - timer.lastSyncHRClock;
                double d1 = (double) timer.field_74285_i / (double) l;
                timer.timeSyncAdjustment += (d1 - timer.timeSyncAdjustment) * 0.20000000298023224D;
                timer.lastSyncHRClock = k;
                timer.field_74285_i = 0L;
            }

            if (timer.field_74285_i < 0L) {
                timer.lastSyncHRClock = k;
            }
        } else {
            timer.lastHRTime = d0;
        }

        timer.lastSyncSysClock = i;
        double d2 = (d0 - timer.lastHRTime) * timer.timeSyncAdjustment;
        timer.lastHRTime = d0;

        if (d2 < 0.0D) {
            d2 = 0.0D;
        }

        if (d2 > 1.0D) {
            d2 = 1.0D;
        }

        timer.elapsedPartialTicks = (float) ((double) timer.elapsedPartialTicks + d2 * (double) timer.timerSpeed * (double) timer.ticksPerSecond);
        timer.elapsedTicks = (int) timer.elapsedPartialTicks;
        timer.elapsedPartialTicks -= (float) timer.elapsedTicks;

        if (timer.elapsedTicks > 10) {
            timer.elapsedTicks = 10;
        }

        timer.renderPartialTicks = timer.elapsedPartialTicks;
    }
}
