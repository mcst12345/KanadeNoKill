package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class Transformer implements IClassTransformer {
    public static final Transformer instance = new Transformer();
    private Transformer(){}
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.equals("net.minecraft.server.MinecraftServer")){
            System.out.println("Get MinecraftServer.");
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn,0);
            for(MethodNode mn : cn.methods){
                if (mn.name.equals("func_71190_q")) {
                    InsnList list = new InsnList();
                    list.add(new FieldInsnNode(Opcodes.GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                    LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                    list.add(new InsnNode(Opcodes.RETURN));
                    list.add(label);
                    mn.instructions.insert(list);
                    System.out.println("Insert return in updateTimeLightAndEntities.");
                }
            }
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        } else if (transformedName.equals("net.minecraft.client.Minecraft")) {
            System.out.println("Get Minecraft.");
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn,0);
            for(MethodNode mn : cn.methods){
                if (mn.name.equals("func_71407_l")) {
                    InsnList list = new InsnList();
                    list.add(new FieldInsnNode(Opcodes.GETSTATIC, "kanade/kill/Util", "killing", "Z"));
                    LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                    list.add(new InsnNode(Opcodes.RETURN));
                    list.add(label);
                    mn.instructions.insert(list);
                    System.out.println("Insert return in runTick.");
                }
            }
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        }
        return basicClass;
    }
}
