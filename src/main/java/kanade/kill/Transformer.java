package kanade.kill;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class Transformer implements IClassTransformer {
    public static final Transformer instance = new Transformer();
    private Transformer(){}
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.equals("net.minecraft.server.MinecraftServer")){
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn,0);
            for(MethodNode mn : cn.methods){
                if(mn.name.equals("updateTimeLightAndEntities")){
                    InsnList list = new InsnList();
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"kanade/kill/Util","isKilling","()Z"));
                    LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ,label));
                    list.add(new InsnNode(Opcodes.RETURN));
                    list.add(label);
                    mn.instructions.insert(list);
                }
            }
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        } else if (transformedName.equals("net.minecraft.client.Minecraft")) {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn,0);
            for(MethodNode mn : cn.methods){
                if(mn.name.equals("runTick")){
                    InsnList list = new InsnList();
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"kanade/kill/Util","isKilling","()Z"));
                    LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(Opcodes.IFEQ,label));
                    list.add(new InsnNode(Opcodes.RETURN));
                    list.add(label);
                    mn.instructions.insert(list);
                }
            }
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        }
        return basicClass;
    }
}
