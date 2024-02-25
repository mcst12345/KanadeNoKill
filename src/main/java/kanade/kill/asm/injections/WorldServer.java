package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class WorldServer implements Opcodes {
    public static void OverwriteLoadEntities(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        LabelNode label5 = new LabelNode();
        LabelNode label6 = new LabelNode();
        LabelNode label7 = new LabelNode();
        LabelNode label8 = new LabelNode();
        LabelNode label9 = new LabelNode();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new JumpInsnNode(IFNONNULL, label0));
        list.add(new InsnNode(RETURN));
        list.add(label0);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/util/EntityUtil", "isDead", "(Ljava/lang/Object;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label2);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 3));
        list.add(label3);
        list.add(new FrameNode(F_APPEND, 2, new Object[]{"java/util/List", "java/util/Iterator"}, 0, null));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label4));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 4));
        list.add(label5);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/world/WorldServer", "func_184165_i", "(Lnet/minecraft/entity/Entity;)Z", false));
        list.add(new JumpInsnNode(IFEQ, label6));
        list.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
        list.add(new TypeInsnNode(NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
        list.add(new JumpInsnNode(IFNE, label6));
        list.add(label7);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/WorldServer", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label8);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/WorldServer", "func_72923_a", "(Lnet/minecraft/entity/Entity;)V", false));
        list.add(label6);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new JumpInsnNode(GOTO, label3));
        list.add(label4);
        list.add(new FrameNode(F_CHOP, 1, null, 0, null));
        list.add(new InsnNode(RETURN));
        list.add(label9);
        mn.instructions = list;

        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("entity", "Lnet/minecraft/entity/Entity;", null, label5, label6, 4));
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/WorldServer;", null, label0, label9, 0));
        mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label9, 1));
        mn.localVariables.add(new LocalVariableNode("lists", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label9, 2));

        Launch.LOGGER.info("Overwrite loadEntities.");
    }

    public static void OverwriteSetEntityState(MethodNode mn) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables.clear();
        mn.instructions.add(new VarInsnNode(ALOAD, 0));
        mn.instructions.add(new VarInsnNode(ALOAD, 1));
        mn.instructions.add(new VarInsnNode(ILOAD, 2));
        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/WorldServer", "setEntityState", "(Lnet/minecraft/world/WorldServer;Lnet/minecraft/entity/Entity;B)V", false));
        mn.instructions.add(new InsnNode(RETURN));
        Launch.LOGGER.info("Overwrite setEntityState");
    }

}
