package kanade.kill;

import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.ExceptionHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Core implements IFMLLoadingPlugin {
    static final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    public static List<IClassTransformer> lists;

    public static Logger LOGGER = LogManager.getLogger("Kanade");

    static {
        try {//EarlyMethods
            Core.LOGGER.info("Kanade Core loading.");

            final List<String> classes = new ArrayList<>();
            ProtectionDomain domain = Launch.class.getProtectionDomain();

            classes.add("kanade.kill.reflection.EarlyMethods");
            classes.add("kanade.kill.reflection.ReflectionUtil");
            classes.add("kanade.kill.reflection.EarlyFields");
            classes.add("kanade.kill.asm.ASMUtil");
            classes.add("kanade.kill.asm.injections.DimensionManager");
            classes.add("kanade.kill.asm.injections.Entity");
            classes.add("kanade.kill.asm.injections.EntityLivingBase");
            classes.add("kanade.kill.asm.injections.EntityPlayer");
            classes.add("kanade.kill.asm.injections.FMLClientHandler");
            classes.add("kanade.kill.asm.injections.ItemStack");
            classes.add("kanade.kill.asm.injections.Minecraft");
            classes.add("kanade.kill.asm.injections.MinecraftForge");
            classes.add("kanade.kill.asm.injections.MinecraftServer");
            classes.add("kanade.kill.asm.injections.NonNullList");
            classes.add("kanade.kill.asm.injections.RenderGlobal");
            classes.add("kanade.kill.asm.injections.World");
            classes.add("kanade.kill.asm.injections.WorldClient");
            classes.add("kanade.kill.asm.injections.WorldServer");
            classes.add("kanade.kill.asm.Transformer");
            classes.add("kanade.kill.util.TransformerList");
            classes.add("kanade.kill.thread.TransformersCheckThread");
            classes.add("kanade.kill.thread.ClassLoaderCheckThread");
            classes.add("kanade.kill.classload.KanadeClassLoader");
            classes.add("kanade.kill.util.FieldInfo");
            classes.add("kanade.kill.util.KanadeSecurityManager");
            classes.add("kanade.kill.util.ExceptionHandler");
            classes.add("kanade.kill.AgentMain");
            classes.add("kanade.kill.Attach");


            for (String s : classes) {
                Core.LOGGER.info("Defining class:" + s);
                try (InputStream is = Empty.class.getResourceAsStream('/' + s.replace('.', '/') + ".class")) {
                    assert is != null;
                    //6 lines below are from Apache common io.
                    final ByteArrayOutputStream output = new ByteArrayOutputStream();
                    final byte[] buffer = new byte[8024];
                    int n;
                    while (-1 != (n = is.read(buffer))) {
                        output.write(buffer, 0, n);
                    }
                    byte[] bytes = output.toByteArray();
                    cachedClasses.put(s, Unsafe.instance.defineClass(s, bytes, 0, bytes.length, Launch.classLoader, domain));
                }

            }


            Core.LOGGER.info("Injecting into LaunchClassLoader.");

            Object old = Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset);
            lists = (List<IClassTransformer>) cachedClasses.get("kanade.kill.util.TransformerList").getConstructor(Collection.class).newInstance(old);
            Unsafe.instance.putObjectVolatile(Launch.classLoader, EarlyFields.transformers_offset, lists);

            Core.LOGGER.info("Constructing check thread.");
            Thread check = (Thread) cachedClasses.get("kanade.kill.thread.TransformersCheckThread").newInstance();
            check.start();

            Core.LOGGER.info("Replacing exception handler.");
            Unsafe.instance.putObjectVolatile(Thread.currentThread(), EarlyFields.uncaughtExceptionHandler_offset, ExceptionHandler.instance);

            Core.LOGGER.info("Core loading completed.");
        } catch (Throwable e) {
            LOGGER.fatal(e);
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
