package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RenderPlayer implements Opcodes {
    public static void InjectConstructor(MethodNode mn){
        AbstractInsnNode index = null;
        for(AbstractInsnNode ain : mn.instructions.toArray()){
            if(ain instanceof InsnNode && ain.getOpcode() == RETURN){
                index = ain;
                break;
            }
        }
        if(index == null){
            throw new IllegalStateException("The fuck?");
        }
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD,0));
        list.add(new TypeInsnNode(NEW,"kanade/kill/render/WingLayer"));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD,0));
        list.add(new MethodInsnNode(INVOKESPECIAL,"kanade/kill/render/WingLayer","<init>","(Lnet/minecraft/client/renderer/entity/RenderLivingBase;)V",false));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/renderer/entity/RenderPlayer", "func_177094_a", "(Lnet/minecraft/client/renderer/entity/layers/LayerRenderer;)Z", false));
        list.add(new InsnNode(POP));
        mn.instructions.insertBefore(index,list);
        Launch.LOGGER.info("Inject into <init>.");
    }
}
