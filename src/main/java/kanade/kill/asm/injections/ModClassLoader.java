package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ModClassLoader implements Opcodes {
    public static void OverwriteAddModAPITransformer(MethodNode mn){
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD,0));
        mn.instructions.add(new VarInsnNode(ALOAD,1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC,"kanade/kill/asm/hooks/ModClassLoader","addModAPITransformer","(Lnet/minecraftforge/fml/common/ModClassLoader;Lnet/minecraftforge/fml/common/discovery/ASMDataTable;)Lnet/minecraftforge/fml/common/asm/transformers/ModAPITransformer;"));
        mn.instructions.add(new InsnNode(ARETURN));
        Launch.LOGGER.info("Overwrite addModAPITransformer.");
    }
}
