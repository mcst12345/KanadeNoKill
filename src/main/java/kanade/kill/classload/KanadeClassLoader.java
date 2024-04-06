package kanade.kill.classload;

import LZMA.LzmaInputStream;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import kanade.kill.Launch;
import kanade.kill.asm.Transformer;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.NativeMethods;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.patcher.ClassPatch;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.repackage.com.nothome.delta.GDiffPatcher;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import scala.concurrent.util.Unsafe;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.*;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class KanadeClassLoader extends LaunchClassLoader {
    public static final Set<String> exclusions = new HashSet<>();
    private final Map<String, byte[]> resourceCache = new ConcurrentHashMap<>(1000);
    List<IClassTransformer> transformers = new ArrayList<>();
    private static int num;
    private static final Manifest EMPTY = new Manifest();

    public static final List<IClassTransformer> NecessaryTransformers = new ArrayList<>();
    public static final boolean debug = System.getProperty("Debug") != null;

    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private static boolean goodTransformer(String name) {
        return  name.equals("optifine.OptiFineClassTransformer") ||
                name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.SideTransformer") ||
                name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer") ||
                name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriberTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer") ||
                name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.SoundEngineFixTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.AccessTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.ItemBlockTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.ItemBlockSpecialTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.PotionEffectTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.TerminalTransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer") ||
                name.equals("net.minecraftforge.fml.common.asm.transformers.MarkerTransformer");
    }
    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public String untransformName(final String name) {
        if (DeobfuscatingTransformer != null) {
            return DeobfuscatingTransformer.unmapClassName(name);
        }

        return name;
    }

    public URLConnection findCodeSourceConnectionFor(final String name) {
        final URL resource = findResource(name);
        if (resource != null) {
            try {
                return resource.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public KanadeClassLoader(URL[] urls) {
        super(urls);
    }

    private static void save(byte[] clazz, String file) {
        if (!debug || clazz == null || file == null || file.isEmpty()) {
            return;
        }
        try {
            Files.write(new File("transformedClasses" + File.separator + file + ".class").toPath(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> DefineClass(String name, byte[] b, int off, int len,
                                ProtectionDomain protectionDomain) {
        return defineClass(name, b, off, len, protectionDomain);
    }

    private byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        if(!Launch.betterCompatible){
            return basicClass;
        }
        List<IClassTransformer> Transformers = (List<IClassTransformer>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformers_offset);
        if (Transformers.getClass() != ArrayList.class) {
            Transformers = new ArrayList<>(Transformers);
            Unsafe.instance.putObjectVolatile(this, EarlyFields.transformers_offset, Transformers);
        }
        for (final IClassTransformer transformer : transformers) {
            try {
                basicClass = transformer.transform(name, transformedName, basicClass);
            } catch (ClassCircularityError ignored) {
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception when running transformers:" + transformedName + ":", t);
            }
        }
        for (final IClassTransformer transformer : Transformers) {
            try {
                basicClass = transformer.transform(name, transformedName, basicClass);
            } catch (ClassCircularityError ignored) {
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception when running transformers:" + transformedName + ":", t);
            }
        }
        return basicClass;
    }

    @Override
    public void registerTransformer(String transformerClassName) {
        if(transformerClassName.equals("net.minecraftforge.fml.common.asm.transformers.PatchingTransformer")){
            return;
        }
        Launch.LOGGER.info("Register transformer:" + transformerClassName);
        try {
            IClassTransformer transformer = (IClassTransformer) loadClass(transformerClassName).newInstance();
            if (goodTransformer(transformerClassName)) {
                Launch.LOGGER.info("This is a necessary transformer.");
                NecessaryTransformers.add(transformer);
                if (transformer instanceof IClassNameTransformer && DeobfuscatingTransformer == null) {
                    DeobfuscatingTransformer = (IClassNameTransformer) transformer;
                }
            } else if(Launch.betterCompatible){
                transformers.add(transformer);
            }
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "A critical problem occurred registering the ASM transformer class %s", transformerClassName);
        }
    }

    private String transformName(final String name) {
        if (DeobfuscatingTransformer != null) {
            return DeobfuscatingTransformer.remapClassName(name);
        }
        return name;
    }

    static {
        exclusions.add("kanade.kill.thread.ClassLoaderCheckThread");
        exclusions.add("net.minecraftforge.fml.common.patcher.ClassPatchManager");
        exclusions.add("me.xdark.shell.JVMUtil");
        exclusions.add("me.xdark.shell.NativeLibrary");
        exclusions.add("me.xdark.shell.ShellcodeRunner");
        exclusions.add("one.helfy.Field");
        exclusions.add("one.helfy.JVM");
        exclusions.add("one.helfy.Type");
        exclusions.add("one.helfy.JVMException");
        exclusions.add("kanade.kill.util.memory.MemoryHelper");
        exclusions.add("kanade.kill.classload.KanadeClassLoader");
        exclusions.add("kanade.kill.asm.Transformer");
        exclusions.add("kanade.kill.util.NativeMethods");
        exclusions.add("kanade.kill.Launch");
        exclusions.add("kanade.kill.ServerMain");
        exclusions.add("kanade.kill.Core");
        exclusions.add("org.lwjgl.opengl.GLOffsets");
        exclusions.add("org.lwjgl.opengl.OpenGLHelper");
        exclusions.add("kanade.kill.util.NumberUtil");
        exclusions.add("kanade.kill.classload.FakeClassLoadr");
        exclusions.add("kanade.kill.util.superRender.ImagePanel");
        exclusions.add("kanade.kill.util.superRender.DeathWindow");
        exclusions.add("kanade.kill.util.InternalUtils");
    }

    //Launch.classLoader.getClass().getDeclaredField("transformerExceptions")
    //Why don's you use LaunchClassLoader.class ?
    @SuppressWarnings("unused")
    public Set<String> transformerExceptions = Collections.EMPTY_SET;

    public IClassNameTransformer DeobfuscatingTransformer;

    @Override
    public byte[] getClassBytes(String name) throws IOException {
        Set<String> negativeResourceCache = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.negativeResourceCache_offset);
        if (negativeResourceCache.contains(name)) {
            return null;
        } else if (resourceCache.containsKey(name)) {
            return resourceCache.get(name);
        }
        if (name.indexOf('.') == -1) {
            for (final String reservedName : RESERVED_NAMES) {
                if (name.toUpperCase(Locale.ENGLISH).startsWith(reservedName)) {
                    final byte[] data = getClassBytes("_" + name);
                    if (data != null) {
                        resourceCache.put(name, data);
                        return data;
                    }
                }
            }
        }

        InputStream classStream = null;
        try {
            final String resourcePath = name.replace('.', '/').concat(".class");
            final URL classResource = findResource(resourcePath);

            if (classResource == null) {
                negativeResourceCache.add(name);
                return null;
            }
            classStream = classResource.openStream();

            final byte[] data = readFully(classStream);
            resourceCache.put(name, data);
            return data;
        } finally {
            closeSilently(classStream);
        }
    }

    private byte[] getOrCreateBuffer() {
        ThreadLocal<byte[]> loadBuffer = (ThreadLocal<byte[]>) Unsafe.instance.getObjectVolatile(this, EarlyFields.loadBuffer_offset);
        byte[] buffer = loadBuffer.get();
        if (buffer == null) {
            loadBuffer.set(new byte[BUFFER_SIZE]);
            buffer = loadBuffer.get();
        }
        return buffer;
    }

    private byte[] readFully(InputStream stream) {
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
            Launch.LOGGER.warn("Problem loading class", t);
            return new byte[0];
        }
    }

    public List<IClassTransformer> getTransformers() {
        return transformers;
    }
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        HashMap<CodeSource, ProtectionDomain> pdcache = (HashMap<CodeSource, ProtectionDomain>) Unsafe.instance.getObjectVolatile(this, EarlyFields.pdcache_offset);
        if (cs == null)
            return null;
        ProtectionDomain pd;
        pd = pdcache.get(cs);
        if (pd == null) {
            PermissionCollection perms = getPermissions(cs);
            pd = new ProtectionDomain(cs, perms, this, null);
            pdcache.put(cs, pd);
        }
        return pd;
    }

    @SuppressWarnings("unused")
    public static Class<?> defineClass(Object unused, String name, byte[] b, int off, int len,
                                       ClassLoader loader,
                                       ProtectionDomain protectionDomain) {
        b = Transformer.instance.transform(name, ((KanadeClassLoader) Launch.classLoader).untransformName(name), b, null);
        if (debug) {
            save(b, name);
        }
        Class<?> clazz = Unsafe.instance.defineClass(name, b, off, len, Launch.classLoader, protectionDomain);
        NativeMethods.SetTag(clazz, 16);//good class tag :)
        return clazz;
    }

    @SuppressWarnings("unused")
    public static Class<?> defineAnonymousClass(Object unused, Class<?> hostClass, byte[] data, Object[] cpPatches) {
        data = Transformer.instance.transform("", "", data, null);
        if (debug) {
            save(data, "AnonyClass" + num++);
        }
        Class<?> clazz = Unsafe.instance.defineAnonymousClass(hostClass, data, cpPatches);
        NativeMethods.SetTag(clazz, 16);//good class tag :)
        return clazz;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("kanade.kill.")) {
            Launch.LOGGER.info("Kanade class wants to load:" + name);
        }
        ClassLoader parent = (ClassLoader) Unsafe.instance.getObjectVolatile(this, EarlyFields.parent_offset);
        if (exclusions.contains(name)) {
            return parent.loadClass(name);
        }
        if (FakeClassLoadr.INSTANCE.cache.containsKey(name)) {
            return FakeClassLoadr.INSTANCE.cache.get(name);
        }
        String transformedName = transformName(name);
        String untransformedName = untransformName(name);
        Set<String> classLoaderExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.classLoaderExceptions_offset);
        Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>) Unsafe.instance.getObjectVolatile(this, EarlyFields.cachedClasses_offset);
        Set<String> transformerExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformerExceptions_offset);

        Map<Package, Manifest> packageManifests = (Map<Package, Manifest>) Unsafe.instance.getObjectVolatile(this, EarlyFields.packageManifests_offset);

        for (final String exception : classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return parent.loadClass(name);
            }
        }

        if (cachedClasses.containsKey(name)) {
            return cachedClasses.get(name);
        }

        final String fileName = untransformedName.replace('.', '/').concat(".class");
        URLConnection urlConnection = findCodeSourceConnectionFor(fileName);


        for (final String exception : transformerExceptions) {
            if (name.startsWith(exception)) {
                try {
                    byte[] CLASS = getClassBytes(name);
                    if (CLASS == null) {
                        throw new ClassNotFoundException(name);
                    }
                    CLASS = Transformer.instance.transform(untransformedName, transformedName, CLASS, new byte[0]);
                    final Class<?> clazz = Unsafe.instance.defineClass(transformedName, CLASS, 0, CLASS.length, this, new ProtectionDomain(new CodeSource(urlConnection != null ? urlConnection.getURL() : null, new Certificate[0]), null));
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        }

        try {

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);

            CodeSigner[] signers = null;

            if (lastDot > -1 && !untransformedName.startsWith("net.minecraft.")) {
                if (urlConnection instanceof JarURLConnection) {
                    final JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                    final JarFile jarFile = jarURLConnection.getJarFile();

                    if (jarFile != null && jarFile.getManifest() != null) {
                        final Manifest manifest = jarFile.getManifest();
                        final JarEntry entry = jarFile.getJarEntry(fileName);

                        Package pkg = getPackage(packageName);
                        getClassBytes(untransformedName);
                        signers = entry.getCodeSigners();
                        if (pkg == null) {
                            pkg = definePackage(packageName, manifest, jarURLConnection.getJarFileURL());
                            packageManifests.put(pkg, manifest);
                        }
                    }
                } else {
                    Package pkg = getPackage(packageName);
                    if (pkg == null) {
                        pkg = definePackage(packageName, null, null, null, null, null, null, null);
                        packageManifests.put(pkg, EMPTY);
                    }
                }
            }

            byte[] bytes = getClassBytes(untransformedName);

            {
                //Launch.LOGGER.info(ClassPatchManager.class.getClassLoader().getClass().getName());
                bytes = patchClass(untransformedName,transformedName,bytes);
                save(bytes,name+"-patched");
            }

            if (bytes == null) {
                Launch.LOGGER.warn("Failed to get bytes of class:" + name);
                Launch.LOGGER.warn("Untransformed name:" + untransformedName);
            }

            for (IClassTransformer t : NecessaryTransformers) {
                try {
                    bytes = t.transform(untransformedName, transformedName, bytes);
                } catch (Throwable th) {
                    Launch.LOGGER.error("Catch exception:",th);
                }
            }

            byte[] unchanged = bytes != null ? Arrays.copyOf(bytes,bytes.length) : null;

            byte[] transformedClass = runTransformers(untransformedName, transformedName, bytes);
            transformedClass = Transformer.instance.transform(untransformedName, transformedName
                    , transformedClass,unchanged);

            if (debug) {
                save(transformedClass, name);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = Unsafe.instance.defineClass(transformedName, transformedClass, 0, transformedClass.length, this, getProtectionDomain(codeSource));
            NativeMethods.SetTag(clazz, 16);//Good class tag :)
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private static byte[] patchClass(String name, String transformedName, byte[] bytes){
        if (patches == null)
        {
            return bytes;
        }
        if (patchedClasses.containsKey(name))
        {
            return patchedClasses.get(name);
        }
        List<ClassPatch> list = patches.get(name);
        if (list.isEmpty())
        {
            return bytes;
        }
        boolean ignoredError = false;
        for (ClassPatch patch: list)
        {
            if (!patch.targetClassName.equals(transformedName) && !patch.sourceClassName.equals(name))
            {
                Launch.LOGGER.warn("Binary patch found {} for wrong class {}", patch.targetClassName, transformedName);
            }
            if (!patch.existsAtTarget && (bytes == null || bytes.length == 0))
            {
                bytes = new byte[0];
            }
            else if (!patch.existsAtTarget)
            {
                Launch.LOGGER.warn("Patcher expecting empty class data file for {}, but received non-empty", patch.targetClassName);
            }
            else if (bytes == null || bytes.length == 0)
            {
                Launch.LOGGER.fatal("Patcher expecting non-empty class data file for {}, but received empty.", patch.targetClassName);
                throw new RuntimeException(String.format("Patcher expecting non-empty class data file for %s, but received empty, your vanilla jar may be corrupt.", patch.targetClassName));
            }
            else
            {
                int inputChecksum = Hashing.adler32().hashBytes(bytes).asInt();
                if (patch.inputChecksum != inputChecksum)
                {
                    Launch.LOGGER.fatal("There is a binary discrepancy between the expected input class {} ({}) and the actual class. Checksum on disk is {}, in patch {}. Things are probably about to go very wrong. Did you put something into the jar file?", transformedName, name, Integer.toHexString(inputChecksum), Integer.toHexString(patch.inputChecksum));
                    if (!Boolean.parseBoolean(System.getProperty("fml.ignorePatchDiscrepancies","false")))
                    {
                        Launch.LOGGER.fatal("The game is going to exit, because this is a critical error, and it is very improbable that the modded game will work, please obtain clean jar files.");
                        System.exit(1);
                    }
                    else
                    {
                        Launch.LOGGER.fatal("FML is going to ignore this error, note that the patch will not be applied, and there is likely to be a malfunctioning behaviour, including not running at all");
                        ignoredError = true;
                        continue;
                    }
                }
            }
            synchronized (patcher)
            {
                try
                {
                    bytes = patcher.patch(bytes, patch.patch);
                }
                catch (IOException e)
                {
                    Launch.LOGGER.error("Encountered problem runtime patching class {}", name, e);
                }
            }
        }
        if (!ignoredError && Launch.Debug)
        {
            Launch.LOGGER.info("Successfully applied runtime patches for {} (new size {})", transformedName, bytes != null ? bytes.length : 0);
        }
        patchedClasses.put(name,bytes);
        return bytes;
    }

    static {
        patchedClasses = new HashMap<>();
        patcher = new GDiffPatcher();
        setup(Launch.client ? Side.CLIENT : Side.SERVER);
    }

    private static final GDiffPatcher patcher;
    private static ListMultimap<String, ClassPatch> patches;

    private static final Map<String,byte[]> patchedClasses;

    private static void setup(Side side)
    {
        Pattern binpatchMatcher = Pattern.compile(String.format("binpatch/%s/.*.binpatch", side.toString().toLowerCase(Locale.ENGLISH)));
        JarInputStream jis = null;
        try
        {
            try
            {
                InputStream binpatchesCompressed = Loader.class.getResourceAsStream("/binpatches.pack.lzma");
                if (binpatchesCompressed==null)
                {
                    if (!FMLLaunchHandler.isDeobfuscatedEnvironment())
                    {
                        FMLLog.log.fatal("The binary patch set is missing, things are not going to work!");
                    }
                    return;
                }
                try (LzmaInputStream binpatchesDecompressed = new LzmaInputStream(binpatchesCompressed))
                {
                    ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
                    try (JarOutputStream jos = new JarOutputStream(jarBytes))
                    {
                        Pack200.newUnpacker().unpack(binpatchesDecompressed, jos);
                        jis = new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray()));
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error occurred reading binary patches. Expect severe problems!", e);
            }

            patches = ArrayListMultimap.create();

            do
            {
                try
                {
                    JarEntry entry = jis.getNextJarEntry();
                    if (entry == null)
                    {
                        break;
                    }
                    if (binpatchMatcher.matcher(entry.getName()).matches())
                    {
                        ClassPatch cp = readPatch(entry, jis);
                        if (cp != null)
                        {
                            patches.put(cp.sourceClassName, cp);
                        }
                    }
                    else
                    {
                        jis.closeEntry();
                    }
                }
                catch (IOException ignored)
                {
                }
            } while (true);
        }
        finally
        {
            IOUtils.closeQuietly(jis);
        }
        Launch.LOGGER.info("Read {} binary patches", patches.size());
        patchedClasses.clear();
    }

    private static ClassPatch readPatch(JarEntry patchEntry, JarInputStream jis)
    {
        ByteArrayDataInput input;
        try
        {
            input = ByteStreams.newDataInput(ByteStreams.toByteArray(jis));
        }
        catch (IOException e)
        {
            FMLLog.log.warn(FMLLog.log.getMessageFactory().newMessage("Unable to read binpatch file {} - ignoring", patchEntry.getName()), e);
            return null;
        }
        String name = input.readUTF();
        String sourceClassName = input.readUTF();
        String targetClassName = input.readUTF();
        boolean exists = input.readBoolean();
        int inputChecksum = 0;
        if (exists)
        {
            inputChecksum = input.readInt();
        }
        int patchLength = input.readInt();
        byte[] patchBytes = new byte[patchLength];
        input.readFully(patchBytes);

        return new ClassPatch(name, sourceClassName, targetClassName, exists, inputChecksum, patchBytes);
    }
}
