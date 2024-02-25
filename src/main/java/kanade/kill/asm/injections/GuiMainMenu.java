package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class GuiMainMenu implements Opcodes {
    public static void OverwriteInitGui(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/GuiMainMenu", "initGui", "(Lnet/minecraft/client/gui/GuiMainMenu;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite initGui().");
    }

    public static void OverwriteDrawScreen(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ILOAD, 1));
        mn.instructions.add(new VarInsnNode(ILOAD, 2));
        mn.instructions.add(new VarInsnNode(FLOAD, 3));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/GuiMainMenu", "drawScreen", "(Lnet/minecraft/client/gui/GuiMainMenu;IIF)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite drawScreen().");
    }
}
