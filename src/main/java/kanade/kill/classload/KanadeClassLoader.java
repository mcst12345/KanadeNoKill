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

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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

    @Override

    public Class<?> findClass(final String name) throws ClassNotFoundException {
        Set<String> invalidClasses = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.invalidClasses_offset);
        Set<String> classLoaderExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.classLoaderExceptions_offset);
        ClassLoader parent = (ClassLoader) Unsafe.instance.getObjectVolatile(this, EarlyFields.parent_offset);
        Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>) Unsafe.instance.getObjectVolatile(this, EarlyFields.cachedClasses_offset);
        Set<String> transformerExceptions = (Set<String>) Unsafe.instance.getObjectVolatile(this, EarlyFields.transformerExceptions_offset);
        IClassNameTransformer renameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(this, EarlyFields.renameTransformer_offset);
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
                    CLASS = Transformer.instance.transform(name, renameTransformer != null ? renameTransformer.remapClassName(name) : name, CLASS);
                    final Class<?> clazz = super.defineClass(name, CLASS, 0, CLASS.length, new CodeSource(null, (Certificate[]) null));
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
            final String transformedName = renameTransformer != null ? renameTransformer.remapClassName(name) : name;
            if (cachedClasses.containsKey(transformedName)) {
                return cachedClasses.get(transformedName);
            }

            final String untransformedName = renameTransformer != null ? renameTransformer.unmapClassName(name) : name;

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

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, getClassBytes(untransformedName));

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            invalidClasses.add(name);
            Core.LOGGER.trace("Exception encountered attempting classloading of " + name, e);
            Core.LOGGER.error("Exception encountered attempting classloading of %s", e);
            throw new ClassNotFoundException(name, e);
        }
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
    }

    private boolean isSealed(final String path, final Manifest manifest) {
        Attributes attributes = manifest.getAttributes(path);
        String sealed = null;
        if (attributes != null) {
            sealed = attributes.getValue(Attributes.Name.SEALED);
        }

        if (sealed == null) {
            attributes = manifest.getMainAttributes();
            if (attributes != null) {
                sealed = attributes.getValue(Attributes.Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }
}
