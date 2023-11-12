package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Core implements IFMLLoadingPlugin {
    public static final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    public static final List<IClassTransformer> lists;

    static {
        try {//EarlyMethods
            System.out.println("Kanade Core loading.");

            InputStream is;
            byte[] clazz;

            is = Empty.class.getResourceAsStream("/kanade/kill/FileUtil.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            cachedClasses.put("kanade.kill.FileUtil", Unsafe.instance.defineClass("kanade.kill.FileUtil", clazz, 0, clazz.length, Launch.classLoader, null));

            System.out.println("Extracting files.");
            is = Empty.class.getResourceAsStream("/kanade/kill/Util.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.Util.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/EarlyFields.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.EarlyFields.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/Transformer.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.Transformer.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/TransformerList.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.TransformerList.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/CheckThread.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.CheckThread.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/ASMUtil.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.ASMUtil.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/EarlyMethods.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.EarlyMethods.class"));

            System.out.println("Defining classes.");

            FileInputStream fis;

            fis = new FileInputStream("Kanade.EarlyFields.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.EarlyFields", Unsafe.instance.defineClass("kanade.kill.EarlyFields", clazz, 0, clazz.length, Launch.classLoader, null));

            fis = new FileInputStream("Kanade.EarlyMethods.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.EarlyMethods", Unsafe.instance.defineClass("kanade.kill.EarlyMethods", clazz, 0, clazz.length, Launch.classLoader, null));

            fis = new FileInputStream("Kanade.Util.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.Util",Unsafe.instance.defineClass("kanade.kill.Util",clazz,0,clazz.length, Launch.classLoader,null));

            fis = new FileInputStream("Kanade.ASMUtil.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.ASMUtil", Unsafe.instance.defineClass("kanade.kill.ASMUtil", clazz, 0, clazz.length, Launch.classLoader, null));

            fis = new FileInputStream("Kanade.Transformer.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.Transformer", Unsafe.instance.defineClass("kanade.kill.Transformer", clazz, 0, clazz.length, Launch.classLoader, null));

            fis = new FileInputStream("Kanade.TransformerList.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.TransformerList", Unsafe.instance.defineClass("kanade.kill.TransformerList", clazz, 0, clazz.length, Launch.classLoader, null));

            fis = new FileInputStream("Kanade.CheckThread.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.CheckThread", Unsafe.instance.defineClass("kanade.kill.CheckThread", clazz, 0, clazz.length, Launch.classLoader, null));

            System.out.println("Injecting into LaunchClassLoader.");

            Object old = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            lists = (List<IClassTransformer>) cachedClasses.get("kanade.kill.TransformerList").getConstructor(Collection.class).newInstance(old);
            Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, lists);

            System.out.println("Constructing check thread.");
            Thread check = (Thread) cachedClasses.get("kanade.kill.CheckThread").newInstance();
            check.start();

            System.out.println("Core loading completed.");
        } catch (Throwable e) {
            e.printStackTrace();
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
