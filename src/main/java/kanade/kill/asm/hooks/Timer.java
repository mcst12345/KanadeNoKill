package kanade.kill.asm.hooks;

import kanade.kill.timemanagement.TimeStop;
import net.minecraft.client.Minecraft;

public class Timer {
    public static void updateTimer(net.minecraft.util.Timer timer) {
        if (TimeStop.isTimeStop()) {
            timer.renderPartialTicks = 0;
            timer.elapsedTicks = 40;
            return;
        }
        long i = Minecraft.getSystemTime();
        timer.elapsedPartialTicks = (float) (i - timer.lastSyncSysClock) / timer.tickLength;
        timer.lastSyncSysClock = i;
        timer.renderPartialTicks += timer.elapsedPartialTicks;
        timer.elapsedTicks = (int) timer.renderPartialTicks;
        timer.renderPartialTicks -= (float) timer.elapsedTicks;
    }
}
