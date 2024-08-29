package kanade.kill.util;

import com.google.common.io.ByteStreams;
import kanade.kill.Launch;
import kanade.kill.asm.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarProcessor {

    public static void processDirectory(File f) {
        assert f.isDirectory();
        Launch.LOGGER.info("Process directory:{}", f.getAbsolutePath());
        if (f.listFiles() != null) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                if (file.isDirectory() && !file.getAbsolutePath().equals(f.getAbsolutePath())) {
                    processDirectory(f);
                } else {
                    if (file.getName().endsWith(".jar")) {
                        processJar(file);
                    }
                }
            }
        }
    }

    public static void processJar(File target) {
        if (Launch.JAR.endsWith(target.getName())) {
            Launch.LOGGER.warn("Ignore:{}", target.getAbsolutePath());
            return;
        }
        boolean changed = false;
        Launch.LOGGER.info("Target:{}", target.getName());
        try {
            JarFile jar = new JarFile(target);
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                try (InputStream is = jar.getInputStream(entry)) {
                    Launch.LOGGER.info("Process entry:{}", entry.getName());
                    if (entry.getName().endsWith(".class")) {
                        byte[] bytes = ByteStreams.toByteArray(is);
                        try {
                            ClassReader cr = new ClassReader(bytes);
                            ClassNode cn = new ClassNode();
                            cr.accept(cn, 0);
                            boolean modified = Transformer.RedirectAndExamine(cn, false, null);
                            if (modified) {
                                ClassWriter cw = new ClassWriter(0);
                                cn.accept(cw);
                                jos.putNextEntry(new JarEntry(entry.getName()));
                                jos.write(cw.toByteArray());
                            } else {
                                jos.putNextEntry(new JarEntry(entry.getName()));
                                jos.write(bytes);
                            }
                        } catch (Throwable t) {
                            jos.putNextEntry(new JarEntry(entry.getName()));
                            jos.write(bytes);
                        }
                    } else {
                        jos.putNextEntry(new JarEntry(entry.getName()));
                        jos.write(ByteStreams.toByteArray(is));
                    }
                }
            }
            jos.closeEntry();
            jos.close();

            OverwriteFile(new File(jar.getName() + ".fucked"), new File(jar.getName()), true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void OverwriteFile(File source, File target, boolean backup) {
        source.setReadable(true);
        source.setWritable(true);
        target.setReadable(true);
        target.setWritable(true);
        try {
            if (backup) {
                try (FileOutputStream BACKUP = new FileOutputStream(target.getPath() + ".backup")) {
                    BACKUP.write(Files.readAllBytes(target.toPath()));
                    BACKUP.flush();
                    FileDescriptor fd = BACKUP.getFD();
                    fd.sync();
                }
            }
            try (FileOutputStream TARGET = new FileOutputStream(target)) {
                TARGET.write(Files.readAllBytes(source.toPath()));
                TARGET.flush();
                FileDescriptor fd = TARGET.getFD();
                fd.sync();
            }
            if (!source.delete()) {
                System.out.println("Holy Shit? " + source.getName() + " cannot be deleted! Well,ignoring it.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("The Fuck?");
            Runtime.getRuntime().exit(0);
        }
    }

}
