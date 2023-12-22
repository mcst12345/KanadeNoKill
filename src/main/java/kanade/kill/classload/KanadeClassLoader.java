package kanade.kill.classload;

import kanade.kill.Launch;
import kanade.kill.asm.Transformer;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.TransformerList;
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
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings("unchecked")
public class KanadeClassLoader extends LaunchClassLoader {
    private static final Manifest EMPTY = new Manifest();

    public static final List<IClassTransformer> NecessaryTransformers = new ArrayList<>();
    public static final boolean debug = System.getProperty("Debug") != null;

    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String untransformName(final String name) {
        IClassNameTransformer renameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(this, EarlyFields.renameTransformer_offset);
        if (renameTransformer != null) {
            return renameTransformer.unmapClassName(name);
        }

        return name;
    }

    private URLConnection findCodeSourceConnectionFor(final String name) {
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
        if (!debug) {
            return;
        }
        try {
            Files.write(new File("transformedClasses" + File.separator + file + ".class").toPath(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean goodTransformer(String name) {
        return name.equals("net.minecraftforge.fml.common.asm.transformers.PatchingTransformer") || name.equals("optifine.OptiFineClassTransformer") || name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.SideTransformer") || name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer") || name.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriberTransformer") || name.equals("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer");
    }

    private byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        List<IClassTransformer> transformers = (List<IClassTransformer>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformers_offset);
        if (transformers.getClass() != TransformerList.class) {
            transformers = new TransformerList<>(transformers);
            Unsafe.instance.putObjectVolatile(this, EarlyFields.transformers_offset, transformers);
        }
        for (final IClassTransformer transformer : transformers) {
            try {
                basicClass = transformer.transform(name, transformedName, basicClass);
            } catch (Throwable t) {
                Launch.LOGGER.warn("Catch exception when running transformers:" + transformedName + ":", t);
                return basicClass;
            }
        }
        return basicClass;
    }

    @Override
    public void registerTransformer(String transformerClassName) {
        Launch.LOGGER.info("Register transformer:" + transformerClassName);
        List<IClassTransformer> transformers = (List<IClassTransformer>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformers_offset);
        IClassNameTransformer renameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(this, EarlyFields.renameTransformer_offset);
        try {
            IClassTransformer transformer = (IClassTransformer) loadClass(transformerClassName).newInstance();
            if (goodTransformer(transformerClassName)) {
                NecessaryTransformers.add(transformer);
            }
            transformers.add(transformer);
            if (transformer instanceof IClassNameTransformer && renameTransformer == null) {
                Unsafe.instance.putObjectVolatile(this, EarlyFields.renameTransformer_offset, transformer);
            }
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "A critical problem occurred registering the ASM transformer class %s", transformerClassName);
        }
    }

    private String transformName(final String name) {
        IClassNameTransformer renameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(this, EarlyFields.renameTransformer_offset);
        if (renameTransformer != null) {
            return renameTransformer.remapClassName(name);
        }

        return name;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        ClassLoader parent = (ClassLoader) Unsafe.instance.getObjectVolatile(this, EarlyFields.parent_offset);
        if (name.equals("kanade.kill.util.NativeMethods") || name.equals("kanade.kill.Launch") || name.equals("kanade.kill.ServerMain")) {
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
                        throw new ClassNotFoundException(name);
                    }
                    CLASS = Transformer.instance.transform(untransformedName, transformedName, CLASS);
                    final Class<?> clazz = Unsafe.instance.defineClass(transformedName, CLASS, 0, CLASS.length, this, new ProtectionDomain(new CodeSource(urlConnection != null ? urlConnection.getURL() : null, new Certificate[0]), null));
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        }

        try {
            if (cachedClasses.containsKey(transformedName)) {
                return cachedClasses.get(transformedName);
            }

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
                Launch.LOGGER.error("Failed to get bytes of class:" + name);
                Launch.LOGGER.error("Untransformed name:" + untransformedName);
            }

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, bytes);

            if (debug) {
                save(transformedClass, name);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz;
            Class<?> clazz1;
            try {
                clazz1 = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            } catch (Throwable t) {
                Launch.LOGGER.error("The fuck!", t);
                throw new ClassNotFoundException(name, t);
            }
            clazz = clazz1;
            if (clazz != null) {
                cachedClasses.put(transformedName, clazz);
            }
            return clazz;
        } catch (Throwable e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    @Override
    public byte[] getClassBytes(String name) throws IOException {
        Set<String> negativeResourceCache = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.negativeResourceCache_offset);
        Map<String, byte[]> resourceCache = (Map<String, byte[]>) Unsafe.instance.getObjectVolatile(this, EarlyFields.resourceCache_offset);
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
}
