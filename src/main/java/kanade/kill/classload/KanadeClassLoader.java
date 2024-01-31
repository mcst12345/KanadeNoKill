package kanade.kill.classload;

import kanade.kill.Launch;
import kanade.kill.asm.Transformer;
import kanade.kill.reflection.EarlyFields;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;
import scala.concurrent.util.Unsafe;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings("unchecked")
public class KanadeClassLoader extends LaunchClassLoader {
    private static final Set<String> exclusions = new HashSet<>();
    private final Map<String, byte[]> resourceCache = new ConcurrentHashMap<>(1000);
    private final Set<String> invalid = new HashSet<>();
    List<IClassTransformer> transformers = new ArrayList<>();
    IClassNameTransformer DeobfuscatingTransformer;
    private static final Manifest EMPTY = new Manifest();

    public static final List<IClassTransformer> NecessaryTransformers = new ArrayList<>();
    public static final boolean debug = System.getProperty("Debug") != null;

    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private static boolean goodTransformer(String name) {
        return name.equals("net.minecraftforge.fml.common.asm.transformers.PatchingTransformer") ||
                name.equals("optifine.OptiFineClassTransformer") ||
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
                name.equals("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer");
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
        Launch.LOGGER.info("Register transformer:" + transformerClassName);
        try {
            IClassTransformer transformer = (IClassTransformer) loadClass(transformerClassName).newInstance();
            if (goodTransformer(transformerClassName)) {
                NecessaryTransformers.add(transformer);
            }
            transformers.add(transformer);
            if (transformer instanceof IClassNameTransformer && DeobfuscatingTransformer == null) {
                DeobfuscatingTransformer = (IClassNameTransformer) transformer;
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
        exclusions.add("kanade.kill.asm.Transformer");
        exclusions.add("kanade.kill.util.NativeMethods");
        exclusions.add("kanade.kill.Launch");
        exclusions.add("kanade.kill.ServerMain");
        exclusions.add("kanade.kill.Core");
    }

    //Launch.classLoader.getClass().getDeclaredField("transformerExceptions")
    //Why don's you use LaunchClassLoader.class ?
    @SuppressWarnings("unused")
    public Set<String> transformerExceptions = Collections.EMPTY_SET;

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (invalid.contains(name)) {
            throw new ClassNotFoundException(name);
        }
        ClassLoader parent = (ClassLoader) Unsafe.instance.getObjectVolatile(this, EarlyFields.parent_offset);
        if (exclusions.contains(name)) {
            return parent.loadClass(name);
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
                        invalid.add(name);
                        throw new ClassNotFoundException(name);
                    }
                    CLASS = Transformer.instance.transform(untransformedName, transformedName, CLASS);
                    final Class<?> clazz = Unsafe.instance.defineClass(transformedName, CLASS, 0, CLASS.length, this, new ProtectionDomain(new CodeSource(urlConnection != null ? urlConnection.getURL() : null, new Certificate[0]), null));
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (IOException e) {
                    invalid.add(name);
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

            if (bytes == null) {
                Launch.LOGGER.warn("Failed to get bytes of class:" + name);
                Launch.LOGGER.warn("Untransformed name:" + untransformedName);
                invalid.add(name);
            }

            byte[] transformedClass = runTransformers(untransformedName, transformedName, bytes);
            transformedClass = Transformer.instance.transform(untransformedName, transformedName
                    , transformedClass);

            if (debug) {
                save(transformedClass, name);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = Unsafe.instance.defineClass(transformedName, transformedClass, 0, transformedClass.length, this, getProtectionDomain(codeSource));
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            invalid.add(name);
            throw new ClassNotFoundException(name, e);
        }
    }

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
}
