package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ServerCommandManager implements Opcodes {
    public static void InjectConstructor(MethodNode mn) {
        AbstractInsnNode index = null;
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof TypeInsnNode) {
                index = ain.getPrevious();
            }
        }
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "kanade/kill/command/KanadeKillCommand"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "kanade/kill/command/KanadeKillCommand", "<init>", "()V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/command/ServerCommandManager", "func_71560_a", "(Lnet/minecraft/command/ICommand;)Lnet/minecraft/command/ICommand;", false));
        list.add(new InsnNode(POP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "kanade/kill/command/CommandInfo"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "kanade/kill/command/CommandInfo", "<init>", "()V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/command/ServerCommandManager", "func_71560_a", "(Lnet/minecraft/command/ICommand;)Lnet/minecraft/command/ICommand;", false));
        list.add(new InsnNode(POP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "kanade/kill/command/DebugCommand"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "kanade/kill/command/DebugCommand", "<init>", "()V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/command/ServerCommandManager", "func_71560_a", "(Lnet/minecraft/command/ICommand;)Lnet/minecraft/command/ICommand;", false));
        list.add(new InsnNode(POP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "kanade/kill/command/KanadeReflection"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "kanade/kill/command/KanadeReflection", "<init>", "()V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/command/ServerCommandManager", "func_71560_a", "(Lnet/minecraft/command/ICommand;)Lnet/minecraft/command/ICommand;", false));
        list.add(new InsnNode(POP));

        if (index == null) {
            throw new IllegalStateException("The fuck?");
        }
        mn.instructions.insertBefore(index, list);
        Launch.LOGGER.info("Inject into <init>");
    }
}
