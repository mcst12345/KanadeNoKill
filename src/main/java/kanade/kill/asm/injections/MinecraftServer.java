package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MinecraftServer implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Worlds", "[Lnet/minecraft/world/WorldServer;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "PlayerList", "Lnet/minecraft/server/management/PlayerList;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "Profiler", "Lnet/minecraft/profiler/Profiler;", null, null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new InsnNode(ICONST_1));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/world/WorldServer"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/server/MinecraftServer", "field_71305_c", "[Lnet/minecraft/world/WorldServer;"));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into <init>.");
    }

    public static void OverwriteTick(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MinecraftServer", "tick", "(Lnet/minecraft/server/MinecraftServer;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite tick().");
    }

    public static void OverwriteRun(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MinecraftServer", "run", "(Lnet/minecraft/server/MinecraftServer;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite run().");
    }

    public static void OverwriteSetPlayerList(MethodNode mn) {
        mn.instructions.clear();
        mn.localVariables.clear();
        mn.tryCatchBlocks.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/MinecraftServer", "setPlayerList", "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/management/PlayerList;)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite run().");
    }
}
