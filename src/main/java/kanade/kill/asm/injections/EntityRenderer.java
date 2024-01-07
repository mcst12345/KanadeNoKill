package kanade.kill.asm.injections;

import kanade.kill.Launch;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class EntityRenderer implements Opcodes {
    public static void InjectUpdateCameraAndRender(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(ASMUtil.isTimeStop());
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/EntityRenderer", "field_78531_r", "Lnet/minecraft/client/Minecraft;"));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_193996_ah", "F"));
        list.add(new VarInsnNode(FSTORE, 1));
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into updateCameraAndRender(FJ)V.");
    }
}
