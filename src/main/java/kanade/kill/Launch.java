package kanade.kill;

import cpw.mods.fml.common.Loader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.thread.ClassLoaderCheckThread;
import kanade.kill.thread.KillerThread;
import kanade.kill.thread.SecurityManagerCheckThread;
import kanade.kill.util.FieldInfo;
import kanade.kill.util.ObjectUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import scala.concurrent.util.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;

@SuppressWarnings("unchecked")
public class Launch {
    public static final boolean funny = System.getProperty("Vanish") != null && System.getProperty("Kanade").equalsIgnoreCase("true");
    public static final LaunchClassLoader classLoader;
    public static final boolean client = System.getProperty("minecraft.client.jar") != null;
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    private static final ThreadGroup Kanade = new ThreadGroup("Kanade");
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String, Object> blackboard;
    public static List<IClassTransformer> lists;
    public static Logger LOGGER = LogManager.getLogger("Kanade");
    public static final boolean Debug = System.getProperty("Debug") != null;
    public static final boolean win = System.getProperty("os.name").startsWith("Windows");

    static {


        File file = new File("KanadeAgent" + (win ? ".dll" : ".so"));
        System.load(file.getAbsolutePath());

        final URLClassLoader ucl = (URLClassLoader) Loader.class.getClassLoader();
        classLoader = new KanadeClassLoader(ucl.getURLs());
        blackboard = new HashMap<>();
        Thread.currentThread().setContextClassLoader(classLoader);

        final List<String> classes = new ArrayList<>();
        ProtectionDomain domain = Loader.class.getProtectionDomain();
        ProtectionDomain gl = client ? GL11.class.getProtectionDomain() : null;
        ClassLoader glLoader = client ? GL11.class.getClassLoader() : null;

        classes.add("kanade.kill.util.ObjectUtil");
        classes.add("kanade.kill.Config");
        classes.add("kanade.kill.util.Util");
        classes.add("kanade.kill.util.KanadeArrayList");
        classes.add("kanade.kill.reflection.EarlyMethods");
        classes.add("kanade.kill.reflection.ReflectionUtil");
        classes.add("kanade.kill.reflection.EarlyFields");
        classes.add("kanade.kill.util.ClassUtil");
        classes.add("kanade.kill.util.UnsafeAccessor");
        classes.add("kanade.kill.asm.ASMUtil");
        classes.add("kanade.kill.asm.hooks.EventBus");
        classes.add("kanade.kill.asm.hooks.Entity");
        classes.add("kanade.kill.asm.hooks.World");
        classes.add("kanade.kill.asm.injections.Chunk");
        classes.add("kanade.kill.asm.injections.DimensionManager");
        classes.add("kanade.kill.asm.injections.Entity");
        classes.add("kanade.kill.asm.injections.EntityLivingBase");
        classes.add("kanade.kill.asm.injections.EntityPlayer");
        classes.add("kanade.kill.asm.injections.Event");
        classes.add("kanade.kill.asm.injections.EventBus");
        classes.add("kanade.kill.asm.injections.FMLClientHandler");
        classes.add("kanade.kill.asm.injections.ForgeHooksClient");
        classes.add("kanade.kill.asm.injections.ItemStack");
        classes.add("kanade.kill.asm.injections.Minecraft");
        classes.add("kanade.kill.asm.injections.MinecraftForge");
        classes.add("kanade.kill.asm.injections.MinecraftServer");
        classes.add("kanade.kill.asm.injections.MouseHelper");
        classes.add("kanade.kill.asm.injections.NetHandlerPlayServer");
        classes.add("kanade.kill.asm.injections.RenderGlobal");
        classes.add("kanade.kill.asm.injections.ServerCommandManager");
        classes.add("kanade.kill.asm.injections.Timer");
        classes.add("kanade.kill.asm.injections.World");
        classes.add("kanade.kill.asm.injections.WorldClient");
        classes.add("kanade.kill.asm.injections.WorldServer");
        classes.add("kanade.kill.asm.Transformer");
        classes.add("kanade.kill.util.TransformerList");
        classes.add("kanade.kill.thread.ClassLoaderCheckThread");
        classes.add("kanade.kill.thread.FieldSaveThread");
        classes.add("kanade.kill.timemanagement.TimeStop");
        classes.add("kanade.kill.classload.KanadeClassLoader");
        classes.add("kanade.kill.util.FieldInfo");
        classes.add("kanade.kill.util.FileUtils");
        classes.add("kanade.kill.util.KanadeSecurityManager");
        classes.add("kanade.kill.thread.SecurityManagerCheckThread");
        classes.add("kanade.kill.thread.KillerThread");
        if (client) {
            classes.add("kanade.kill.asm.hooks.Timer");
            classes.add("kanade.kill.asm.hooks.Minecraft");
            classes.add("kanade.kill.asm.hooks.MouseHelper");
            classes.add("kanade.kill.thread.DisplayGui");
            classes.add("org.lwjgl.opengl.GLOffsets");
            classes.add("org.lwjgl.opengl.OpenGLHelper");
        }

        try {
            for (String s : classes) {
                LOGGER.info("Defining class:" + s);
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
                    Class<?> Clazz;
                    if (s.startsWith("org.lwjgl")) {
                        Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, glLoader, gl);
                    } else {
                        Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, classLoader, domain);
                    }
                    ((Map<String, Class<?>>) Unsafe.instance.getObjectVolatile(classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);
                }
            }
        } catch (Throwable t) {
            LOGGER.fatal("Failed tp launch!", t);
            LOGGER.fatal(t.getCause());
            throw new RuntimeException(t);
        }

        Thread thread = new ClassLoaderCheckThread(Kanade);
        thread.start();
        thread = new SecurityManagerCheckThread(Kanade);
        thread.start();
        thread = new KillerThread(Kanade);
        thread.start();

        net.minecraft.launchwrapper.Launch.classLoader = classLoader;
        net.minecraft.launchwrapper.Launch.blackboard = blackboard;

        Object o = ObjectUtil.generateObject(FieldInfo.class);
        try {
            Field field = o.getClass().getDeclaredField("clazz");
            field.setAccessible(true);
            String s = (String) field.get(o);
            System.out.println(s.length());
            field = o.getClass().getDeclaredField("field");
            field.setAccessible(true);
            s = (String) field.get(o);
            System.out.println(s.length());
        } catch (Throwable e) {
            LOGGER.info(e);
        }
    }

    private Launch() {
    }

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    private void launch(String[] args) {
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        final OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        final OptionSpec<File> assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg().ofType(File.class);
        final OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg().defaultsTo(DEFAULT_TWEAK);
        final OptionSpec<String> nonOption = parser.nonOptions();

        final OptionSet options = parser.parse(args);
        minecraftHome = options.valueOf(gameDirOption);
        net.minecraft.launchwrapper.Launch.minecraftHome = minecraftHome;
        assetsDir = options.valueOf(assetsDirOption);
        net.minecraft.launchwrapper.Launch.assetsDir = assetsDir;
        final String profileName = options.valueOf(profileOption);
        final List<String> tweakClassNames = new ArrayList<>(options.valuesOf(tweakClassOption));

        final List<String> argumentList = new ArrayList<>();
        // This list of names will be interacted with through tweakers. They can append to this list
        // any 'discovered' tweakers from their preferred mod loading mechanism
        // By making this object discoverable and accessible it's possible to perform
        // things like cascading of tweakers
        blackboard.put("TweakClasses", tweakClassNames);

        // This argument list will be constructed from all tweakers. It is visible here so
        // all tweakers can figure out if a particular argument is present, and add it if not
        blackboard.put("ArgumentList", argumentList);

        // This is to prevent duplicates - in case a tweaker decides to add itself or something
        final Set<String> allTweakerNames = new HashSet<>();
        // The 'definitive' list of tweakers
        final List<ITweaker> allTweakers = new ArrayList<>();
        try {
            final List<ITweaker> tweakers = new ArrayList<>(tweakClassNames.size() + 1);
            // The list of tweak instances - may be useful for interoperability
            blackboard.put("Tweaks", tweakers);
            // The primary tweaker (the first one specified on the command line) will actually
            // be responsible for providing the 'main' name and generally gets called first
            ITweaker primaryTweaker = null;
            // This loop will terminate, unless there is some sort of pathological tweaker
            // that reinserts itself with a new identity every pass
            // It is here to allow tweakers to "push" new tweak classes onto the 'stack' of
            // tweakers to evaluate allowing for cascaded discovery and injection of tweakers
            do {
                for (final Iterator<String> it = tweakClassNames.iterator(); it.hasNext(); ) {
                    final String tweakName = it.next();
                    // Safety check - don't reprocess something we've already visited
                    if (allTweakerNames.contains(tweakName)) {
                        LogWrapper.log(Level.WARN, "Tweak class name %s has already been visited -- skipping", tweakName);
                        // remove the tweaker from the stack otherwise it will create an infinite loop
                        it.remove();
                        continue;
                    } else {
                        allTweakerNames.add(tweakName);
                    }
                    LogWrapper.log(Level.INFO, "Loading tweak class name %s", tweakName);

                    // Ensure we allow the tweak class to load with the parent classloader
                    classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf('.')));
                    final ITweaker tweaker = (ITweaker) Class.forName(tweakName, true, classLoader).newInstance();
                    tweakers.add(tweaker);

                    // Remove the tweaker from the list of tweaker names we've processed this pass
                    it.remove();
                    // If we haven't visited a tweaker yet, the first will become the 'primary' tweaker
                    if (primaryTweaker == null) {
                        LogWrapper.log(Level.INFO, "Using primary tweak class name %s", tweakName);
                        primaryTweaker = tweaker;
                    }
                }

                // Now, iterate all the tweakers we just instantiated
                for (final Iterator<ITweaker> it = tweakers.iterator(); it.hasNext(); ) {
                    final ITweaker tweaker = it.next();
                    LogWrapper.log(Level.INFO, "Calling tweak class %s", tweaker.getClass().getName());
                    try {
                        tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                        tweaker.injectIntoClassLoader(classLoader);
                        allTweakers.add(tweaker);
                        // again, remove from the list once we've processed it, so we don't get duplicates
                        it.remove();
                    } catch (Throwable t) {
                        LOGGER.warn("Catch exception:", t);
                    }
                }
                // continue around the loop until there's no tweak classes
            } while (!tweakClassNames.isEmpty());

            // Once we're done, we then ask all the tweakers for their arguments and add them all to the
            // master argument list
            for (final ITweaker tweaker : allTweakers) {
                argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments()));
            }

            // Finally we turn to the primary tweaker, and let it tell us where to go to launch
            final String launchTarget = client ? "kanade.kill.ClientMain" : "net.minecraft.server.MinecraftServer";
            //net.minecraft.server.MinecraftServer
            final Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            final Method mainMethod = clazz.getMethod("main", String[].class);

            LogWrapper.info("Launching wrapped minecraft {%s}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[0]));
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "Unable to launch");
            System.exit(1);
        }
    }
}
