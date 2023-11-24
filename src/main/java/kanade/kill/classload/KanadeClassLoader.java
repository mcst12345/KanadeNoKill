package kanade.kill.classload;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.net.URL;

public class KanadeClassLoader extends LaunchClassLoader {
    public KanadeClassLoader(URL[] sources) {
        super(sources);
    }
}
