package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
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
        byte[] transformed;
        switch (transformedName) {
            case "net.minecraft.server.MinecraftServer": {
                System.out.println("Get MinecraftServer.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.client.Minecraft": {
                System.out.println("Get Minecraft.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.item.ItemStack": {
                System.out.println("Get ItemStack.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.world.World": {
                System.out.println("Get World.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.entity.EntityLivingBase": {
                System.out.println("Get EntityLivingBase.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.entity.player.EntityPlayer": {
                System.out.println("Get EntityPlayer.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
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
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
            case "net.minecraft.util.NonNullList": {
                System.out.println("Get NonNullList.");
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "remove":
                        case "set": {
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", true));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z"));
                            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", true));
                            list.add(new InsnNode(Opcodes.ARETURN));
                            list.add(label);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            mn.instructions.insert(list);
                            switch (mn.name) {
                                case "set":
                                    System.out.println("Insert return in set.");
                                    break;
                                case "remove":
                                    System.out.println("Insert return in remove.");
                                    break;
                            }
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

                            list.add(label0);
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            list.add(new VarInsnNode(Opcodes.ISTORE, 1));
                            list.add(label1);
                            list.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/NonNullList", "size", "()I", true));
                            list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, label2));
                            list.add(label3);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/NonNullList", "get", "(I)Ljava/lang/Object;", true));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "kanade/kill/Util", "NoRemove", "(Ljava/lang/Object;)Z", false));
                            list.add(new JumpInsnNode(Opcodes.IFNE, label4));
                            list.add(label5);
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/util/NonNullList", "defaultElement", "Ljava/lang/Object;"));
                            list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/NonNullList", "set", "(ILjava/lang/Object;)V", true));
                            list.add(label4);
                            list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            list.add(new IincInsnNode(1, 1));
                            list.add(new JumpInsnNode(Opcodes.GOTO, label1));
                            list.add(label2);
                            list.add(new FrameNode(Opcodes.F_CHOP, 1, null, 0, null));
                            list.add(new InsnNode(Opcodes.RETURN));
                            list.add(label6);
                            mn.instructions.insert(list);

                            mn.localVariables.clear();
                            mn.localVariables.add(new LocalVariableNode("i", "I", null, label1, label2, 1));
                            mn.localVariables.add(new LocalVariableNode("this", "Lnet/minecraft/util/NonNullList;", null, label0, label6, 0));
                            System.out.println("Overwrite clear.");
                            break;
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
        return basicClass;
    }
}
