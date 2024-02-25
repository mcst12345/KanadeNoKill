package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class RenderLivingBase implements Opcodes {
    public static void InjectDoRenderHead(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new VarInsnNode(DLOAD, 2));
        list.add(new VarInsnNode(DLOAD, 4));
        list.add(new VarInsnNode(DLOAD, 6));
        list.add(new VarInsnNode(FLOAD, 8));
        list.add(new VarInsnNode(FLOAD, 9));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/RenderLivingBase", "doRender", "(Lnet/minecraft/client/renderer/entity/RenderLivingBase;Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", false));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into doRender.");
    }
}
