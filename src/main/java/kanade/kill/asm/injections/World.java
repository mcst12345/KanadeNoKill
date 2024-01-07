package kanade.kill.asm.injections;

import kanade.kill.Launch;
import kanade.kill.asm.ASMUtil;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class World implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "protects", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", null));
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "entities", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", null));
        cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "players", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "field_73010_i", "Ljava/util/List;"));
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into <init>.");
    }

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
        list.add(label0);
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/util/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z"), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label2);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 3));
        list.add(label3);
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label4));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 4));
        list.add(label5);
        list.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "Event_bus", "Lcpw/mods/fml/common/eventhandler/EventBus;"));
        list.add(new TypeInsnNode(NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/common/eventhandler/EventBus", "post", "(Lcpw/mods/fml/common/eventhandler/Event;)Z", false));
        list.add(new JumpInsnNode(IFNE, label6));
        list.add(label7);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label8);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "func_72923_a", "(Lnet/minecraft/entity/Entity;)V", false));
        list.add(label6);
        list.add(new JumpInsnNode(GOTO, label3));
        list.add(label4);
        list.add(new InsnNode(RETURN));
        list.add(label9);
        mn.instructions = list;
        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("entity4", "Lnet/minecraft/entity/Entity;", null, label5, label6, 4));
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/World;", null, label0, label9, 0));
        mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label9, 1));
        mn.localVariables.add(new LocalVariableNode("list", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label9, 2));
        Launch.LOGGER.info("Overwrite loadEntities(Collection<Entity>).");
    }

    public static void OverwriteUnloadEntities(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        list.add(label0);
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/item/KillItem", "inList", "(Ljava/lang/Object;)Z"), Type.getType("(Ljava/lang/Object;)Z")));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label2);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72997_g", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label3);
        list.add(new InsnNode(RETURN));
        list.add(label4);
        mn.instructions = list;
        mn.localVariables.clear();
        mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/World;", null, label0, label4, 0));
        mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label4, 1));
        mn.localVariables.add(new LocalVariableNode("list", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label4, 2));
        Launch.LOGGER.info("Overwrite unloadEntities(Collection<Entity>).");
    }

    public static void InjectCountEntities1(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        LabelNode label5 = new LabelNode();
        list.add(label0);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
        list.add(new LdcInsnNode(Type.getType("Ljava/util/ArrayList;")));
        list.add(new JumpInsnNode(IF_ACMPEQ, label1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label4);
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label2));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 3));
        list.add(label5);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "contains", "(Ljava/lang/Object;)Z", true));
        list.add(new JumpInsnNode(IFNE, label3));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label3);
        list.add(new JumpInsnNode(GOTO, label4));
        list.add(label2);

        mn.instructions.insert(list);

        mn.localVariables.add(new LocalVariableNode("e", "Lnet/minecraft/entity/Entity;", null, label5, label3, 3));
        Launch.LOGGER.info("Inject into countEntities(Class<?>).");
    }

    public static void InjectCountEntities2(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        LabelNode label5 = new LabelNode();
        list.add(label0);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
        list.add(new LdcInsnNode(Type.getType("Ljava/util/ArrayList;")));
        list.add(new JumpInsnNode(IF_ACMPEQ, label1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 3));
        list.add(label4);
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label2));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 4));
        list.add(label5);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "contains", "(Ljava/lang/Object;)Z", true));
        list.add(new JumpInsnNode(IFNE, label3));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 4));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label3);
        list.add(new JumpInsnNode(GOTO, label4));
        list.add(label2);

        mn.instructions.insert(list);

        mn.localVariables.add(new LocalVariableNode("e", "Lnet/minecraft/entity/Entity;", null, label5, label3, 4));

        Launch.LOGGER.info("Inject into countEntities(net.minecraft.entity.EnumCreatureType,boolean).");
    }

    public static void InjectUpdateEntities(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        LabelNode label6 = new LabelNode();

        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
        list.add(new JumpInsnNode(IFNE, label));
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/Launch", "client", "Z"));
        list.add(new JumpInsnNode(IFEQ, label6));
        list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "dead", "Z"));
        list.add(new JumpInsnNode(IFNE, label));
        list.add(new JumpInsnNode(GOTO, label6));
        list.add(label);
        list.add(new InsnNode(RETURN));
        list.add(label6);

        LabelNode label0 = new LabelNode();
        LabelNode label1 = new LabelNode();
        LabelNode label2 = new LabelNode();
        LabelNode label3 = new LabelNode();
        LabelNode label4 = new LabelNode();
        LabelNode label5 = new LabelNode();
        list.add(label0);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
        list.add(new LdcInsnNode(Type.getType("Ljava/util/ArrayList;")));
        list.add(new JumpInsnNode(IF_ACMPEQ, label1));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(label1);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
        list.add(new VarInsnNode(ASTORE, 1));
        list.add(label4);
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
        list.add(new JumpInsnNode(IFEQ, label2));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
        list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
        list.add(new VarInsnNode(ASTORE, 2));
        list.add(label5);
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "contains", "(Ljava/lang/Object;)Z", true));
        list.add(new JumpInsnNode(IFNE, label3));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "entities", "Ljava/util/List;"));
        list.add(new VarInsnNode(ALOAD, 2));
        list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
        list.add(new InsnNode(POP));
        list.add(label3);
        list.add(new JumpInsnNode(GOTO, label4));
        list.add(label2);
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into updateEntities().");
    }

    public static void InjectUpdateEntityWithOptionalForce(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label0 = new LabelNode();
        list.add(ASMUtil.isTimeStop());
        list.add(new JumpInsnNode(IFEQ, label0));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(ASMUtil.inList());
        list.add(new JumpInsnNode(IFNE, label0));
        list.add(new InsnNode(RETURN));
        list.add(label0);
        mn.instructions.insert(list);
        Launch.LOGGER.info("Inject into InjectUpdateEntityWithOptionalForce().");
    }
}
