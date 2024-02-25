package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class WorldClient implements Opcodes {
    public static void AddField(ClassNode cn) {
        cn.fields.add(new FieldNode(ACC_PUBLIC, "EntityList", "Ljava/util/Set;", "Ljava/util/Set<Lnet/minecraft/entity/Entity;>;", null));
        Launch.LOGGER.info("Adding field.");
    }
    public static void OverwriteRemoveEntityFromWorld(MethodNode mn) {
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

        list.add(label0);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_175729_l", "Lnet/minecraft/util/IntHashMap;"));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "func_76041_a", "(I)Ljava/lang/Object;", false));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/item/KillItem", "inList", "(Ljava/lang/Object;)Z", false));
        list.add(new JumpInsnNode(IFEQ, label2));
        list.add(label3);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InsnNode(ARETURN));
        list.add(label2);
        list.add(new FrameNode(F_APPEND, 1, new Object[]{"net/minecraft/entity/Entity"}, 0, null));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_175729_l", "Lnet/minecraft/util/IntHashMap;"));
        list.add(new VarInsnNode(ILOAD, 1));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "func_76049_d", "(I)Ljava/lang/Object;", false));
        list.add(new InsnNode(POP));
        list.add(label4);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new JumpInsnNode(IFNONNULL, label5));
        list.add(label6);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_73032_d", "Ljava/util/Set;"));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "remove", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label7);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/multiplayer/WorldClient", "func_72900_e", "(Lnet/minecraft/entity/Entity;)V", false));
        list.add(label5);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InsnNode(ARETURN));
        list.add(label8);
        mn.instructions = list;

        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/multiplayer/WorldClient;", null, label0, label8, 0));
        mn.localVariables.add(new LocalVariableNode("entityID", "I", null, label0, label8, 1));
        mn.localVariables.add(new LocalVariableNode("entity", "Lnet/minecraft/entity/Entity;", null, label1, label8, 2));

        Launch.LOGGER.info("Overwrite removeEntityFromWorld.");
    }

    public static void InjectRemoveAllEntities(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_72997_g", "Ljava/util/List;"));
        list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/item/KillItem", "inList", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
        list.add(new InsnNode(POP));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into removeAllEntities.");
    }
}
