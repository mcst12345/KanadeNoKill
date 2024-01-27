package kanade.kill;

import com.mx_wj.AvaritiaGod.event.AvaritiaGodEvent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class Empty {
    public static void main(String[] args) {
        try (InputStream is = AvaritiaGodEvent.class.getResourceAsStream("/com/mx_wj/AvaritiaGod/event/AvaritiaGodEvent.class")) {
            assert is != null;
            ClassReader cr = new ClassReader(is);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            if (cn.visibleAnnotations == null) {
                cn.visibleAnnotations = new ArrayList<>();
                cn.visibleAnnotations.add(new AnnotationNode("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;"));
            } else {
                cn.visibleAnnotations.add(new AnnotationNode("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;"));
            }
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            byte[] bytes = cw.toByteArray();
            Files.write(new File("/Changed.class").toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
