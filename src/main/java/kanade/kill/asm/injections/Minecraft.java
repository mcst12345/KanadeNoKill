package kanade.kill.asm.injections;

import kanade.kill.Launch;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Minecraft implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "PLAYER", "Lnet/minecraft/client/entity/EntityPlayerSP;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Profiler", "Lnet/minecraft/profiler/Profiler;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "EntityRenderer", "Lnet/minecraft/client/renderer/EntityRenderer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "renderManager", "Lnet/minecraft/client/renderer/entity/RenderManager;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mouseHelper", "Lnet/minecraft/util/MouseHelper;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "WORLD", "Lnet/minecraft/client/multiplayer/WorldClient;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "IngameGUI", "Lnet/minecraft/client/gui/GuiIngame;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "CurrentScreen", "Lnet/minecraft/client/gui/GuiScreen;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "itemRenderer", "Lnet/minecraft/client/renderer/RenderItem;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "RenderGlobal", "Lnet/minecraft/client/renderer/RenderGlobal;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "RenderEngine", "Lnet/minecraft/client/renderer/texture/TextureManager;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "BlockRenderDispatcher", "Lnet/minecraft/client/renderer/BlockRendererDispatcher;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "FontRenderer", "Lnet/minecraft/client/gui/FontRenderer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "ItemRenderer", "Lnet/minecraft/client/renderer/ItemRenderer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "PlayerController", "Lnet/minecraft/client/multiplayer/PlayerControllerMP;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "StandardGalacticFontRenderer", "Lnet/minecraft/client/gui/FontRenderer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "GameSettings", "Lnet/minecraft/client/settings/GameSettings;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, "dead", "Z", null, null));
    }
    public static void AddMethod(ClassNode cn) {
        Launch.LOGGER.info("Adding method.");
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
        inGameFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "mouseHelper", "Lnet/minecraft/util/MouseHelper;"));
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
        inGameNotFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "mouseHelper", "Lnet/minecraft/util/MouseHelper;"));
        inGameNotFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/MouseHelper", "func_74373_b", "()V", false));
        inGameNotFocus.instructions.add(l1);
        inGameNotFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        inGameNotFocus.instructions.add(new InsnNode(RETURN));
        inGameNotFocus.instructions.add(l2);
        inGameNotFocus.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/Minecraft;", null, l0, l2, 0));
        cn.methods.add(inGameNotFocus);
        MethodNode func_152343_a = new MethodNode(ACC_PUBLIC, "func_152343_a", "(Ljava/util/concurrent/Callable;)Lcom/google/common/util/concurrent/ListenableFuture;", "<V:Ljava/lang/Object;>(Ljava/util/concurrent/Callable<TV;>;)Lcom/google/common/util/concurrent/ListenableFuture<TV;>;", null);
        func_152343_a.instructions.add(new InsnNode(ACONST_NULL));
        func_152343_a.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/google/common/util/concurrent/Futures", "immediateFuture", "(Ljava/lang/Object;)Lcom/google/common/util/concurrent/ListenableFuture;", false));
        func_152343_a.instructions.add(new InsnNode(ARETURN));
        cn.methods.add(func_152343_a);
    }

    public static void InjectDisplayGuiScreen(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
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
        Launch.LOGGER.info("Inject into displayGuiScreen(GuiScreen).");
    }

    public static void InjectRun(MethodNode mn) {
        AbstractInsnNode index = null;
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof FieldInsnNode) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (fin.name.equals("field_71425_J") && fin.getOpcode() == GETFIELD) {
                    index = fin.getNext();
                    break;
                }
            }
        }
        if (index == null) {
            throw new IllegalStateException("The fuck?");
        }
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/thread/DisplayGui", "run", "()V", false));
        mn.instructions.insert(index, list);
        Launch.LOGGER.info("Inject into run().");
    }

    public static void InjectRunTickKeyboard(MethodNode mn) {
        AbstractInsnNode index = null;
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof InsnNode) {
                if (ain.getOpcode() == RETURN) {
                    index = ain;
                    break;
                }
            }
        }
        if (index == null) {
            throw new IllegalStateException("The fuck?");
        }
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Minecraft", "runTickKeyboard", "(Lnet/minecraft/client/Minecraft;)V", false));
        mn.instructions.insertBefore(index, list);
        Launch.LOGGER.info("Inject into runTickKeyboard().");
    }

    public static void OverwriteRunGameLoop(MethodNode mn) {
        mn.tryCatchBlocks.clear();
        mn.instructions.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Minecraft", "RunGameLoop", "(Lnet/minecraft/client/Minecraft;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        mn.localVariables.clear();
        mn.maxLocals = 1;
        mn.maxStack = 1;
        Launch.LOGGER.info("Overwrite runGameLoop()V.");
    }

    public static void OverwriteClickMouse(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Minecraft", "clickMouse", "(Lnet/minecraft/client/Minecraft;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        mn.maxStack = 1;
        mn.maxLocals = 1;
        Launch.LOGGER.info("Overwrite ClickMouse().");
    }

    public static void OverwriteRightClickMouse(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Minecraft", "rightClickMouse", "(Lnet/minecraft/client/Minecraft;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        mn.maxStack = 1;
        mn.maxLocals = 1;
        Launch.LOGGER.info("Overwrite RightClickMouse().");
    }

    public static void OverwriteGetSystemTime(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/Minecraft", "getSystemTime", "()J", false));
        mn.instructions.add(new InsnNode(LRETURN));
        Launch.LOGGER.info("Overwrite getSystemTime().");
    }
}
