package kanade.kill;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.thread.ClassLoaderCheckThread;
import kanade.kill.thread.KillerThread;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.ObjectUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import scala.concurrent.util.Unsafe;
import sun.reflect.Reflection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.*;

@SuppressWarnings("unchecked")
public class Launch {
    public static final String deathImage = "KanadeDeath.png";
    public static final boolean funny = System.getProperty("Vanish") != null && System.getProperty("Kanade").equalsIgnoreCase("true");
    public static final LaunchClassLoader classLoader;
    public static final boolean client = System.getProperty("minecraft.client.jar") != null;
    public static final int BUFFER_SIZE = 1 << 12;
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    private static final ThreadGroup Kanade = new ThreadGroup("Kanade");
    private static final ThreadLocal<byte[]> loadBuffer = new ThreadLocal<>();
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String, Object> blackboard;
    public static List<IClassTransformer> lists;
    public static Logger LOGGER = LogManager.getLogger("Kanade");
    public static final boolean Debug = System.getProperty("Debug") != null;
    public static final boolean win = System.getProperty("os.name").startsWith("Windows");
    public static final boolean betterCompatible = System.getProperty("BetterCompatible") != null;
    public static final Instrumentation INSTRUMENTATION;
    public static final List<String> classes = new ArrayList<>();
    public static final List<String> late_classes = new ArrayList<>();

    static {
        System.out.println(FMLDeobfuscatingRemapper.class.getClassLoader().getClass().getName());
        if (client) {
            try (InputStream is = Empty.class.getResourceAsStream("/KanadeDeath.png")) {
                assert is != null;
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final byte[] buffer = new byte[8024];
                int n;
                while (-1 != (n = is.read(buffer))) {
                    output.write(buffer, 0, n);
                }
                byte[] bytes = output.toByteArray();
                File file = new File("KanadeDeath.png");
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
                Files.write(file.toPath(), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File file = new File("KanadeAgent" + (win ? ".dll" : ".so"));
        System.load(file.getAbsolutePath());
        NativeMethods.Test("12345:)");
        NativeMethods.DeadAdd(114514);
        System.out.println(NativeMethods.DeadContain(114514));
        System.out.println(NativeMethods.DeadContain(39));

        final URLClassLoader ucl = (URLClassLoader) Loader.class.getClassLoader();
        classLoader = new KanadeClassLoader(ucl.getURLs());
        blackboard = new HashMap<>();
        Thread.currentThread().setContextClassLoader(classLoader);


        ProtectionDomain domain = Loader.class.getProtectionDomain();
        ProtectionDomain gl = client ? GL11.class.getProtectionDomain() : null;
        ClassLoader appClassLoader = client ? GL11.class.getClassLoader() : null;

        if (Launch.client) {
            late_classes.add("kanade.kill.util.BufferBuilder");
            late_classes.add("kanade.kill.util.DefaultVertexFormats");
            late_classes.add("kanade.kill.util.FontRenderer");
            late_classes.add("kanade.kill.util.VertexFormat");
            late_classes.add("kanade.kill.util.VertexFormatElement");
            late_classes.add("kanade.kill.util.ClientFakeObjects");
            late_classes.add("kanade.kill.Keys");
            late_classes.add("kanade.kill.render.RenderBeaconBeam");
            late_classes.add("kanade.kill.util.KanadeFontRender");
            late_classes.add("kanade.kill.util.VertexFormatElement$EnumType");
            late_classes.add("kanade.kill.util.VertexFormatElement$EnumUsage");
            late_classes.add("kanade.kill.util.VertexFormat$1");
            late_classes.add("kanade.kill.render.WingLayer");
        }


        late_classes.add("kanade.kill.entity.Lain");
        late_classes.add("kanade.kill.entity.EntityBeaconBeam");
        late_classes.add("kanade.kill.util.EntityUtil");
        late_classes.add("kanade.kill.item.KillItem");
        late_classes.add("kanade.kill.item.DeathItem");
        late_classes.add("kanade.kill.reflection.LateFields");
        late_classes.add("kanade.kill.network.NetworkHandler");
        late_classes.add("kanade.kill.network.packets.Annihilation");
        late_classes.add("kanade.kill.network.packets.Annihilation$MessageHandler");
        late_classes.add("kanade.kill.network.packets.ClientTimeStop");
        late_classes.add("kanade.kill.network.packets.ClientTimeStop$MessageHandler");
        late_classes.add("kanade.kill.network.packets.CoreDump");
        late_classes.add("kanade.kill.network.packets.CoreDump$MessageHandler");
        late_classes.add("kanade.kill.network.packets.KillCurrentPlayer");
        late_classes.add("kanade.kill.network.packets.KillCurrentPlayer$MessageHandler");
        late_classes.add("kanade.kill.network.packets.KillEntity");
        late_classes.add("kanade.kill.network.packets.KillEntity$MessageHandler");
        late_classes.add("kanade.kill.network.packets.ServerTimeStop");
        late_classes.add("kanade.kill.network.packets.ServerTimeStop$MessageHandler");
        late_classes.add("kanade.kill.network.packets.SwitchTimePoint");
        late_classes.add("kanade.kill.network.packets.SwitchTimePoint$MessageHandler");
        late_classes.add("kanade.kill.network.packets.SaveTimePoint$MessageHandler");
        late_classes.add("kanade.kill.network.packets.SaveTimePoint");
        late_classes.add("kanade.kill.network.packets.TimeBack$MessageHandler");
        late_classes.add("kanade.kill.network.packets.TimeBack");
        late_classes.add("kanade.kill.network.packets.ClientReload$MessageHandler");
        late_classes.add("kanade.kill.network.packets.ClientReload");
        late_classes.add("kanade.kill.network.packets.BlackHole");
        late_classes.add("kanade.kill.network.packets.BlackHole$MessageHandler");
        late_classes.add("kanade.kill.network.packets.ConfigUpdatePacket");
        late_classes.add("kanade.kill.network.packets.ConfigUpdatePacket$MessageHandler");
        late_classes.add("kanade.kill.network.packets.UpdatePlayerProtectedState");
        late_classes.add("kanade.kill.network.packets.UpdatePlayerProtectedState$MessageHandler");
        late_classes.add("kanade.kill.network.packets.KillAll");
        late_classes.add("kanade.kill.network.packets.KillAll$MessageHandler");
        late_classes.add("kanade.kill.network.packets.UpdateSuperMode");
        late_classes.add("kanade.kill.network.packets.UpdateSuperMode$MessageHandler");
        late_classes.add("kanade.kill.network.packets.Reset");
        late_classes.add("kanade.kill.network.packets.Reset$MessageHandler");
        late_classes.add("kanade.kill.command.KanadeKillCommand");
        late_classes.add("kanade.kill.command.DebugCommand");
        late_classes.add("kanade.kill.command.KanadeReflection");
        late_classes.add("kanade.kill.network.packets.KillAllEntities");
        late_classes.add("kanade.kill.network.packets.KillAllEntities$MessageHandler");
        late_classes.add("kanade.kill.network.packets.UpdateTickCount");
        late_classes.add("kanade.kill.network.packets.UpdateTickCount$MessageHandler");

        classes.add("me.xdark.shell.JVMUtil");
        classes.add("me.xdark.shell.NativeLibrary");
        classes.add("me.xdark.shell.ShellcodeRunner");
        classes.add("one.helfy.Field");
        classes.add("one.helfy.JVM");
        classes.add("one.helfy.JVMException");
        classes.add("one.helfy.Type");
        classes.add("kanade.kill.util.memory.MemoryHelper");
        classes.add("kanade.kill.util.UnsafeFucker");
        classes.add("kanade.kill.classload.FakeClassLoadr");
        classes.add("kanade.kill.util.NumberUtil");
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
        classes.add("kanade.kill.asm.hooks.ClassInheritanceMultiMap");
        classes.add("kanade.kill.asm.hooks.DimensionManager");
        classes.add("kanade.kill.asm.hooks.Entity");
        classes.add("kanade.kill.asm.hooks.EntityTracker");
        classes.add("kanade.kill.asm.hooks.EventBus");
        classes.add("kanade.kill.asm.hooks.FMLModContainer");
        classes.add("kanade.kill.asm.hooks.ItemStack");
        classes.add("kanade.kill.asm.hooks.MinecraftServer");
        classes.add("kanade.kill.asm.hooks.ModClassLoader");
        classes.add("kanade.kill.asm.hooks.SimpleChannelHandlerWrapper");
        classes.add("kanade.kill.asm.hooks.World");
        classes.add("kanade.kill.asm.hooks.WorldServer");
        classes.add("kanade.kill.asm.injections.Chunk");
        classes.add("kanade.kill.asm.injections.ClassInheritanceMultiMap");
        classes.add("kanade.kill.asm.injections.DimensionManager");
        classes.add("kanade.kill.asm.injections.Entity");
        classes.add("kanade.kill.asm.injections.EntityLivingBase");
        classes.add("kanade.kill.asm.injections.EntityPlayer");
        classes.add("kanade.kill.asm.injections.EntityRenderer");
        classes.add("kanade.kill.asm.injections.EntityTracker");
        classes.add("kanade.kill.asm.injections.Event");
        classes.add("kanade.kill.asm.injections.EventBus");
        classes.add("kanade.kill.asm.injections.FMLClientHandler");
        classes.add("kanade.kill.asm.injections.FMLModContainer");
        classes.add("kanade.kill.asm.injections.ForgeHooksClient");
        classes.add("kanade.kill.asm.injections.GuiMainMenu");
        classes.add("kanade.kill.asm.injections.ItemStack");
        classes.add("kanade.kill.asm.injections.KeyBinding");
        classes.add("kanade.kill.asm.injections.Minecraft");
        classes.add("kanade.kill.asm.injections.MinecraftForge");
        classes.add("kanade.kill.asm.injections.MinecraftServer");
        classes.add("kanade.kill.asm.injections.ModClassLoader");
        classes.add("kanade.kill.asm.injections.MouseHelper");
        classes.add("kanade.kill.asm.injections.NetHandlerPlayServer");
        classes.add("kanade.kill.asm.injections.NonNullList");
        classes.add("kanade.kill.asm.injections.RenderGlobal");
        classes.add("kanade.kill.asm.injections.RenderItem");
        classes.add("kanade.kill.asm.injections.RenderLivingBase");
        classes.add("kanade.kill.asm.injections.RenderManager");
        classes.add("kanade.kill.asm.injections.RenderPlayer");
        classes.add("kanade.kill.asm.injections.ServerCommandManager");
        classes.add("kanade.kill.asm.injections.SimpleChannelHandlerWrapper");
        classes.add("kanade.kill.asm.injections.Timer");
        classes.add("kanade.kill.asm.injections.World");
        classes.add("kanade.kill.asm.injections.WorldClient");
        classes.add("kanade.kill.asm.injections.WorldServer");
        classes.add("kanade.kill.asm.Transformer");
        classes.add("kanade.kill.util.TransformerList");
        classes.add("kanade.kill.thread.ClassLoaderCheckThread");
        classes.add("kanade.kill.timemanagement.TimeStop");
        classes.add("kanade.kill.timemanagement.TimeBack");
        classes.add("kanade.kill.util.FileUtils");
        classes.add("kanade.kill.util.KanadeSecurityManager");
        classes.add("kanade.kill.thread.KillerThread");
        classes.add("kanade.kill.ModMain");
        classes.add("kanade.kill.ModMain$1");

        if (client) {
            classes.add("kanade.kill.ClientMain");
            classes.add("kanade.kill.asm.hooks.Timer");
            classes.add("kanade.kill.asm.hooks.RenderItem");
            classes.add("kanade.kill.asm.hooks.GuiMainMenu");
            classes.add("kanade.kill.asm.hooks.ItemStackClient");
            classes.add("kanade.kill.asm.hooks.Minecraft");
            classes.add("kanade.kill.asm.hooks.Minecraft$1");
            classes.add("kanade.kill.asm.hooks.MouseHelper");
            classes.add("kanade.kill.asm.hooks.RenderLivingBase");
            classes.add("kanade.kill.thread.DisplayGui");
            classes.add("org.lwjgl.opengl.GLOffsets");
            classes.add("org.lwjgl.opengl.OpenGLHelper");
            classes.add("kanade.kill.asm.hooks.SoundSystemStarterThread");
            classes.add("kanade.kill.util.superRender.ImagePanel");
            classes.add("kanade.kill.util.superRender.DeathWindow");
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
                        Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, appClassLoader, gl);
                    } else if (s.startsWith("net.minecraftforge.fml.relauncher")) {
                        Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, appClassLoader, domain);
                        Unsafe.instance.defineClass(s, bytes, 0, bytes.length, classLoader, domain);
                    } else {
                        Clazz = Unsafe.instance.defineClass(s, bytes, 0, bytes.length, classLoader, domain);
                    }
                    ((Map<String, Class>) Unsafe.instance.getObjectVolatile(classLoader, EarlyFields.cachedClasses_offset)).put(s, Clazz);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        Thread thread = new ClassLoaderCheckThread(Kanade);
        thread.start();
        thread = new KillerThread(Kanade);
        thread.start();

        net.minecraft.launchwrapper.Launch.classLoader = classLoader;
        net.minecraft.launchwrapper.Launch.blackboard = blackboard;

        INSTRUMENTATION = NativeMethods.ConstructInst();

        LOGGER.info("Fucking reflection filters.");
        try {
            Field filter = ReflectionUtil.getField(Reflection.class,"fieldFilterMap");
            ObjectUtil.putStatic(filter,null);
            filter = ReflectionUtil.getField(Reflection.class,"methodFilterMap");
            ObjectUtil.putStatic(filter,null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        //INSTRUMENTATION.addTransformer(Transformer.instance);
    }

    private Launch() {
    }

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    public static byte[] readFully(InputStream stream) {
        try {
            byte[] buffer = getOrCreateBuffer();

            int read;
            int totalLength = 0;
            while ((read = stream.read(buffer, totalLength, buffer.length - totalLength)) != -1) {
                totalLength += read;

                // Extend our buffer
                if (totalLength >= buffer.length - 1) {
                    byte[] newBuffer = new byte[buffer.length + BUFFER_SIZE];
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    buffer = newBuffer;
                }
            }

            final byte[] result = new byte[totalLength];
            System.arraycopy(buffer, 0, result, 0, totalLength);
            return result;
        } catch (Throwable t) {
            LogWrapper.log(Level.WARN, t, "Problem loading class");
            return new byte[0];
        }
    }

    private static byte[] getOrCreateBuffer() {
        byte[] buffer = loadBuffer.get();
        if (buffer == null) {
            loadBuffer.set(new byte[BUFFER_SIZE]);
            buffer = loadBuffer.get();
        }
        return buffer;
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
