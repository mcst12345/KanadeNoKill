package kanade.kill.asm;

import kanade.kill.Core;
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
        Core.LOGGER.info("Insert return in " + mn.name);
    }

    public static void InsertReturn(MethodNode mn, Type type) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();
        list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "allReturn", "Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        if (mn.name.startsWith("func_")) {
            list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "Annihilation", "Z"));
            list.add(new JumpInsnNode(IFEQ, label));
        }
        switch (type.getSort()) {
            case Type.VOID: {
                list.add(new InsnNode(RETURN));
                break;
            }
            case Type.SHORT:
            case Type.CHAR:
            case Type.BYTE:
            case Type.INT:
            case Type.BOOLEAN: {
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(IRETURN));
                break;
            }
            case Type.FLOAT: {
                list.add(new InsnNode(FCONST_0));
                list.add(new InsnNode(FRETURN));
                break;
            }
            case Type.LONG: {
                list.add(new InsnNode(LCONST_0));
                list.add(new InsnNode(LRETURN));
                break;
            }
            case Type.DOUBLE: {
                list.add(new InsnNode(DCONST_0));
                list.add(new InsnNode(DRETURN));
                break;
            }
            case Type.OBJECT: {
                list.add(new InsnNode(ACONST_NULL));
                list.add(new InsnNode(ARETURN));
                break;
            }
            default: {
                throw new IllegalStateException("The fuck?");
            }
        }
        list.add(label);
        list.add(new FrameNode(F_SAME, 0, null, 0, null));
        mn.instructions.insert(list);
        Core.LOGGER.info("Insert return in " + mn.name + ".");
    }
    public static void clearMethod(MethodNode mn) {
        mn.instructions.clear();
        if (!Modifier.isStatic(mn.access)) {
            mn.localVariables.removeIf(v -> v.index != 0);
        } else {
            mn.localVariables.clear();
        }
        mn.instructions.add(new InsnNode(RETURN));
        Core.LOGGER.info("Clear method:" + mn.name + ".");
    }

    public static MethodInsnNode isDead() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "isDead", "(Lnet/minecraft/entity/Entity;)Z", false);
    }

    public static MethodInsnNode inList() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/item/KillItem", "inList", "(Ljava/lang/Object;)Z", false);
    }

    public static MethodInsnNode NoRemove() {
        return new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "NoRemove", "(Ljava/lang/Object;)Z");
    }

    public static int BadMethod(MethodNode mn) {
        if (Modifier.isAbstract(mn.access) || mn.name.equals("<init>") || mn.name.equals("<clinit>")) {
            return 0;
        }
        if (Modifier.isNative(mn.access)) {
            Core.LOGGER.warn("Remove method:" + mn.name);
            return 2;
        }
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof MethodInsnNode) {
                MethodInsnNode min = (MethodInsnNode) ain;
                if (min.owner.equals("sun/misc/Unsafe") || min.owner.contains("java/lang/reflect") || min.owner.contains("sun/tools") || (min.owner.contains("lwjgl") && !(min.name.equals("getEventButton") || min.name.equals("getEventButtonState") || min.name.equals("getEventDWheel"))) || (min.owner.equals("java/lang/System") && (min.name.equals("exit") || min.name.equals("load") || min.name.equals("loadLibrary"))) || min.owner.equals("java/lang/Runtime") || min.owner.contains("ReflectionHelper") || min.owner.contains("opengl")) {
                    Core.LOGGER.warn("Remove method:" + mn.name);
                    return 2;
                }
                switch (min.name) {
                    case "func_110143_aJ":
                    case "func_70106_y":
                    case "func_70659_e":
                    case "func_70645_a":
                    case "func_130011_c":
                    case "func_70606_j":
                    case "func_70097_a":
                    case "func_111128_a":
                    case "func_175681_c":
                    case "func_72960_a":
                    case "func_76622_b":
                    case "func_72847_b":
                    case "func_110142_aN":
                    case "func_174925_a":
                    case "func_94547_a":
                    case "func_76359_i":
                    case "func_70074_a":
                    case "func_70103_a":
                    case "func_70674_bp":
                    case "func_70436_m":
                    case "func_71053_j":
                    case "func_194028_b":
                    case "func_72900_e":
                    case "func_82142_c":
                    case "func_70665_d":
                    case "func_78328_b":
                    case "func_184429_b":
                    case "func_175598_ae": {
                        Core.LOGGER.warn("Insert return in method:" + mn.name);
                        return 1;
                    }
                }
            }
        }
        switch (mn.name) {
            case "transform":
            case "acceptOptions":
            case "injectIntoClassLoader":
            case "getLaunchTarget":
            case "getLaunchArguments":
            case "getASMTransformerClass":
            case "getAccessTransformerClass":
            case "getModContainerClass":
            case "getSetupClass":
            case "injectData": {
                Core.LOGGER.warn("Insert return in method:" + mn.name);
                return 1;
            }
        }
        return 0;
    }

    public static void FuckMethod(MethodNode mn) {
        if (mn.desc.equals("(Ljava/lang/String;Ljava/lang/String;[B)[B")) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(ALOAD, 3));
            mn.instructions.add(new InsnNode(ARETURN));
            mn.localVariables.removeIf(v -> v.index > 3);
            return;
        }
        Type type = Type.getReturnType(mn.desc);
        mn.instructions.clear();
        mn.localVariables.clear();
        switch (type.getSort()) {
            case Type.VOID: {
                mn.instructions.add(new InsnNode(RETURN));
                break;
            }
            case Type.SHORT:
            case Type.CHAR:
            case Type.BYTE:
            case Type.INT:
            case Type.BOOLEAN: {
                mn.instructions.add(new InsnNode(ICONST_0));
                mn.instructions.add(new InsnNode(IRETURN));
                break;
            }
            case Type.FLOAT: {
                mn.instructions.add(new InsnNode(FCONST_0));
                mn.instructions.add(new InsnNode(FRETURN));
                break;
            }
            case Type.LONG: {
                mn.instructions.add(new InsnNode(LCONST_0));
                mn.instructions.add(new InsnNode(LRETURN));
                break;
            }
            case Type.DOUBLE: {
                mn.instructions.add(new InsnNode(DCONST_0));
                mn.instructions.add(new InsnNode(DRETURN));
                break;
            }
            case Type.OBJECT: {
                mn.instructions.add(new InsnNode(ACONST_NULL));
                mn.instructions.add(new InsnNode(ARETURN));
                break;
            }
            default: {
                throw new IllegalStateException("The fuck?");
            }
        }
    }
}
