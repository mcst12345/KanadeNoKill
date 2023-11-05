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
public class Transformer implements IClassTransformer {
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
                cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "HatedByLife", "Z", null, null));
            }
            case "net.minecraft.server.MinecraftServer": {
                System.out.println("Get MinecraftServer.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_71190_q")) {
                        InsnList list = new InsnList();
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                        LabelNode label = new LabelNode();
                        list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                        list.add(new InsnNode(Opcodes.RETURN));
                        list.add(label);
                        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                        mn.instructions.insert(list);
                        System.out.println("Insert return in updateTimeLightAndEntities.");
                    }
                }
                break;
            }
            case "net.minecraft.client.Minecraft": {
                System.out.println("Get Minecraft.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_71407_l")) {
                        InsnList list = new InsnList();
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                        LabelNode label = new LabelNode();
                        list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                        list.add(new InsnNode(Opcodes.RETURN));
                        list.add(label);
                        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                        mn.instructions.insert(list);
                        System.out.println("Insert return in runTick.");
                    }
                }
                break;
            }
            case "net.minecraft.item.ItemStack": {
                System.out.println("Get ItemStack.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_190926_b")) {
                        InsnList list = new InsnList();
                        LabelNode label = new LabelNode();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/item/ItemStack", "field_151002_e", "Lnet/minecraft/item/Item;"));
                        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                        list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                        list.add(new InsnNode(Opcodes.ICONST_0));
                        list.add(new InsnNode(Opcodes.IRETURN));
                        list.add(label);
                        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                        mn.instructions.insert(list);
                        System.out.println("Insert return in isEmpty.");
                    }
                }
                break;
            }
            case "net.minecraft.world.World": {
                System.out.println("Get World.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72838_d": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in spawnEntity.");
                            break;
                        }
                        case "func_72923_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in onEntityAdded.");
                            break;
                        }
                        case "func_72973_f":
                        case "func_72900_e":
                        case "func_72847_b": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "func_72973_f":
                                    System.out.println("Insert return in removeEntityDangerously.");
                                    break;
                                case "func_72900_e":
                                    System.out.println("Insert return in removeEntity.");
                                    break;
                                case "func_72847_b":
                                    System.out.println("Insert return in onEntityRemoved.");
                                    break;
                            }
                            break;
                        }
                        case "func_175650_b": {
                            mn.instructions.clear();
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
                            list.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
                            list.add(new InsnNode(Opcodes.DUP));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 2));
                            list.add(label1);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(Opcodes.H_INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label2);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 3));
                            list.add(label3);
                            list.add(new FrameNode(Opcodes.F_APPEND, 2, new Object[]{"java/util/List", "java/util/Iterator"}, 0, null));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label4));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 4));
                            list.add(label5);
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
                            list.add(new TypeInsnNode(Opcodes.NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
                            list.add(new InsnNode(Opcodes.DUP));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label8);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/World", "func_72923_a", "(Lnet/minecraft/entity/Entity;)V", false));
                            list.add(label6);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new JumpInsnNode(Opcodes.GOTO, label3));
                            list.add(label4);
                            list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label9);
                            mn.instructions.add(list);
                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("entity4", "Lnet/minecraft/entity/Entity;", null, label5, label6, 4));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/World;", null, label0, label9, 0));
                            mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label9, 1));
                            mn.localVariables.add(new LocalVariableNode("list", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label9, 2));
                            System.out.println("Overwrite loadEntities.");
                            break;
                        }
                        case "func_175681_c": {
                            mn.instructions.clear();
                            InsnList list = new InsnList();
                            LabelNode label0 = new LabelNode();
                            LabelNode label1 = new LabelNode();
                            LabelNode label2 = new LabelNode();
                            LabelNode label3 = new LabelNode();
                            LabelNode label4 = new LabelNode();
                            list.add(label0);
                            list.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
                            list.add(new InsnNode(Opcodes.DUP));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 2));
                            list.add(label1);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(Opcodes.H_INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label2);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "field_72997_g", "Ljava/util/List;"));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label3);
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label4);
                            mn.instructions.add(list);
                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/World;", null, label0, label4, 0));
                            mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label4, 1));
                            mn.localVariables.add(new LocalVariableNode("list", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label4, 2));
                            System.out.println("Overwrite unloadEntities.");
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.EntityLivingBase": {
                System.out.println("Get EntityLivingBase.");

                System.out.println("Add field.");
                cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "Death_Time", "I", null, null));

                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70665_d":
                        case "func_70653_a":
                        case "func_70645_a":
                        case "func_70606_j": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "func_70645_a":
                                    System.out.println("Insert return in onDeath.");
                                    break;
                                case "func_70606_j":
                                    System.out.println("Insert return in setHealth.");
                                    break;
                                case "func_70653_a":
                                    System.out.println("Insert return in knockBack.");
                                    break;
                                case "func_70665_d":
                                    System.out.println("Insert return in damageEntity.");
                                    break;
                            }
                            break;
                        }
                        case "func_110138_aP":
                        case "func_110143_aJ": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new LdcInsnNode(20.0f));
                            list.add(new InsnNode(Opcodes.FRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "func_110138_aP":
                                    System.out.println("Insert return in getMaxHealth.");
                                    break;
                                case "func_110143_aJ":
                                    System.out.println("Insert return in getHealth.");
                                    break;
                            }
                            break;
                        }
                        case "func_70097_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in attackEntityFrom.");
                            break;
                        }
                        case "func_70089_S": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_1));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in isEntityAlive.");
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
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "func_70645_a":
                                    System.out.println("Insert return in onDeath.");
                                    break;
                                case "func_70665_d":
                                    System.out.println("Insert return in damageEntity.");
                                    break;
                                case "func_70106_y":
                                    System.out.println("Insert return in setDead.");
                                    break;
                            }
                            break;
                        }
                        case "func_70097_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in attackEntityFrom.");
                            break;
                        }
                        case "func_71019_a":
                        case "func_71040_bB":
                        case "func_145779_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ACONST_NULL));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in dropItem.");
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.util.NonNullList": {
                System.out.println("Get NonNullList.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "remove": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in remove.");
                            break;
                        }
                        case "clear": {
                            mn.instructions.clear();
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
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
                            list.add(new JumpInsnNode(Opcodes.IFNONNULL, label1));

                            list.add(label2);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(Opcodes.H_INVOKESTATIC, "net/minecraft/util/NonNullList", "lambda$clear$0", "(Ljava/lang/Object;)Z", false), Type.getType("(Ljava/lang/Object;)Z")));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/AbstractList", "removeIf", "(Ljava/util/function/Predicate;)Z", false));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(new JumpInsnNode(Opcodes.GOTO, label3));
                            list.add(label1);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new VarInsnNode(Opcodes.ISTORE, 1));
                            list.add(label4);
                            list.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "size", "()I", false));
                            list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, label3));
                            list.add(label5);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/util/NonNullList", "field_191199_b", "Ljava/lang/Object;"));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", false));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label6);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new IincInsnNode(1, 1));
                            list.add(new JumpInsnNode(Opcodes.GOTO, label4));
                            list.add(label3);
                            list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label8);
                            mn.instructions.insert(list);

                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("i", "I", null, label4, label3, 1));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", null, label0, label8, 0));
                            System.out.println("Overwrite clear.");
                            break;
                        }
                        case "set": {
                            mn.instructions.clear();
                            InsnList list = new InsnList();
                            LabelNode label0 = new LabelNode();
                            LabelNode label1 = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", false));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label0);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/util/NonNullList", "field_191198_a", "Ljava/util/List;"));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label1);

                            mn.instructions.add(list);
                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", "Lnet/minecraft/util/NonNullList<TE;>;", label0, label1, 0));
                            mn.localVariables.add(new LocalVariableNode("p_set_1_", "I", null, label0, label1, 1));
                            mn.localVariables.add(new LocalVariableNode("p_set_2_", "Ljava/lang/Object;", "TE;", label0, label1, 2));

                            System.out.println("Overwrite set.");
                            break;
                        }
                    }
                }
                MethodNode mn = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, "lambda$clear$0", "(Ljava/lang/Object;)Z", null, null);
                LabelNode label0 = new LabelNode();
                LabelNode label1 = new LabelNode();
                LabelNode label2 = new LabelNode();
                LabelNode label3 = new LabelNode();
                mn.instructions.add(label0);
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
                mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, label1));
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, label2));
                mn.instructions.add(label1);
                mn.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                mn.instructions.add(label2);
                mn.instructions.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.INTEGER}));
                mn.instructions.add(new InsnNode(Opcodes.IRETURN));
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
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in spawnEntity.");
                            break;
                        }
                        case "func_175650_b": {
                            mn.instructions.clear();
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
                            list.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
                            list.add(new InsnNode(Opcodes.DUP));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 2));
                            list.add(label1);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(Opcodes.H_INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label2);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 3));
                            list.add(label3);
                            list.add(new FrameNode(Opcodes.F_APPEND, 2, new Object[]{"java/util/List", "java/util/Iterator"}, 0, null));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label4));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true));
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 4));
                            list.add(label5);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/world/WorldServer", "func_184165_i", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label6));
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "Lnet/minecraftforge/fml/common/eventhandler/EventBus;"));
                            list.add(new TypeInsnNode(Opcodes.NEW, "net/minecraftforge/event/entity/EntityJoinWorldEvent"));
                            list.add(new InsnNode(Opcodes.DUP));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraftforge/event/entity/EntityJoinWorldEvent", "<init>", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", false));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/fml/common/eventhandler/EventBus", "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFNE, label6));
                            list.add(label7);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/WorldServer", "field_72996_f", "Ljava/util/List;"));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label8);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/WorldServer", "onEntityAdded", "(Lnet/minecraft/entity/Entity;)V", false));
                            list.add(label6);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new JumpInsnNode(Opcodes.GOTO, label3));
                            list.add(label4);
                            list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label9);
                            mn.instructions.insert(list);

                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("entity", "Lnet/minecraft/entity/Entity;", null, label5, label6, 4));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/world/WorldServer;", null, label0, label9, 0));
                            mn.localVariables.add(new LocalVariableNode("entityCollection", "Ljava/util/Collection;", "Ljava/util/Collection<Lnet/minecraft/entity/Entity;>;", label0, label9, 1));
                            mn.localVariables.add(new LocalVariableNode("lists", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/entity/Entity;>;", label1, label9, 2));

                            System.out.println("Overwrite loadEntities.");
                            break;
                        }
                        case "func_72847_b": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in onEntityRemoved.");
                            break;
                        }
                        case "func_72923_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in onEntityAdded.");
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
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new InsnNode(Opcodes.IRETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in spawnEntity.");
                            break;
                        }
                        case "func_72847_b":
                        case "func_72900_e": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "func_72900_e":
                                    System.out.println("Insert return in removeEntity.");
                                    break;
                                case "func_72847_b":
                                    System.out.println("Insert return in onEntityRemoved.");
                                    break;
                            }
                            break;
                        }
                        case "func_72923_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in onEntityAdded.");
                            break;
                        }
                        case "func_73027_a": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            System.out.println("Insert return in addEntityToWorld.");
                            break;
                        }
                        case "func_73028_b": {
                            mn.instructions.clear();
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
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_175729_l", "Lnet/minecraft/util/IntHashMap;"));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "lookup", "(I)Ljava/lang/Object;", false));
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
                            list.add(new VarInsnNode(Opcodes.ASTORE, 2));
                            list.add(label1);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label2));
                            list.add(label3);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label2);
                            list.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/entity/Entity"}, 0, null));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_175729_l", "Lnet/minecraft/util/IntHashMap;"));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/IntHashMap", "removeObject", "(I)Ljava/lang/Object;", false));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label4);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new JumpInsnNode(Opcodes.IFNONNULL, label5));
                            list.add(label6);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_73032_d", "Ljava/util/Set;"));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "remove", "(Ljava/lang/Object;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(label7);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/multiplayer/WorldClient", "func_72900_e", "(Lnet/minecraft/entity/Entity;)V", false));
                            list.add(label5);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label8);
                            mn.instructions.insert(list);

                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/client/multiplayer/WorldClient;", null, label0, label8, 0));
                            mn.localVariables.add(new LocalVariableNode("entityID", "I", null, label0, label8, 1));
                            mn.localVariables.add(new LocalVariableNode("entity", "Lnet/minecraft/entity/Entity;", null, label1, label8, 2));

                            System.out.println("Overwrite removeEntityFromWorld.");
                            break;
                        }
                        case "func_73022_a": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/WorldClient", "field_72997_g", "Ljava/util/List;"));
                            list.add(new InvokeDynamicInsnNode("test", "()Ljava/util/function/Predicate;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("(Ljava/lang/Object;)Z"), new Handle(Opcodes.H_INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false), Type.getType("(Lnet/minecraft/entity/Entity;)Z")));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeIf", "(Ljava/util/function/Predicate;)Z", true));
                            list.add(new InsnNode(Opcodes.POP));
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
                        InsnList list = new InsnList();
                        LabelNode label = new LabelNode();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/KillItem", "inList", "(Lnet/minecraft/entity/Entity;)Z", false));
                        list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                        list.add(new InsnNode(Opcodes.ICONST_0));
                        list.add(new InsnNode(Opcodes.IRETURN));
                        list.add(label);
                        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                        System.out.println("Insert return in onLivingUpdate.");
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
                File file = new File(path);
                if (file.getParentFile() != null && file.getParentFile().getName().equals("mods")) {
                    goodClass = false;
                }
            } else {
                goodClass = false;
            }
        }

        System.out.println("Examine class:" + transformedName);

        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    switch (fin.name) {
                        case "field_70128_L": {
                            if (fin.getOpcode() == Opcodes.GETFIELD) {
                                System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife");
                                fin.name = "HatedByLife";
                            } else if (goodClass) {
                                System.out.println("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife");
                                fin.name = "HatedByLife";
                            }
                            break;
                        }
                        case "field_70725_aQ": {
                            if (fin.getOpcode() == Opcodes.GETFIELD) {
                                System.out.println("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time");
                                fin.name = "Death_Time";
                            } else if (goodClass) {
                                System.out.println("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time");
                                fin.name = "Death_Time";
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
