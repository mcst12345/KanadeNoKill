package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
                        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/item/ItemStack", "item", "Lnet/minecraft/item/Item;"));
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "kanade/kill/ModMain", "kill_item", "Lnet/minecraft/item/Item;"));
                        list.add(new JumpInsnNode(Opcodes.IF_ACMPNE, label));
                        list.add(new InsnNode(Opcodes.ICONST_0));
                        list.add(new InsnNode(Opcodes.IRETURN));
                        list.add(label);
                        list.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                        mn.instructions.insert(list);
                        System.out.println("Insert return in isEmpty.");
                    }
                }
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                transformed = cw.toByteArray();
                save(transformed, transformedName);
                return transformed;
            }
        }
        return basicClass;
    }
}
