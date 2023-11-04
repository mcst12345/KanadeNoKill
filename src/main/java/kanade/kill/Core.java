package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Core implements IFMLLoadingPlugin {
    public static final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    public static final List<IClassTransformer> lists;

    static {
        try {
            InputStream is = Empty.class.getResourceAsStream("/kanade/kill/Util.class");

            byte[] clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.Util",Unsafe.instance.defineClass("kanade.kill.Util",clazz,0,clazz.length, Launch.classLoader,null));

            is = Empty.class.getResourceAsStream("/kanade/kill/EarlyFields.class");
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.EarlyFields",Unsafe.instance.defineClass("kanade.kill.EarlyFields",clazz,0,clazz.length, Launch.classLoader,null));

            is = Empty.class.getResourceAsStream("/kanade/kill/Transformer.class");
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.Transformer", Unsafe.instance.defineClass("kanade.kill.Transformer", clazz, 0, clazz.length, Launch.classLoader, null));

            is = Empty.class.getResourceAsStream("/kanade/kill/TransformerList.class");
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.TransformerList", Unsafe.instance.defineClass("kanade.kill.TransformerList", clazz, 0, clazz.length, Launch.classLoader, null));

            is = Empty.class.getResourceAsStream("/kanade/kill/CheckThread.class");
            clazz = new byte[is.available()];
            is.read(clazz);
            is.close();
            cachedClasses.put("kanade.kill.CheckThread", Unsafe.instance.defineClass("kanade.kill.CheckThread", clazz, 0, clazz.length, Launch.classLoader, null));

            Object old = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            lists = (List<IClassTransformer>) cachedClasses.get("kanade.kill.TransformerList").getConstructor(Collection.class).newInstance(old);
            Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, lists);

            Thread check = (Thread) cachedClasses.get("kanade.kill.CheckThread").newInstance();
            check.start();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
