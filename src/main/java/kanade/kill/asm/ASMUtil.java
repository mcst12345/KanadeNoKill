package kanade.kill.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;

public class ASMUtil implements Opcodes {
    public static void InsertReturn(MethodNode mn, @Nullable Object type, @Nullable Object getReturn, int varIndex, AbstractInsnNode shouldReturn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        if (varIndex != -1) {
            list.add(new VarInsnNode(ALOAD, varIndex));
        }
        list.add(shouldReturn);
        list.add(new JumpInsnNode(IFEQ, label));
        if (type == Type.VOID_TYPE) {
            list.add(new InsnNode(RETURN));
        } else if (type == null) {
            if (getReturn == null) {
                list.add(new InsnNode(ACONST_NULL));
            } else {
                if (!(getReturn instanceof InsnList)) {
                    throw new IllegalArgumentException("field \"getReturn\" should be instanceof InsnList as the return value is an object.");
                }
                list.add((InsnList) getReturn);
            }
            list.add(new InsnNode(ARETURN));
        } else if (type == Type.BOOLEAN_TYPE) {
            list.add(new InsnNode(getReturn == Boolean.TRUE ? ICONST_1 : ICONST_0));
            list.add(new InsnNode(IRETURN));
        } else {
            list.add(new LdcInsnNode(getReturn));
            if (type == Type.INT_TYPE) {
                list.add(new InsnNode(IRETURN));
            } else if (type == Type.FLOAT_TYPE) {
                list.add(new InsnNode(FRETURN));
            } else if (type == Type.DOUBLE_TYPE) {
                list.add(new InsnNode(DRETURN));
            } else if (type == Type.LONG_TYPE) {
                list.add(new InsnNode(LRETURN));
            } else {
                throw new UnsupportedOperationException("Type " + type + " isn't supported yet!");
            }
        }
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        System.out.println("Insert return in " + mn.name);
    }

    public static void clearMethod(MethodNode mn) {
        mn.instructions.clear();
        if (!Modifier.isStatic(mn.access)) {
            mn.localVariables.removeIf(v -> v.index != 0);
        } else {
            mn.localVariables.clear();
        }
        mn.instructions.add(new InsnNode(RETURN));
    }
}
