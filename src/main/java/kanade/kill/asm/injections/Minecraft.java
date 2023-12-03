package kanade.kill.asm.injections;

import kanade.kill.Core;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Minecraft implements Opcodes {
    public static void AddField(ClassNode cn) {
        Core.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "PLAYER", "Lnet/minecraft/client/entity/EntityPlayerSP;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "profiler", "Lnet/minecraft/profiler/Profiler;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "entityRenderer", "Lnet/minecraft/client/renderer/EntityRenderer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "renderManager", "Lnet/minecraft/client/renderer/entity/RenderManager;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mouseHelper", "Lnet/minecraft/util/MouseHelper;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "WORLD", "Lnet/minecraft/client/multiplayer/WorldClient;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "IngameGUI", "Lnet/minecraft/client/gui/GuiIngame;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "CurrentScreen", "Lnet/minecraft/client/gui/GuiScreen;", null, null));
    }
    public static void AddMethod(ClassNode cn) {
        Core.LOGGER.info("Adding method.");
        MethodNode inGameFocus = new MethodNode(ACC_PUBLIC, "SetIngameFocus", "()V", null, null);
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        inGameFocus.instructions.add(label0);
        inGameFocus.instructions.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/Display", "isActive", "()Z", false));
        inGameFocus.instructions.add(new JumpInsnNode(IFEQ, label1));
        inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71415_G", "Z"));
        inGameFocus.instructions.add(new JumpInsnNode(IFNE, label1));
        inGameFocus.instructions.add(new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "field_142025_a", "Z"));
        inGameFocus.instructions.add(new JumpInsnNode(IFNE, label2));
        inGameFocus.instructions.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/settings/KeyBinding", "func_186704_a", "()V", false));
        inGameFocus.instructions.add(label2);
        inGameFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameFocus.instructions.add(new InsnNode(ICONST_1));
        inGameFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "field_71415_G", "Z"));
        inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71417_B", "Lnet/minecraft/util/MouseHelper;"));
        inGameFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/MouseHelper", "func_74372_a", "()V", false));
        inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameFocus.instructions.add(new InsnNode(ACONST_NULL));
        inGameFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", "func_147108_a", "(Lnet/minecraft/client/gui/GuiScreen;)V", false));
        inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameFocus.instructions.add(new IntInsnNode(SIPUSH, 10000));
        inGameFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "field_71429_W", "I"));
        inGameFocus.instructions.add(label1);
        inGameFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        inGameFocus.instructions.add(new InsnNode(RETURN));
        inGameFocus.instructions.add(label3);
        inGameFocus.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/Minecraft;", null, label0, label3, 0));
        cn.methods.add(inGameFocus);
        MethodNode inGameNotFocus = new MethodNode(ACC_PUBLIC, "SetIngameNotInFocus", "()V", null, null);
        LabelNode l0 = new LabelNode();
        LabelNode l1 = new LabelNode();
        LabelNode l2 = new LabelNode();
        LabelNode l3 = new LabelNode();
        LabelNode l4 = new LabelNode();
        inGameNotFocus.instructions.add(l0);
        inGameNotFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameNotFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71415_G", "Z"));
        inGameNotFocus.instructions.add(new JumpInsnNode(IFEQ, l1));
        inGameNotFocus.instructions.add(l4);
        inGameNotFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameNotFocus.instructions.add(new InsnNode(ICONST_0));
        inGameNotFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "field_71415_G", "Z"));
        inGameNotFocus.instructions.add(l3);
        inGameNotFocus.instructions.add(new VarInsnNode(ALOAD, 0));
        inGameNotFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71417_B", "Lnet/minecraft/util/MouseHelper;"));
        inGameNotFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/MouseHelper", "func_74373_b", "()V", false));
        inGameNotFocus.instructions.add(l1);
        inGameNotFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        inGameNotFocus.instructions.add(new InsnNode(RETURN));
        inGameNotFocus.instructions.add(l2);
        inGameNotFocus.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/Minecraft;", null, l0, l2, 0));
        cn.methods.add(inGameNotFocus);
    }

    public static void InjectDisplayGuiScreen(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        LabelNode label_1 = new LabelNode();

        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71462_r", "Lnet/minecraft/client/gui/GuiScreen;"));
        list.add(new TypeInsnNode(INSTANCEOF, "kanade/kill/util/GuiDeath"));
        list.add(new JumpInsnNode(IFEQ, label_1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "field_71462_r", "Lnet/minecraft/client/gui/GuiScreen;"));
        list.add(new TypeInsnNode(CHECKCAST, "kanade/kill/util/GuiDeath"));
        list.add(new FieldInsnNode(GETFIELD, "kanade/kill/util/GuiDeath", "close", "Z"));
        list.add(new JumpInsnNode(IFNE, label_1));
        list.add(new InsnNode(RETURN));
        list.add(label_1);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(ASMUtil.inList());
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "BadGui", "(Lnet/minecraft/client/gui/Gui;)Z", false));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new InsnNode(RETURN));
        list.add(label);


        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Core.LOGGER.info("Inject into displayGuiScreen(GuiScreen).");
    }

    public static void InjectInit(MethodNode mn) {
        for (int i = mn.instructions.size() - 1; i >= 0; i--) {
            AbstractInsnNode ain = mn.instructions.get(i);
            if (ain instanceof InsnNode) {
                InsnNode in = (InsnNode) ain;
                if (in.getOpcode() == RETURN) {
                    mn.instructions.insert(mn.instructions.get(i - 1), new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "save", "()V", false));
                }
            }
        }
        Core.LOGGER.info("Inject into init().");
    }
}
