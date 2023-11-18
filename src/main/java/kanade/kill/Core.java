package kanade.kill;

import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.FileUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Core implements IFMLLoadingPlugin {
    static final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    public static List<IClassTransformer> lists;

    static {
        try {//EarlyMethods
            System.out.println("Kanade Core loading.");

            InputStream is;
            byte[] clazz;
            ProtectionDomain domain = Launch.class.getProtectionDomain();

            is = Empty.class.getResourceAsStream("/kanade/kill/util/FileUtil.class");
            assert is != null;
            clazz = new byte[is.available()];
            is.read(clazz);
            cachedClasses.put("kanade.kill.util.FileUtil", Unsafe.instance.defineClass("kanade.kill.util.FileUtil", clazz, 0, clazz.length, Launch.classLoader, domain));

            System.out.println("Extracting files.");
            is = Empty.class.getResourceAsStream("/kanade/kill/util/Util.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.Util.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/reflection/EarlyFields.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.EarlyFields.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/asm/Transformer.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.Transformer.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/util/TransformerList.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.TransformerList.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/util/CheckThread.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.CheckThread.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/asm/ASMUtil.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.ASMUtil.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/reflection/EarlyMethods.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.EarlyMethods.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/util/KanadeSecurityManager.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.KanadeSecurityManager.class"));
            is = Empty.class.getResourceAsStream("/kanade/kill/util/FieldInfo.class");
            FileUtil.copyInputStreamToFile(is, new File("Kanade.FieldInfo.class"));

            System.out.println("Defining classes.");

            FileInputStream fis;

            fis = new FileInputStream("Kanade.FieldInfo.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.util.FieldInfo", Unsafe.instance.defineClass("kanade.kill.util.FieldInfo", clazz, 0, clazz.length, Launch.classLoader, domain));


            fis = new FileInputStream("Kanade.KanadeSecurityManager.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.util.KanadeSecurityManager", Unsafe.instance.defineClass("kanade.kill.util.KanadeSecurityManager", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.EarlyMethods.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.reflection.EarlyMethods", Unsafe.instance.defineClass("kanade.kill.reflection.EarlyMethods", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.EarlyFields.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.reflection.EarlyFields", Unsafe.instance.defineClass("kanade.kill.reflection.EarlyFields", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.Util.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.util.Util", Unsafe.instance.defineClass("kanade.kill.util.Util", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.ASMUtil.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.asm.ASMUtil", Unsafe.instance.defineClass("kanade.kill.asm.ASMUtil", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.Transformer.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.asm.Transformer", Unsafe.instance.defineClass("kanade.kill.asm.Transformer", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.TransformerList.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.util.TransformerList", Unsafe.instance.defineClass("kanade.kill.util.TransformerList", clazz, 0, clazz.length, Launch.classLoader, domain));

            fis = new FileInputStream("Kanade.CheckThread.class");
            clazz = new byte[fis.available()];
            fis.read(clazz);
            fis.close();
            cachedClasses.put("kanade.kill.util.CheckThread", Unsafe.instance.defineClass("kanade.kill.util.CheckThread", clazz, 0, clazz.length, Launch.classLoader, domain));

            System.out.println("Injecting into LaunchClassLoader.");

            Object old = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            lists = (List<IClassTransformer>) cachedClasses.get("kanade.kill.util.TransformerList").getConstructor(Collection.class).newInstance(old);
            Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, lists);

            System.out.println("Constructing check thread.");
            Thread check = (Thread) cachedClasses.get("kanade.kill.util.CheckThread").newInstance();
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
