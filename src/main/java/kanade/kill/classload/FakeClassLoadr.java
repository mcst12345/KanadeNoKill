package kanade.kill.classload;

import kanade.kill.Launch;

import java.util.HashMap;
import java.util.Map;

public class FakeClassLoadr extends ClassLoader {
    public static final FakeClassLoadr INSTANCE = new FakeClassLoadr();
    final Map<String, Class<?>> cache = new HashMap<>();

    private FakeClassLoadr() {
    }

    public static void putCache(String name, Class<?> clazz) {
        INSTANCE.cache.put(name, clazz);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        return Launch.classLoader.findClass(name);
    }
}
