package kanade.kill.classload;

import kanade.kill.Core;
import kanade.kill.asm.Transformer;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.TransformerList;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import scala.concurrent.util.Unsafe;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static kanade.kill.asm.Transformer.Kanade;

@SuppressWarnings("unchecked")
public class KanadeClassLoader extends LaunchClassLoader {
    public static final KanadeClassLoader INSTANCE = new KanadeClassLoader(Launch.classLoader);
    private static final Manifest EMPTY = new Manifest();
    private final LaunchClassLoader old;

    private KanadeClassLoader(LaunchClassLoader old) {
        super(old.getURLs());
        this.old = old;
        Unsafe.instance.putObjectVolatile(this, EarlyFields.classLoaderExceptions_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.classLoaderExceptions_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.renameTransformer_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.renameTransformer_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.transformers_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.transformers_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.packageManifests_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.packageManifests_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.cachedClasses_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.cachedClasses_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.parent_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.parent_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.sources_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.sources_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.resourceCache_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.resourceCache_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.negativeResourceCache_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.negativeResourceCache_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.transformerExceptions_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.transformerExceptions_offset));
        Unsafe.instance.putObjectVolatile(this, EarlyFields.loadBuffer_offset, Unsafe.instance.getObjectVolatile(old, EarlyFields.loadBuffer_offset));
    }

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
        //if(name.equals("net.minecraft.client.renderer.block.model.ModelResourceLocation")){
        //    return "cgd";
        //}
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

    private byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        try {
            List<IClassTransformer> transformers = (List<IClassTransformer>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformers_offset);
            if (transformers.getClass() != TransformerList.class) {
                transformers = new TransformerList<>(transformers);
                Unsafe.instance.putObjectVolatile(this, EarlyFields.transformers_offset, transformers);
                Unsafe.instance.putObjectVolatile(old, EarlyFields.transformers_offset, transformers);
            }
            for (final IClassTransformer transformer : transformers) {
                basicClass = transformer.transform(name, transformedName, basicClass);
            }
            return basicClass;
        } catch (Throwable t) {
            Core.LOGGER.warn("Catch exception when running transformers:", t);
            return basicClass;
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
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        if (name.equals("sun.instrument.InstrumentationImpl")) {
            return LaunchClassLoader.class.getClassLoader().loadClass(name);
        }
        if (Kanade.contains(name)) {
            return Class.forName(name, false, old);
        }
        Set<String> invalidClasses = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.invalidClasses_offset);
        Set<String> classLoaderExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.classLoaderExceptions_offset);
        ClassLoader parent = (ClassLoader) Unsafe.instance.getObjectVolatile(this, EarlyFields.parent_offset);
        Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>) Unsafe.instance.getObjectVolatile(this, EarlyFields.cachedClasses_offset);
        Set<String> transformerExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformerExceptions_offset);

        Map<Package, Manifest> packageManifests = (Map<Package, Manifest>) Unsafe.instance.getObjectVolatile(this, EarlyFields.packageManifests_offset);
        if (invalidClasses.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        for (final String exception : classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return parent.loadClass(name);
            }
        }

        if (cachedClasses.containsKey(name)) {
            return cachedClasses.get(name);
        }

        for (final String exception : transformerExceptions) {
            if (name.startsWith(exception)) {
                try {
                    byte[] CLASS = getClassBytes(name);
                    if (CLASS == null) {
                        throw new ClassNotFoundException(name);
                    }
                    CLASS = Transformer.instance.transform(name, untransformName(name), CLASS);
                    final Class<?> clazz = Unsafe.instance.defineClass(name, CLASS, 0, CLASS.length, this, new ProtectionDomain(null, null));
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    invalidClasses.add(name);
                    throw e;
                } catch (IOException e) {
                    invalidClasses.add(name);
                    throw new ClassNotFoundException(name);
                }
            }
        }

        try {
            final String transformedName = transformName(name);

            if (cachedClasses.containsKey(transformedName)) {
                return cachedClasses.get(transformedName);
            }

            final String untransformedName = untransformName(name);

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            final String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = findCodeSourceConnectionFor(fileName);

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
                Core.LOGGER.warn("Class " + untransformedName + " is not found.");
            }

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, bytes);

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            return old.findClass(name);
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
            Core.LOGGER.warn("Problem loading class", t);
            return new byte[0];
        }
    }
}
