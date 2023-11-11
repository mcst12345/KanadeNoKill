package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

@SuppressWarnings("SpellCheckingInspection")
public class Transformer implements IClassTransformer, Opcodes {
    public static final boolean debug = System.getProperty("Debug") != null;
    public static final Transformer instance = new Transformer();

    private Transformer(){}

    private static void save(byte[] clazz, String file) {
        if (!debug) {
            return;
        }
        try {
            Files.write(new File(file + ".class").toPath(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodInsnNode isDead() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false);
    }

    private static MethodInsnNode inList() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Ljava/lang/Object;)Z", false);
    }

    private static MethodInsnNode NoRemove() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        byte[] transformed;
        switch (transformedName) {
            case "net.minecraft.entity.Entity": {
                System.out.println("Get Entity.");

                System.out.println("Add field.");
                cn.fields.add(new FieldNode(ACC_PUBLIC, "HatedByLife", "Z", null, null));
            }
            case "net.minecraft.server.MinecraftServer": {
                System.out.println("Get MinecraftServer.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_71190_q")) {
                        ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                    }
                }
                break;
            }
            case "net.minecraft.client.Minecraft": {
                System.out.println("Get Minecraft.");
                System.out.println("Adding field.");
                cn.fields.add(new FieldNode(ACC_PUBLIC, "PLAYER", "Lnet/minecraft/client/entity/EntityPlayerSP;", null, null));

                System.out.println("Adding method.");

                MethodNode inGameFocus = new MethodNode(ACC_PUBLIC, "SetIngameFocus", "()V", null, null);
                LabelNode label0 = new LabelNode();
                LabelNode label1 = new LabelNode();
                LabelNode label2 = new LabelNode();
                LabelNode label3 = new LabelNode();
                inGameFocus.instructions.add(label0);
                inGameFocus.instructions.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/Display", "isActive", "()Z", false));
                inGameFocus.instructions.add(new JumpInsnNode(IFEQ, label1));
                inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "inGameHasFocus", "Z"));
                inGameFocus.instructions.add(new JumpInsnNode(IFNE, label1));
                inGameFocus.instructions.add(new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "IS_RUNNING_ON_MAC", "Z"));
                inGameFocus.instructions.add(new JumpInsnNode(IFNE, label2));
                inGameFocus.instructions.add(new MethodInsnNode(INVOKESTATIC, "net/minecraft/client/settings/KeyBinding", "updateKeyBindState", "()V", false));
                inGameFocus.instructions.add(label2);
                inGameFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
                inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameFocus.instructions.add(new InsnNode(ICONST_1));
                inGameFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "inGameHasFocus", "Z"));
                inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "mouseHelper", "Lnet/minecraft/util/MouseHelper;"));
                inGameFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/MouseHelper", "grabMouseCursor", "()V", false));
                inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameFocus.instructions.add(new InsnNode(ACONST_NULL));
                inGameFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/Minecraft", "displayGuiScreen", "(Lnet/minecraft/client/gui/GuiScreen;)V", false));
                inGameFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameFocus.instructions.add(new IntInsnNode(SIPUSH, 10000));
                inGameFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "leftClickCounter", "I"));
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
                inGameNotFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "inGameHasFocus", "Z"));
                inGameNotFocus.instructions.add(new JumpInsnNode(IFEQ, l1));
                inGameNotFocus.instructions.add(l4);
                inGameNotFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameNotFocus.instructions.add(new InsnNode(ICONST_0));
                inGameNotFocus.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/client/Minecraft", "inGameHasFocus", "Z"));
                inGameNotFocus.instructions.add(l3);
                inGameNotFocus.instructions.add(new VarInsnNode(ALOAD, 0));
                inGameNotFocus.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "mouseHelper", "Lnet/minecraft/util/MouseHelper;"));
                inGameNotFocus.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/MouseHelper", "ungrabMouseCursor", "()V", false));
                inGameNotFocus.instructions.add(l1);
                inGameNotFocus.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
                inGameNotFocus.instructions.add(new InsnNode(RETURN));
                inGameNotFocus.instructions.add(l2);
                inGameNotFocus.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/Minecraft;", null, l0, l2, 0));
                cn.methods.add(inGameNotFocus);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_147108_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new JumpInsnNode(IFNULL, label));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(inList());
                            list.add(new JumpInsnNode(IFEQ, label));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/client/gui/GuiGameOver"));
                            list.add(new JumpInsnNode(IFEQ, label));
                            list.add(new InsnNode(RETURN));
                            list.add(label);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Inject into " + mn.name);
                            break;
                        }
                        case "func_71407_l": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                            break;
                        }
                        case "func_71381_h":
                        case "func_71364_i": {
                            ASMUtil.clearMethod(mn);
                        }
                    }
                }
                break;
            }
            case "net.minecraft.item.ItemStack": {
                System.out.println("Get ItemStack.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_190926_b")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, NoRemove());
                    }
                }
                MethodNode mn = new MethodNode(ACC_PUBLIC, "getITEM", "()Lnet/minecraft/item/Item;", null, null);
                LabelNode label0 = new LabelNode();
                LabelNode label1 = new LabelNode();
                mn.instructions.add(label0);
                mn.instructions.add(new VarInsnNode(ALOAD, 0));
                mn.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "field_151002_e", "Lnet/minecraft/item/Item;"));
                mn.instructions.add(new InsnNode(ARETURN));
                mn.instructions.add(label1);
                mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/item/ItemStack;", null, label0, label1, 0));
                System.out.println("Adding method.");
                cn.methods.add(mn);
                break;
            }
            case "net.minecraft.world.World": {
                System.out.println("Get World.");
                System.out.println("Adding field.");
                cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_FINAL, "protects", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", null));


                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "<init>": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
                            list.add(new InsnNode(DUP));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
                            list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
                            mn.instructions.insert(list);
                            System.out.println("Inject into <init>.");
                            break;
                        }
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, isDead());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, isDead());
                            break;
                        }
                        case "func_72973_f":
                        case "func_72900_e":
                        case "func_72847_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, inList());
                            break;
                        }
                        case "func_175650_b": {
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
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
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
                            list.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
                            list.add(new TypeInsnNode(NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
                            list.add(new InsnNode(DUP));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
                            list.add(new JumpInsnNode(IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(POP));
                            list.add(label8);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "func_72923_a", "(Lnet/minecraft/entity/Entity;)V", false));
                            list.add(label6);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            list.add(new JumpInsnNode(GOTO, label3));
                            list.add(label4);
                            list.add(new FrameNode(F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(RETURN));
                            list.add(label9);
                            mn.instructions = list;
                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("entity4", "Lnet/minecraft/entity/Entity;", null, label5, label6, 4));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/World;", null, label0, label9, 0));
                            mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label9, 1));
                            mn.localVariables.add(new LocalVariableNode("list", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label9, 2));
                            System.out.println("Overwrite loadEntities.");
                            break;
                        }
                        case "func_175681_c": {
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
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
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
                            System.out.println("Overwrite unloadEntities.");
                            break;
                        }
                        case "func_72939_s": {
                            InsnList list = new InsnList();
                            LabelNode label0 = new LabelNode();
                            LabelNode label1 = new LabelNode();
                            LabelNode label2 = new LabelNode();
                            LabelNode label3 = new LabelNode();
                            LabelNode label4 = new LabelNode();
                            LabelNode label5 = new LabelNode();
                            list.add(label0);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
                            list.add(new LdcInsnNode(Type.getType("Ljava/util/ArrayList;")));
                            list.add(new JumpInsnNode(IF_ACMPEQ, label1));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
                            list.add(new InsnNode(DUP));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
                            list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(label1);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "protects", "Ljava/util/List;"));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
                            list.add(new VarInsnNode(ASTORE, 1));
                            list.add(label4);
                            list.add(new FrameNode(F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
                            list.add(new JumpInsnNode(IFEQ, label2));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
                            list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
                            list.add(new VarInsnNode(ASTORE, 2));
                            list.add(label5);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "contains", "(Ljava/lang/Object;)Z", true));
                            list.add(new JumpInsnNode(IFNE, label3));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(POP));
                            list.add(label3);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new JumpInsnNode(GOTO, label4));
                            list.add(label2);
                            list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));

                            mn.instructions.insert(list);

                            mn.localVariables.add(new LocalVariableNode("e", "Lnet/minecraft/entity/Entity;", null, label5, label3, 2));
                            System.out.println("Inject into updateEntities.");
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.EntityLivingBase": {
                System.out.println("Get EntityLivingBase.");

                System.out.println("Add field.");
                cn.fields.add(new FieldNode(ACC_PUBLIC, "Death_Time", "I", null, null));

                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70665_d":
                        case "func_70653_a":
                        case "func_70645_a":
                        case "func_70606_j": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, inList());
                            break;
                        }
                        case "func_110138_aP":
                        case "func_110143_aJ": {
                            ASMUtil.InsertReturn(mn, Type.FLOAT_TYPE, 20.0f, 0, inList());
                            break;
                        }
                        case "func_70097_a": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, inList());
                            break;
                        }
                        case "func_70089_S": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.TRUE, 0, inList());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayer": {
                System.out.println("Get EntityPlayer.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70106_y":
                        case "func_70665_d":
                        case "func_70645_a": {

                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, inList());
                            break;
                        }
                        case "func_70097_a": {

                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, inList());
                            break;
                        }
                        case "func_71019_a":
                        case "func_71040_bB":
                        case "func_145779_a": {

                            ASMUtil.InsertReturn(mn, null, null, 0, inList());
                            break;
                        }
                        case "func_70071_h_": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "invHaveKillItem", "(Lnet/minecraft/entity/player/EntityPlayer;)Z"));
                            list.add(new JumpInsnNode(IFEQ, label));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/KillItem", "AddToList", "(Ljava/lang/Object;)V", false));
                            list.add(label);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Inject into onUpdate.");
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerSP": {
                System.out.println("Get EntityPlayerSP.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70097_a": {

                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, inList());
                            break;
                        }
                        case "func_71040_bB": {

                            ASMUtil.InsertReturn(mn, null, null, 0, inList());
                            break;
                        }
                        case "func_71150_b":
                        case "func_70665_d": {

                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, inList());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerMP": {
                System.out.println("Get EntityPlayerMP.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70645_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, inList());
                            break;
                        }
                        case "func_152339_d": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, inList());
                            break;
                        }
                        case "func_70097_a": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, inList());
                            break;
                        }
                    }
                }
            }
            case "net.minecraft.util.NonNullList": {
                System.out.println("Get NonNullList.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "remove": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                            list.add(new JumpInsnNode(IFEQ, label));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new InsnNode(ARETURN));
                            list.add(label);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in remove.");
                            break;
                        }
                        case "clear": {
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
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
                            list.add(new JumpInsnNode(IFNONNULL, label1));

                            list.add(label2);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "net/minecraft/util/NonNullList", "lambda$clear$0", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "java/util/AbstractList", "removeIf", "(Ljava/util/function/Predicate;)Z", false));
                            list.add(new InsnNode(POP));
                            list.add(new JumpInsnNode(GOTO, label3));
                            list.add(label1);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            list.add(new InsnNode(ICONST_0));
                            list.add(new VarInsnNode(ISTORE, 1));
                            list.add(label4);
                            list.add(new FrameNode(F_APPEND, 1, new Object[]{INTEGER}, 0, null));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "size", "()I", false));
                            list.add(new JumpInsnNode(IF_ICMPGE, label3));
                            list.add(label5);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
                            list.add(new JumpInsnNode(IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", false));
                            list.add(new InsnNode(POP));
                            list.add(label6);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            list.add(new IincInsnNode(1, 1));
                            list.add(new JumpInsnNode(GOTO, label4));
                            list.add(label3);
                            list.add(new FrameNode(F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(RETURN));
                            list.add(label8);
                            mn.instructions = list;

                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("i", "I", null, label4, label3, 1));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", null, label0, label8, 0));
                            System.out.println("Overwrite clear.");
                            break;
                        }
                        case "set": {
                            InsnList list = new InsnList();
                            LabelNode label0 = new LabelNode();
                            LabelNode label1 = new LabelNode();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                            list.add(new JumpInsnNode(IFEQ, label0));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new InsnNode(ARETURN));
                            list.add(label0);
                            list.add(new FrameNode(F_SAME, 0, null, 0, null));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/NonNullList", "field_191198_a", "Ljava/util/List;"));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true));
                            list.add(new InsnNode(ARETURN));
                            list.add(label1);

                            mn.instructions = list;
                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", "Lnet/minecraft/util/NonNullList<TE;>;", label0, label1, 0));
                            mn.localVariables.add(new LocalVariableNode("p_set_1_", "I", null, label0, label1, 1));
                            mn.localVariables.add(new LocalVariableNode("p_set_2_", "Ljava/lang/Object;", "TE;", label0, label1, 2));

                            System.out.println("Overwrite set.");
                            break;
                        }
                    }
                }
                MethodNode mn = new MethodNode(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$clear$0", "(Ljava/lang/Object;)Z", null, null);
                LabelNode label0 = new LabelNode();
                LabelNode label1 = new LabelNode();
                LabelNode label2 = new LabelNode();
                LabelNode label3 = new LabelNode();
                mn.instructions.add(label0);
                mn.instructions.add(new VarInsnNode(ALOAD, 0));
                mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
                mn.instructions.add(new JumpInsnNode(IFNE, label1));
                mn.instructions.add(new InsnNode(ICONST_1));
                mn.instructions.add(new JumpInsnNode(GOTO, label2));
                mn.instructions.add(label1);
                mn.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
                mn.instructions.add(new InsnNode(ICONST_0));
                mn.instructions.add(label2);
                mn.instructions.add(new FrameNode(F_SAME1, 0, null, 1, new Object[]{INTEGER}));
                mn.instructions.add(new InsnNode(IRETURN));
                mn.instructions.add(label3);
                mn.localVariables.add(new LocalVariableNode("o", "Ljava/lang/Object;", null, label0, label3, 0));
                cn.methods.add(mn);
                System.out.println("Add lambda method.");

                break;
            }
            case "net.minecraft.world.WorldServer": {
                System.out.println("Get WorldServer.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, isDead());
                            break;
                        }
                        case "func_175650_b": {
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
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
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
                            list.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
                            list.add(new TypeInsnNode(NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
                            list.add(new InsnNode(DUP));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
                            list.add(new JumpInsnNode(IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/WorldServer", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(POP));
                            list.add(label8);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 4));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/WorldServer", "onEntityAdded", "(Lnet/minecraft/entity/Entity;)V", false));
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

                            System.out.println("Overwrite loadEntities.");
                            break;
                        }
                        case "func_72847_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, inList());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, isDead());
                            break;
                        }
                    }

                }
                break;
            }
            case "net.minecraft.world.WorldClient": {
                System.out.println("Get WorldClient.");

                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, isDead());
                            break;
                        }
                        case "func_72847_b":
                        case "func_72900_e": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, inList());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, isDead());
                            break;
                        }
                        case "func_73027_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 2, isDead());
                            break;
                        }
                        case "func_73028_b": {
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
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "lookup", "(I)Ljava/lang/Object;", false));
                            list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/Entity"));
                            list.add(new VarInsnNode(ASTORE, 2));
                            list.add(label1);
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Ljava/lang/Object;)Z", false));
                            list.add(new JumpInsnNode(IFEQ, label2));
                            list.add(label3);
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new InsnNode(ARETURN));
                            list.add(label2);
                            list.add(new FrameNode(F_APPEND, 1, new Object[]{"net/minecraft/entity/Entity"}, 0, null));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_175729_l", "Lnet/minecraft/util/IntHashMap;"));
                            list.add(new VarInsnNode(ILOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "removeObject", "(I)Ljava/lang/Object;", false));
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

                            System.out.println("Overwrite removeEntityFromWorld.");
                            break;
                        }
                        case "func_73022_a": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_72997_g", "Ljava/util/List;"));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(H_INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
                            list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
                            list.add(new InsnNode(POP));
                            mn.instructions.insert(list);
                            System.out.println("Inject into removeAllEntities.");
                            break;
                        }
                    }
                }

                break;
            }
            case "net.minecraftforge.common.ForgeHooks": {
                System.out.println("Get ForgeHooks.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("onLivingUpdate")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, inList());
                    }
                }
                break;
            }
            case "net.minecraftforge.common.MinecraftForge": {
                System.out.println("Get MinecraftForge.");
                System.out.println("Add field.");
                cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;", null, null));
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<clinit>")) {
                        InsnList list = new InsnList();
                        list.add(new TypeInsnNode(NEW, "net/minecraftforge/fml/common/eventhandler/EventBus"));
                        list.add(new InsnNode(DUP));
                        list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "<init>", "()V", false));
                        list.add(new FieldInsnNode(PUTSTATIC, "net/minecraftforge/common/MinecraftForge", "Event_bus", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
                        mn.instructions.insert(list);
                        System.out.println("Inject into <clinit>.");
                    }
                }
            }
        }

        boolean goodClass = true;
        if (name.equals(transformedName)) {
            final URL res = Launch.classLoader.findResource(name.replace('.', '/').concat(".class"));

            if (res != null) {
                String path = res.getPath();

                if (path.contains("!")) {
                    path = path.substring(0, path.indexOf("!"));
                }
                if (path.contains("file:/")) {
                    path = path.replace("file:/", "");
                }

                if (path.startsWith("mods", path.lastIndexOf(File.separator) - 4)) {
                    goodClass = false;
                }
            }
        }

        System.out.println("Examine class:" + transformedName);

        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    switch (fin.name) {
                        case "field_70128_L": {
                            if (fin.getOpcode() == GETFIELD) {
                                System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife");
                                fin.name = "HatedByLife";
                            } else if (goodClass) {
                                System.out.println("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife");
                                fin.name = "HatedByLife";
                            }
                            break;
                        }
                        case "field_70725_aQ": {
                            if (fin.getOpcode() == GETFIELD) {
                                System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time");
                                fin.name = "Death_Time";
                            } else if (goodClass) {
                                System.out.println("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time");
                                fin.name = "Death_Time";
                            }
                            break;
                        }
                        case "EVENT_BUS": {
                            if (fin.owner.equals("net/minecraftforge/common/MinecraftForge")) {
                                if (fin.getOpcode() == GETSTATIC) {
                                    fin.name = "Event_bus";
                                    System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":EVENT_BUS to Event_bus");
                                } else if (goodClass) {
                                    fin.name = "Event_bus";
                                    System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":EVENT_BUS to Event_bus");
                                }
                            }
                            break;
                        }
                        case "field_71439_g": {
                            if (fin.getOpcode() == GETFIELD) {
                                System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":player to PLAYER");
                                fin.name = "PLAYER";
                            } else if (goodClass) {
                                System.out.println("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":player to PLAYER");
                                fin.name = "PLAYER";
                            }
                            break;
                        }
                    }
                } else if (ain instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    switch (min.name) {
                        case "func_71381_h": {
                            if (goodClass) {
                                min.name = "SetIngameFocus";
                            }
                            break;
                        }
                        case "func_71364_i": {
                            if (goodClass) {
                                min.name = "SetIngameNotInFocus";
                            }
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        transformed = cw.toByteArray();
        save(transformed, transformedName);
        return transformed;
    }
}
