package kanade.kill;

import com.google.common.io.ByteStreams;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.thread.ClassLoaderCheckThread;
import kanade.kill.thread.KillerThread;
import kanade.kill.thread.SecurityManagerThread;
import kanade.kill.util.JarProcessor;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.ObjectUtil;
import miku.lib.HSDB.SaJDI;
import miku.lib.utils.NoPrivateOrProtected;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraftforge.fml.common.Loader;
import one.helfy.JVM;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;
import paulscode.sound.SoundSystem;
import scala.concurrent.util.Unsafe;
import sun.misc.URLClassPath;
import sun.reflect.Reflection;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Launch {
    private static boolean LainMode = false;
    public static boolean LainModeEnabled() {
        return LainMode;
    }
    public static final String deathImage = "KanadeDeath.png";
    public static final boolean funny = System.getProperty("Vanish") != null && System.getProperty("Kanade").equalsIgnoreCase("true");
    public static final LaunchClassLoader classLoader;
    public static final boolean client;
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

    public static void Lain() {
        LainMode = true;
    }

    private Launch() {
    }

    public static final String JAR;

    static {
        client = System.getProperty("sun.java.command").contains("--uuid");
        SaJDI.appendJar();
        JVM.InitVtableCaches();
        try {
            NoPrivateOrProtected.FuckAccess(System.class);
            NoPrivateOrProtected.FuckAccess(Class.class);
            NoPrivateOrProtected.FuckAccess(Method.class);
            NoPrivateOrProtected.FuckAccess(Field.class);
            NoPrivateOrProtected.FuckAccess(ClassLoader.class);
            NoPrivateOrProtected.FuckAccess(URLClassLoader.class);
            NoPrivateOrProtected.FuckAccess(SecureClassLoader.class);
            NoPrivateOrProtected.FuckAccess(URLClassPath.class);
            NoPrivateOrProtected.FuckAccess(Thread.class);
            NoPrivateOrProtected.FuckAccess(ThreadGroup.class);
            NoPrivateOrProtected.FuckAccess(Class.forName("sun.reflect.NativeMethodAccessorImpl"));
            NoPrivateOrProtected.FuckAccess(Timer.class);
            NoPrivateOrProtected.FuckAccess(Object.class);
            NoPrivateOrProtected.FuckAccess(LaunchClassLoader.class);
            NoPrivateOrProtected.FuckAccess(Loader.class);
            if (client) {
                NoPrivateOrProtected.FuckAccess(SoundSystem.class);
                NoPrivateOrProtected.FuckAccess(GL11.class);
                NoPrivateOrProtected.FuckAccess(GL12.class);
                NoPrivateOrProtected.FuckAccess(GL13.class);
                NoPrivateOrProtected.FuckAccess(GL14.class);
                NoPrivateOrProtected.FuckAccess(GL15.class);
                NoPrivateOrProtected.FuckAccess(GL20.class);
                NoPrivateOrProtected.FuckAccess(GL21.class);
                NoPrivateOrProtected.FuckAccess(GL32.class);
                NoPrivateOrProtected.FuckAccess(GL42.class);
                NoPrivateOrProtected.FuckAccess(GL30.class);
                NoPrivateOrProtected.FuckAccess(GL31.class);
                NoPrivateOrProtected.FuckAccess(GL33.class);
                NoPrivateOrProtected.FuckAccess(GL40.class);
                NoPrivateOrProtected.FuckAccess(GL41.class);
                NoPrivateOrProtected.FuckAccess(GL43.class);
                NoPrivateOrProtected.FuckAccess(GL44.class);
                NoPrivateOrProtected.FuckAccess(GL45.class);
                NoPrivateOrProtected.FuckAccess(ContextCapabilities.class);
                NoPrivateOrProtected.FuckAccess(GLContext.class);
                NoPrivateOrProtected.FuckAccess(Class.forName("org.lwjgl.opengl.APIUtil"));
                NoPrivateOrProtected.FuckAccess(Class.forName("org.lwjgl.opengl.StateTracker"));
                NoPrivateOrProtected.FuckAccess(Class.forName("org.lwjgl.opengl.References"));
                NoPrivateOrProtected.FuckAccess(Class.forName("org.lwjgl.opengl.BaseReferences"));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Map<String, byte[]> RESOURCES = new HashMap<>();

    static {
        final boolean win = System.getProperty("os.name").startsWith("Windows");
        String tmp = Empty.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("!/kanade/kill/Empty.class", "").replace("file:", "");
        if (win) {
            tmp = tmp.substring(1);
        }
        try {
            tmp = URLDecoder.decode(tmp, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {
        }
        JAR = tmp;

        try (JarFile jar = new JarFile(JAR)) {
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                try (InputStream is = jar.getInputStream(entry)) {
                    String s = entry.getName().replace(".class", "").replace('/', '.');
                    byte[] data = ByteStreams.toByteArray(is);
                    RESOURCES.put(s, data);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        LOGGER.info("Launch method entered.");
        LOGGER.info("Working directory:{}", new File("./").getAbsolutePath());
        LOGGER.info("Our jar:{}", JAR);

        JarProcessor.processDirectory(new File("mods"));

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

    static {
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

        final URLClassLoader ucl = (URLClassLoader) Loader.class.getClassLoader();
        classLoader = new KanadeClassLoader(ucl.getURLs());

        blackboard = new HashMap<>();
        Thread.currentThread().setContextClassLoader(classLoader);


        Thread thread = new ClassLoaderCheckThread(Kanade);
        thread.start();
        thread = new KillerThread(Kanade);
        thread.start();
        thread = new SecurityManagerThread(Kanade);
        thread.start();

        net.minecraft.launchwrapper.Launch.classLoader = classLoader;
        net.minecraft.launchwrapper.Launch.blackboard = blackboard;

        INSTRUMENTATION = NativeMethods.ConstructInst();

        LOGGER.info("Fucking reflection filters.");
        try {
            Field filter = ReflectionUtil.getField(Reflection.class, "fieldFilterMap");
            ObjectUtil.putStatic(filter, null);
            filter = ReflectionUtil.getField(Reflection.class, "methodFilterMap");
            ObjectUtil.putStatic(filter, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Fucking CoreModManager");
        try (InputStream is = Empty.class.getResourceAsStream("/CoreModManager")) {
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
            Clazz = Unsafe.instance.defineClass("net.minecraftforge.fml.relauncher.CoreModManager", bytes, 0, bytes.length, Launch.class.getClassLoader(), Loader.class.getProtectionDomain());
            classLoader.cachedClasses.put("net.minecraftforge.fml.relauncher.CoreModManager", Clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        LOGGER.info("Checking account...");
//        file = new File("account");
//        if (!file.exists()) {
//            String name = JOptionPane.showInputDialog("Please enter username");
//            String uuid = JOptionPane.showInputDialog("Please enter uuid");
//            boolean res = NetworkTool.CheckUser("?username=" + name + "&uuid=" + uuid);
//            if (!res) {
//                JOptionPane.showMessageDialog(null, "Invalid account!", "ERROR", JOptionPane.ERROR_MESSAGE);
//                Runtime.getRuntime().exit(114514);
//            } else if (client) {
//                JOptionPane.showMessageDialog(null, "Verify passed!", null, JOptionPane.INFORMATION_MESSAGE);
//            }
//
//            LOGGER.info("Continue.");
//        } else {
//            byte[] buf;
//
//            try (FileInputStream fis = new FileInputStream(file)) {
//                buf = new byte[fis.available()];
//                fis.read(buf, 0, fis.available());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            String s = new String(buf, StandardCharsets.US_ASCII);
//
//            if (!NetworkTool.CheckUser(s)) {
//                LOGGER.error("Invalid account!");
//                JOptionPane.showMessageDialog(null, "Invalid account!", "ERROR", JOptionPane.ERROR_MESSAGE);
//                Runtime.getRuntime().exit(114514);
//            } else {
//                if (client) {
//                    JOptionPane.showMessageDialog(null, "Verify passed!", null, JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        }
    }
}
