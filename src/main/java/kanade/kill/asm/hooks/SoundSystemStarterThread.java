package kanade.kill.asm.hooks;

import kanade.kill.reflection.EarlyFields;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;
import scala.concurrent.util.Unsafe;

public class SoundSystemStarterThread {
    public static boolean playing(SoundSystem sound, String s) {
        synchronized (SoundSystemConfig.THREAD_SYNC) {
            Library library = (Library) Unsafe.instance.getObjectVolatile(sound, EarlyFields.soundLibrary_offset);
            if (library == null || library.getSources() == null || s == null) {
                return false;
            } else {
                Source source = library.getSources().get(s);
                if (source == null) {
                    return false;
                } else {
                    return source.playing() || source.paused() || source.preLoad;
                }
            }
        }
    }
}
