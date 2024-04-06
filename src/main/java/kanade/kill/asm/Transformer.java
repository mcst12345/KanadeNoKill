package kanade.kill.asm;

import kanade.kill.Empty;
import kanade.kill.Launch;
import kanade.kill.asm.injections.Timer;
import kanade.kill.asm.injections.*;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.util.ObjectUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static kanade.kill.Launch.Debug;

@SuppressWarnings("SpellCheckingInspection")
public class Transformer implements Opcodes {
    public static final Transformer instance = new Transformer();

    private Transformer(){}

    private static final Set<String> classes = new HashSet<>();
    public static boolean DEOBF_STARTED = false;

    static {
        classes.add("morph.avaritia.proxy.ProxyClient");
        classes.add("ic2.core.uu.LeanItemStack");
        classes.add("codechicken.lib.internal.proxy.ProxyClient");
    }

    static {
        File file = new File("transformedClasses");
        try {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            } else if (!file.isDirectory()) {
                Files.delete(file.toPath());
                Files.createDirectory(file.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean RedirectAndExamine(ClassNode cn, boolean goodClass, String transformedName) {
        if (Debug) {
            Launch.LOGGER.info("Examine class:" + cn.name + "  good:" + goodClass);
        }
        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            if (isAbstract(mn.access)) {
                continue;
            }
            if (isNative(mn.access) && !goodClass) {
                mn.access &= ~Opcodes.ACC_NATIVE;
                ASMUtil.FuckMethod(mn);
                continue;
            }
            boolean init = mn.name.equals("<init>") || mn.name.equals("<clinit>");
            ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
            boolean f = false;
            Type type = Type.getReturnType(mn.desc);
            boolean flag = type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY;
            if (mn.name.equals("<init>")) {
                AbstractInsnNode Return = null;
                for (AbstractInsnNode ain : mn.instructions.toArray()) {
                    if (ain instanceof InsnNode) {
                        InsnNode in = (InsnNode) ain;
                        if (in.getOpcode() == RETURN) {
                            Return = in;
                            break;
                        }
                    }
                }
                if (Return == null) {
                    Launch.LOGGER.warn("No return insn found in instructions. Inject at head.");
                }
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new LdcInsnNode(25L));
                list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NativeMethods", "SetTag", "(Ljava/lang/Object;J)V", false));
                if (Return != null) {
                    mn.instructions.insertBefore(Return, list);
                } else {
                    mn.instructions.insert(list);
                }
                mn.maxStack += 2;
                changed = true;
                if (Debug) {
                    Launch.LOGGER.info("Inject into constructor of " + cn.name);
                }
            }
            while (iterator.hasNext()) {
                AbstractInsnNode ain = iterator.next();
                if (!goodClass) {
                    if (ain instanceof TypeInsnNode) {
                        TypeInsnNode tin = (TypeInsnNode) ain;
                        if (tin.getOpcode() == NEW) {
                            InsnList list = new InsnList();
                            list.add(new InsnNode(DUP));
                            list.add(new LdcInsnNode(25L));
                            list.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NativeMethods", "SetTag", "(Ljava/lang/Object;J)V", false));
                            mn.instructions.insertBefore(tin.getNext(), list);
                            if (!f) {
                                mn.maxStack += 3;
                                f = true;
                            }
                            changed = true;
                            /*if(tin.desc.equals("javax/swing/JWindow")){
                                mn.maxStack++;
                                int index = mn.maxStack;
                                mn.instructions.insertBefore(tin.getNext(),new VarInsnNode(ASTORE,index));
                                InsnList list1 = new InsnList();
                                list1.add(new VarInsnNode(ALOAD,index));
                                list1.add(new MethodInsnNode(INVOKEVIRTUAL,"java.awt.Window","removeNotify","()V",false));

                            }*/
                        }
                    } else if (flag) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            if (min.name.equals("sleep") && min.owner.equals("java/lang/Thread")) {
                                iterator.set(new InsnNode(POP2));
                                changed = true;
                                continue;
                            }
                            if (min.owner.equals("sun/misc/Unsafe")) {
                                if (min.name.startsWith("put")) {
                                    MethodInsnNode fuck = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/UnsafeFucker", min.name + "Fuck", "(Ljava/lang/Object;" + min.desc.substring(1), false);
                                    iterator.set(fuck);
                                    changed = true;
                                } else if (min.name.startsWith("get") && !min.name.contains("Object")) {
                                    MethodInsnNode fuck = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/UnsafeFucker", min.name + "Fuck", "(Ljava/lang/Object;" + min.desc.substring(1), false);
                                    iterator.set(fuck);
                                    changed = true;
                                }
                                continue;
                            } else if (min.owner.equals("java.lang.reflect.Field")) {
                                if (min.name.equals("set")) {
                                    iterator.set(new InsnNode(POP2));
                                    iterator.add(new InsnNode(POP));//Why don't we have POP3?
                                    changed = true;
                                }
                            }
                            if ((min.name.equals("setContentPane") && min.desc.equals("(Ljava/awt/Container;)V")) || (min.name.equals("setAlwaysOnTop") && min.desc.equals("(Z)V")) || (min.name.equals("setVisible") && min.desc.equals("(Z)V")) || (min.owner.equals("java/lang/System") && min.name.equals("load")) || (min.owner.equals("org/lwjgl/opengl/Display") && min.name.equals("setFullscreen"))) {
                                mn.instructions.set(min, new InsnNode(POP));
                                changed = true;
                                continue;
                            }
                            if (!init && min.owner.startsWith("org/lwjgl") || min.owner.startsWith("net/minecraft/client/renderer") || min.owner.startsWith("sun/awt") || min.owner.startsWith("javax/swing") || min.owner.startsWith("org/eclipse/swt") || min.owner.startsWith("javafx/")) {
                                if(Debug){
                                    Launch.LOGGER.info("Find render method.");
                                }
                                ASMUtil.InsertReturn3(mn, type);
                                changed = true;
                            }
                        }
                    }
                }
                if (ain instanceof InsnNode) {
                    InsnNode in = (InsnNode) ain;
                    switch (in.getOpcode()) {
                        case IDIV: {
                            MethodInsnNode min = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NumberUtil", "checkDivisor", "(I)I", false);
                            mn.instructions.insertBefore(ain, min);
                            changed = true;
                            break;
                        }
                        case FDIV: {
                            MethodInsnNode min = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NumberUtil", "checkDivisor", "(F)F", false);
                            mn.instructions.insertBefore(ain, min);
                            changed = true;
                            break;
                        }
                        case LDIV: {
                            MethodInsnNode min = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NumberUtil", "checkDivisor", "(J)J", false);
                            mn.instructions.insertBefore(ain, min);
                            changed = true;
                            break;
                        }
                        case DDIV: {
                            MethodInsnNode min = new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NumberUtil", "checkDivisor", "(D)D", false);
                            mn.instructions.insertBefore(ain, min);
                            changed = true;
                            break;
                        }
                    }
                } else if (ain instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    switch (fin.name) {
                        case "field_146106_i": {
                            if (fin.getOpcode() == GETFIELD || goodClass) {
                                fin.name = "gameProfile";
                                changed = true;
                            }
                            break;
                        }
                        case "field_151002_e": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "ITEM";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "ITEM";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175618_aM": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "BlockRenderDispatcher";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "BlockRenderDispatcher";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71466_p": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "FontRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "FontRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71318_t": {
                            if (goodClass) {
                                fin.name = "PlayerList";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71305_c": {
                            if (goodClass) {
                                fin.name = "Worlds";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71464_q": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "StandardGalacticFontRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "StandardGalacticFontRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175620_Y": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "ItemRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "ItemRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71442_b": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "PlayerController";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "PlayerController";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71446_o": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "RenderEngine";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "RenderEngine";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71438_f": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "RenderGlobal";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "RenderGlobal";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175621_X": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "itemRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "itemRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70170_p": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "WORLD";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "WORLD";
                                changed = true;
                            }
                            break;
                        }
                        case "field_73010_i": {
                            if (goodClass) {
                                fin.name = "players";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70128_L": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "HatedByLife";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "HatedByLife";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70725_aQ": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "Death_Time";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "Death_Time";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70718_bc": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "RecentlyHIT";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "RecentlyHIT";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70165_t": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "X";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "X";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70163_u": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "Y";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "Y";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70161_v": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "Z";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "Z";
                                changed = true;
                            }
                            break;
                        }
                        case "EVENT_BUS": {
                            if (fin.owner.equals("net/minecraftforge/common/MinecraftForge")) {
                                if (fin.getOpcode() == GETSTATIC) {
                                    fin.name = "Event_bus";
                                    changed = true;
                                } else if (goodClass) {
                                    fin.name = "Event_bus";
                                    changed = true;
                                }
                            }
                            break;
                        }
                        case "eventBus": {
                            if (fin.owner.equals("net/minecraftforge/fml/common/FMLCommonHandler")) {
                                if (fin.getOpcode() == GETFIELD) {
                                    fin.name = "EventBus";
                                    changed = true;
                                } else if (goodClass) {
                                    fin.name = "EventBus";
                                    changed = true;
                                }
                            }
                            break;
                        }
                        case "dimensions": {
                            if (fin.owner.equals("net/minecraftforge/common/DimensionManager")) {
                                changed = true;
                                if (goodClass) {
                                    fin.name = "Dimensions";
                                } else {
                                    fin.owner = "kanade/kill/fake/FakeObjects";
                                }
                            }
                            break;
                        }
                        case "unloadQueue": {
                            if (fin.owner.equals("net/minecraftforge/common/DimensionManager")) {
                                changed = true;
                                if (goodClass) {
                                    fin.name = "UnloadQueue";
                                } else {
                                    fin.owner = "kanade/kill/fake/FakeObjects";
                                }
                            }
                            break;
                        }
                        case "worlds": {
                            if (fin.owner.equals("net/minecraftforge/common/DimensionManager")) {
                                changed = true;
                                if (goodClass) {
                                    fin.name = "Worlds";
                                } else {
                                    fin.owner = "kanade/kill/fake/FakeObjects";
                                }
                            }
                            break;
                        }
                        case "field_73032_d": {
                            if (fin.getOpcode() == GETFIELD || goodClass) {
                                fin.name = "EntityList";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71439_g": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "PLAYER";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "PLAYER";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70181_x": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "mY";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "mY";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70159_w": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "mX";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "mX";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70179_y": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "mZ";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "mZ";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71304_b":
                        case "field_72984_F":
                        case "field_71424_I": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "Profiler";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "Profiler";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71460_t": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "EntityRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "EntityRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_73007_j": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "weathers";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "weathers";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70180_af": {
                            if (fin.getOpcode() == GETFIELD || goodClass) {
                                fin.name = "DataManager";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175616_W":
                        case "field_175010_j": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "renderManager";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "renderManager";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71456_v": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "IngameGUI";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "IngameGUI";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71462_r": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "CurrentScreen";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "CurrentScreen";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71417_B": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "mouseHelper";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "mouseHelper";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71441_e": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "WORLD";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "WORLD";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71071_by": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "Inventory";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "Inventory";
                                changed = true;
                            }
                            break;
                        }
                        case "field_78729_o": {
                            if (goodClass) {
                                fin.name = "EntityRenderMap";
                                changed = true;
                            }
                            break;
                        }
                        case "field_72997_g": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "unloads";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "unloads";
                                changed = true;
                            }
                            break;
                        }
                        case "field_76645_j":
                        case "field_72996_f": {
                            if (fin.getOpcode() == GETFIELD || goodClass) {
                                fin.name = "entities";
                                changed = true;
                            }
                            break;
                        }
                        case "INSTANCE": {
                            if (fin.owner.equals("net/minecraftforge/fml/client/FMLClientHandler")) {
                                if (fin.getOpcode() == GETSTATIC) {
                                    fin.name = "instance";
                                    changed = true;
                                } else if (goodClass) {
                                    fin.name = "instance";
                                    changed = true;
                                }
                                break;
                            }
                        }
                        if (fin.owner.equals("net/minecraft/launchwrapper/Launch")) {
                            if (fin.getOpcode() == GETSTATIC) {
                                fin.owner = "kanade/kill/Launch";
                                changed = true;
                            } else if (goodClass) {
                                fin.owner = "kanade/kill/Launch";
                                changed = true;
                            }
                        }
                    }
                } else if (ain instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (min.owner.equals("net/minecraft/launchwrapper/Launch")) {
                        min.owner = "kanade/kill/Launch";
                        changed = true;
                    }
                    switch (min.name) {
                        case "func_71381_h": {
                            if (goodClass) {
                                min.name = "SetIngameFocus";
                                changed = true;
                            }
                            break;
                        }
                        case "func_71364_i": {
                            if (goodClass) {
                                min.name = "SetIngameNotInFocus";
                                changed = true;
                            }
                            break;
                        }
                        case "func_152343_a": {
                            if (goodClass) {
                                min.name = "AddTask";
                                changed = true;
                            }
                            break;
                        }
                        case "func_74506_a": {
                            if (goodClass) {
                                min.name = "UnPressAllKeys";
                            }
                            changed = true;
                            break;
                        }
                        case "setGrabbed": {
                            if (!min.owner.equals("org/lwjgl/input/Mouse")) {
                                break;
                            }
                            if (!goodClass) {
                                iterator.set(new InsnNode(POP));
                                changed = true;
                            }
                            break;
                        }
                        case "defineClass": {
                            if (min.desc.equals("(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;")) {
                                min.setOpcode(INVOKESTATIC);
                                min.owner = "kanade/kill/classload/KanadeClassLoader";
                                min.desc = "(Ljava/lang/Object;Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;";
                            }
                            break;
                        }
                        case "defineAnonymousClass": {
                            if (min.desc.equals("(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;")) {
                                min.setOpcode(INVOKESTATIC);
                                min.owner = "kanade/kill/classload/KanadeClassLoader";
                                min.desc = "(Ljava/lang/Object;Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;";
                            }
                            break;
                        }
                    }
                }
            }
            if (!goodClass) {
                if (mn.name.equals("func_76986_a") || (mn.desc.endsWith("V") && mn.name.toLowerCase().contains("render") && mn.desc.startsWith("(L"))) {
                    changed = true;
                    ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.isDead());
                }

                if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                    if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>") || isAbstract(mn.access) || isNative(mn.access))) {
                        ASMUtil.InsertReturn1(mn, type);
                        changed = true;
                        if (mn.name.startsWith("func_")) {
                            ASMUtil.InsertReturn2(mn, type);
                        }
                    }
                }
                if (mn.localVariables != null) {
                    for (LocalVariableNode lvn : mn.localVariables) {
                        if (lvn.desc.startsWith("Lnet/minecraftforge/") && lvn.desc.contains("/event/")) {
                            if(Debug){
                                Launch.LOGGER.info("Find event listsner:" + transformedName);
                            }
                            if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY && !(mn.name.equals("<init>") || mn.name.equals("<clinit>"))) {
                                ASMUtil.InsertReturn2(mn, type);
                                changed = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (mn.name.equals("func_82738_a") || mn.name.equals("func_82572_b") || mn.name.equals("func_147456_g") || mn.name.equals("func_70071_h_") || mn.name.equals("func_73660_a") || mn.name.equals("func_73831_a") || mn.name.equals("func_110550_d")) {
                InsnList list = new InsnList();
                LabelNode label0 = new LabelNode();
                list.add(ASMUtil.isTimeStop());
                list.add(new JumpInsnNode(IFEQ, label0));
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(ASMUtil.inList());
                list.add(new JumpInsnNode(IFNE, label0));
                list.add(new InsnNode(RETURN));
                list.add(label0);
                list.add(new FrameNode(F_SAME, 0, null, 0, null));
                mn.instructions.insert(list);
                changed = true;
            }
            if (mn.name.equals("func_70030_z") || mn.name.equals("func_70071_h_") || mn.name.equals("func_70636_d")) {
                changed = true;
                ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.isDead());
            }
            if (!goodClass && mn.name.equals("func_73863_a")) {
                InsnList list = new InsnList();
                LabelNode label0 = new LabelNode();
                list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "field_71432_P", "Lnet/minecraft/client/Minecraft;"));
                list.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/Minecraft", "PLAYER", "Lnet/minecraft/client/entity/EntityPlayerSP;"));
                list.add(ASMUtil.inList());
                list.add(new JumpInsnNode(IFEQ, label0));
                list.add(new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "guiProtect", "Z"));
                list.add(new JumpInsnNode(IFEQ, label0));
                list.add(new InsnNode(RETURN));
                list.add(label0);
                list.add(new FrameNode(F_SAME, 0, null, 0, null));
                mn.instructions.insert(list);
                changed = true;
            }
            if (mn.name.equals("func_78088_a")) {
                changed = true;
                ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
            }
            if (mn.name.equals("func_177071_a")) {
                changed = true;
                ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.isDead());
            }
            if (!init && !isStatic(mn.access) && !goodClass && mn.desc.endsWith("V")) {
                changed = true;
                ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/NativeMethods", "HaveDeadTag", "(Ljava/lang/Object;)Z", false));
            }

        }
        return changed;
    }

    public byte[] transform(String name, String transformedName, byte[] basicClass, byte[] unchanged) {
        if (!DEOBF_STARTED && !transformedName.equals(name)) {
            DEOBF_STARTED = true;
        }
        if (transformedName.equals("net.minecraft.item.ItemStackLoader")) {
            ClassNode cn = new ClassNode();
            cn.version = V1_8;
            cn.access = ACC_PUBLIC | ACC_SUPER;
            cn.name = "net/minecraft/item/ItemStackLoader";
            cn.superName = "java/lang/Object";
            cn.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, "NOITEM", "Z", null, null));
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        }
        if (basicClass == null) {
            return null;
        }
        boolean changed = false;
        int compute = ClassWriter.COMPUTE_MAXS;
        ;
        boolean goodClass = true;
        byte[] originalBytes = null;
        if (classes.contains(transformedName)) {
            changed = true;
            InputStream is = Empty.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            assert is != null;
            basicClass = Launch.readFully(is);
        } else if (name.startsWith("kanade.kill.")) {
            InputStream is = Empty.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            assert is != null;
            return Launch.readFully(is);
        } else {
            if(unchanged == null){
                try {
                    originalBytes = Launch.classLoader.getClassBytes(name);
                    for (IClassTransformer t : KanadeClassLoader.NecessaryTransformers) {
                        try {
                            originalBytes = t.transform(name, transformedName, originalBytes);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (IOException ignored) {
                }
            } else {
                if(unchanged.length != 0){
                    originalBytes = Arrays.copyOf(unchanged,unchanged.length);
                } else {
                    originalBytes = Arrays.copyOf(basicClass,basicClass.length);
                }
            }
        }


        ClassReader cr = new ClassReader(originalBytes != null ? originalBytes : basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        ClassNode changedClass = null;
        if (unchanged != null && Launch.betterCompatible && unchanged.length != 0 && originalBytes != null && !(Arrays.equals(originalBytes, basicClass))) {
            cr = new ClassReader(basicClass);
            changedClass = new ClassNode();
            cr.accept(changedClass, 0);
        }
        byte[] transformed;

        if (!name.isEmpty()) {
            if (name.equals(transformedName)) {
                if (ObjectUtil.ModClass(name)) {
                    goodClass = false;
                }
            }
        } else {
            goodClass = false;
        }

        if (changedClass != null) {
            final List<MethodNode> methods = new ArrayList<>(changedClass.methods);
            for (MethodNode mn1 : changedClass.methods) {
                for (MethodNode mn2 : cn.methods) {
                    if (mn1.name.equals(mn2.name) && mn1.desc.equals(mn2.desc)) {
                        methods.remove(mn1);
                    }
                }
            }
            if (!methods.isEmpty()) {
                changed = true;
                if(Debug){
                    Launch.LOGGER.info("Adding methods to class:"+ Arrays.toString(methods.toArray()));
                }
                cn.methods.addAll(methods);
            }
            final List<FieldNode> fields = new ArrayList<>(changedClass.fields);
            for (FieldNode fn1 : changedClass.fields) {
                for (FieldNode fn2 : cn.fields) {
                    if (fn1.name.equals(fn2.name) && fn1.desc.equals(fn2.desc)) {
                        fields.remove(fn1);
                    }
                }
            }
            if (!fields.isEmpty()) {
                changed = true;
                cn.fields.addAll(fields);
                if(Debug){
                    Launch.LOGGER.info("Adding fields to class:" + Arrays.toString(fields.toArray()));
                }
            }
            final List<String> interfaces = new ArrayList<>(changedClass.interfaces);
            for (String i1 : changedClass.interfaces) {
                for (String i2 : cn.interfaces) {
                    if (i1.equals(i2)) {
                        interfaces.remove(i1);
                    }
                }
            }
            if (!interfaces.isEmpty()) {
                changed = true;
                cn.interfaces.addAll(interfaces);
                if(Debug){
                    Launch.LOGGER.info("Adding interfaces to class:" + Arrays.toString(interfaces.toArray()));
                }
            }
        }

        changed = changed || RedirectAndExamine(cn, goodClass, transformedName);

        switch (transformedName) {
            case "net.minecraftforge.fml.common.ModClassLoader": {
                changed = true;
                Launch.LOGGER.info("Get ModClassLoader.");
                for(MethodNode mn : cn.methods){
                    if(mn.name.equals("addModAPITransformer")){
                        ModClassLoader.OverwriteAddModAPITransformer(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.util.ClassInheritanceMultiMap": {
                Launch.LOGGER.info("Get ClassInheritanceMultiMap.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "add": {
                            if (mn.desc.equals("(Ljava/lang/Object;)Z")) {
                                ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.isDead());
                            }
                            break;
                        }
                        case "func_181743_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_180215_b": {
                            ClassInheritanceMultiMap.InjectGetByClass(mn);
                            break;
                        }
                        case "remove": {
                            if (mn.desc.equals("(Ljava/lang/Object;)Z")) {
                                ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.inList());
                            }
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.FMLCommonHandler": {
                Launch.LOGGER.info("Get FMLCommonHandler.");
                changed = true;
                FMLCommonHandler.AddFields(cn);
                break;
            }
            case "net.minecraft.client.gui.GuiMainMenu": {
                Launch.LOGGER.info("Get GuiMainMenu.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_73866_w_": {
                            GuiMainMenu.OverwriteInitGui(mn);
                            break;
                        }
                        case "func_73863_a": {
                            GuiMainMenu.OverwriteDrawScreen(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.EntityTracker": {
                Launch.LOGGER.info("Get EntityTracker.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_72788_a")) {
                        EntityTracker.OverwriteTick(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.client.tutorial.Tutorial": {
                Launch.LOGGER.info("Get Tutorial.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_193303_d")) {
                        ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, ASMUtil.isTimeStop());
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.RenderItem": {
                Launch.LOGGER.info("Get RenderItem.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_184394_a")) {
                        RenderItem.OverwriteRenderItemModel(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper": {
                Launch.LOGGER.info("Get SimpleChannelHandlerWrapper.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("channelRead0")) {
                        SimpleChannelHandlerWrapper.OverwriteChannelRead0(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.client.settings.KeyBinding": {
                Launch.LOGGER.info("Get KeyBinding.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_74506_a")) {
                        mn.name = "UnPressAllKeys";
                    }
                }
                KeyBinding.AddMethod(cn);
                break;
            }
            case "net.minecraft.client.audio.SoundManager$SoundSystemStarterThread": {
                changed = true;
                Launch.LOGGER.info("Get SoundManager$SoundSystemStarterThread.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("playing")) {
                        mn.instructions.clear();
                        mn.tryCatchBlocks.clear();
                        mn.instructions.add(new VarInsnNode(ALOAD, 0));
                        mn.instructions.add(new VarInsnNode(ALOAD, 1));
                        mn.instructions.add(new MethodInsnNode(INVOKESTATIC, "kanade/kill/asm/hooks/SoundSystemStarterThread", "playing", "(Lpaulscode/sound/SoundSystem;Ljava/lang/String;)Z", false));
                        mn.instructions.add(new InsnNode(IRETURN));
                    }
                }
                break;
            }
            case "net.minecraft.util.MouseHelper": {
                changed = true;
                Launch.LOGGER.info("Get MouseHelper");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_74372_a": {
                            MouseHelper.OverwriteGrabMouseCursor(mn);
                            break;
                        }
                        case "func_74373_b": {
                            MouseHelper.OverwriteUngrabMouseCursor(mn);
                            break;
                        }
                        case "func_74374_c": {
                            MouseHelper.OverwriteMouseXYChange(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.common.DimensionManager": {

                changed = true;
                Launch.LOGGER.info("Get DimensionManager.");

                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("setWorld")) {
                        DimensionManager.InjectSetWorld(mn);
                        break;
                    }
                }

                DimensionManager.AddField(cn);
                break;
            }
            case "net.minecraft.entity.Entity": {
                changed = true;
                Launch.LOGGER.info("Get Entity.");
                Entity.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_189511_e": {
                            Entity.OverwriteWriteToNBT(mn);
                            break;
                        }
                        case "func_70020_e": {
                            Entity.OverwriteReadFromNBT(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.server.MinecraftServer": {
                changed = true;
                compute = ClassWriter.COMPUTE_FRAMES;
                Launch.LOGGER.info("Get MinecraftServer.");
                MinecraftServer.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "<init>": {
                            MinecraftServer.InjectConstructor(mn);
                            break;
                        }
                        case "func_71217_p": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
                            MinecraftServer.OverwriteTick(mn);
                            break;
                        }
                        case "run": {
                            MinecraftServer.OverwriteRun(mn);
                            break;
                        }
                        case "func_184105_a": {
                            MinecraftServer.OverwriteSetPlayerList(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.FMLModContainer": {
                Launch.LOGGER.info("Get FMLModContainer");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("handleModStateEvent")) {
                        FMLModContainer.OverwriteHandleModStateEvent(mn);
                    }
                }
                break;
            }
            case "net.minecraft.client.Minecraft": {
                changed = true;
                Launch.LOGGER.info("Get Minecraft.");
                Minecraft.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_147108_a": {
                            Minecraft.InjectDisplayGuiScreen(mn);
                            break;
                        }
                        case "func_71411_J": {
                            Minecraft.OverwriteRunGameLoop(mn);
                            break;
                        }
                        case "func_184118_az":
                        case "func_71407_l": {
                            if (mn.name.equals("func_184118_az")) {
                                Minecraft.InjectRunTickKeyboard(mn);
                            }
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "dead", "Z"));
                            break;
                        }
                        case "func_71381_h":
                        case "func_71364_i": {
                            ASMUtil.clearMethod(mn);
                            break;
                        }
                        case "func_99999_d": {
                            Minecraft.InjectRun(mn);
                            break;
                        }
                        case "func_152343_a": {
                            mn.name = "AddTask";
                            break;
                        }
                        case "func_147116_af": {
                            Minecraft.OverwriteClickMouse(mn);
                            break;
                        }
                        case "func_147121_ag": {
                            Minecraft.OverwriteRightClickMouse(mn);
                            break;
                        }
                        case "func_71386_F": {
                            Minecraft.OverwriteGetSystemTime(mn);
                            break;
                        }
                    }
                }
                Minecraft.AddMethod(cn);
                break;
            }
            case "net.minecraft.item.ItemStack": {
                changed = true;
                Launch.LOGGER.info("Get ItemStack.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_190926_b": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.NoRemove());
                            break;
                        }
                        case "func_82840_a": {
                            ItemStack.OverwriteGetTooltip(mn);
                            break;
                        }
                        case "func_77945_a": {
                            ItemStack.OverwriteUpdateAnimation(mn);
                            break;
                        }
                        case "func_77957_a": {
                            ItemStack.OverwriteUseItemRightClick(mn);
                        }
                    }
                }
                ItemStack.AddFields(cn);
                ItemStack.AddMethods(cn);
                break;
            }
            case "net.minecraft.world.World": {
                changed = true;
                compute = ClassWriter.COMPUTE_FRAMES;
                Launch.LOGGER.info("Get World.");
                World.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "<init>": {
                            World.InjectConstructor(mn);
                            break;
                        }
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_72973_f":
                        case "func_72900_e":
                        case "func_72847_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "func_175650_b": {
                            World.OverwriteLoadEntities(mn);
                            break;
                        }
                        case "func_175681_c": {
                            World.OverwriteUnloadEntities(mn);
                            break;
                        }
                        case "func_72907_a": {
                            World.InjectCountEntities1(mn);
                            break;
                        }
                        case "countEntities": {
                            World.InjectCountEntities2(mn);
                            break;
                        }
                        case "func_72939_s": {
                            World.OverwriteUpdateEntities(mn);
                            break;
                        }
                        case "func_72866_a": {
                            World.InjectUpdateEntityWithOptionalForce(mn);
                            break;
                        }
                        case "func_72877_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, ASMUtil.isTimeStop());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.EntityLivingBase": {
                changed = true;
                Launch.LOGGER.info("Get EntityLivingBase.");
                EntityLivingBase.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70665_d":
                        case "func_70653_a":
                        case "func_70645_a":
                        case "func_70606_j": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_110138_aP":
                        case "func_110143_aJ": {
                            ASMUtil.InsertReturn(mn, Type.FLOAT_TYPE, new Float(20.0f), 0, ASMUtil.inList());
                            break;
                        }
                        case "func_70097_a": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_70089_S": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.TRUE, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_70679_bo": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, ASMUtil.isTimeStop());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.entity.RenderLivingBase": {
                Launch.LOGGER.info("Get RenderLivingBase.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_76986_a") && mn.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")) {
                        RenderLivingBase.InjectDoRenderHead(mn);
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.entity.RenderPlayer": {
                Launch.LOGGER.info("Get RenderPlayer.");
                changed = true;
                for(MethodNode mn : cn.methods){
                    if(mn.name.equals("<init>") && mn.desc.equals("(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V")){
                        RenderPlayer.InjectConstructor(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayer": {
                changed = true;
                Launch.LOGGER.info("Get EntityPlayer.");
                EntityPlayer.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70106_y":
                        case "func_70665_d":
                        case "func_70645_a": {

                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_70097_a": {

                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_71019_a":
                        case "func_71040_bB":
                        case "func_145779_a": {

                            ASMUtil.InsertReturn(mn, null, null, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_70071_h_": {
                            EntityPlayer.InjectOnUpdate(mn);
                            break;
                        }
                        case "<init>": {
                            EntityPlayer.InjectConstructor(mn);
                            break;
                        }
                        case "func_70108_f": {
                            EntityPlayer.InjectApplyEntityCollision(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.client.ForgeHooksClient": {
                changed = true;
                Launch.LOGGER.info("Get ForgeHooksClient.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("drawScreen")) {
                        ForgeHooksClient.InjectDrawScreen(mn);
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerSP": {
                changed = true;
                Launch.LOGGER.info("Get EntityPlayerSP.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70097_a": {

                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_71040_bB": {

                            ASMUtil.InsertReturn(mn, null, null, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_71150_b":
                        case "func_70665_d": {

                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.inList());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerMP": {
                changed = true;
                Launch.LOGGER.info("Get EntityPlayerMP.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_70645_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 0, ASMUtil.inList());
                            break;
                        }
                        case "func_152339_d": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "func_70097_a": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.util.NonNullList": {
                changed = true;
                Launch.LOGGER.info("Get NonNullList.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "remove": {
                            NonNullList.InjectRemove(mn);
                            break;
                        }
                        case "clear": {
                            NonNullList.OverwriteClear(mn);
                            break;
                        }
                        case "set": {
                            NonNullList.OverwriteSet(mn);
                            break;
                        }
                    }
                }
                NonNullList.AddMethod(cn);
                break;
            }
            case "net.minecraft.world.WorldServer": {
                changed = true;
                Launch.LOGGER.info("Get WorldServer.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_175650_b": {
                            WorldServer.OverwriteLoadEntities(mn);
                            break;
                        }
                        case "func_72847_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_72960_a": {
                            WorldServer.OverwriteSetEntityState(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.client.multiplayer.WorldClient": {
                changed = true;
                Launch.LOGGER.info("Get WorldClient.");

                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72838_d": {
                            ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_72847_b":
                        case "func_72900_e": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_73027_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 2, ASMUtil.isDead());
                            break;
                        }
                        case "func_73028_b": {
                            WorldClient.OverwriteRemoveEntityFromWorld(mn);
                            break;
                        }
                        case "func_73022_a": {
                            WorldClient.InjectRemoveAllEntities(mn);
                            break;
                        }
                    }
                }
                WorldClient.AddField(cn);
                break;

            }
            case "net.minecraftforge.common.ForgeHooks": {
                changed = true;
                Launch.LOGGER.info("Get ForgeHooks.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("onLivingUpdate")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.RenderGlobal": {
                changed = true;
                Launch.LOGGER.info("Get RenderGlobal");
                RenderGlobal.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_72726_b": {
                            ASMUtil.InsertReturn(mn, null, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "disableParticle", "Z"));
                            break;
                        }
                        case "func_72734_e": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/Config", "disableParticle", "Z"));
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.entity.RenderManager": {
                changed = true;
                compute = ClassWriter.COMPUTE_FRAMES;
                Launch.LOGGER.info("Get RenderManager");
                RenderManager.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_188391_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "<init>": {
                            RenderManager.InjectConstructor(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.common.MinecraftForge": {
                changed = true;
                Launch.LOGGER.info("Get MinecraftForge.");
                MinecraftForge.AddField(cn);
                break;
            }
            case "net.minecraftforge.fml.client.FMLClientHandler": {
                Launch.LOGGER.info("Get FMLClientHandler.");
                changed = true;
                FMLClientHandler.AddField(cn);
                break;
            }
            case "net.minecraft.command.ServerCommandManager": {
                Launch.LOGGER.info("Get ServerCommandManager.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<init>")) {
                        ServerCommandManager.InjectConstructor(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.eventhandler.EventBus": {
                Launch.LOGGER.info("Get EventBus.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("post")) {
                        EventBus.OverwritePost(mn);
                        break;
                    } else if (mn.name.equals("register") && mn.desc.equals("(Ljava/lang/Object;)V")) {
                        EventBus.OverwriteRegister(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.world.chunk.Chunk": {
                Launch.LOGGER.info("Get Chunk.");
                changed = true;
                Chunk.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_76612_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                        case "func_76608_a":
                        case "func_76622_b": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "<init>": {
                            Chunk.InjectConstructor(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.util.Timer": {
                Launch.LOGGER.info("Get Timer.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_74275_a")) {
                        Timer.OverwriteUpdateTimer(mn);
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.EntityRenderer": {
                Launch.LOGGER.info("Get EntityRenderer.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_181560_a": {
                            EntityRenderer.InjectUpdateCameraAndRender(mn);
                            break;
                        }
                        case "func_78464_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, ASMUtil.isTimeStop());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.client.particle.ParticleManager": {
                Launch.LOGGER.info("Get ParticleManager.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_178927_a": {
                            ASMUtil.InsertReturn(mn, null, null, -1, ASMUtil.isTimeStop());
                            break;
                        }
                        case "func_78868_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, ASMUtil.isTimeStop());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.network.NetHandlerPlayServer": {
                changed = true;
                Launch.LOGGER.info("Get NetHandlerPlayServer.");
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_194028_b": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/network/NetHandlerPlayServer", "field_147369_b", "Lnet/minecraft/entity/player/EntityPlayerMP;"));
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, list, ASMUtil.inList());
                            break;
                        }
                        case "func_184341_b": {
                            NetHandlerPlayServer.OverwriteIsMoveVehiclePacketInvalid(mn);
                            break;
                        }
                        case "func_183006_b": {
                            NetHandlerPlayServer.OverwriteIsMovePlayerPacketInvalid(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.InventoryPlayer": {
                Launch.LOGGER.info("Get InventoryPlayer.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_146027_a": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/player/InventoryPlayer", "field_70458_d", "Lnet/minecraft/entity/player/EntityPlayer;"));
                            ASMUtil.InsertReturn(mn, Type.INT_TYPE, 0, list, ASMUtil.inList());
                            break;
                        }
                        case "func_70436_m": {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/player/InventoryPlayer", "field_70458_d", "Lnet/minecraft/entity/player/EntityPlayer;"));
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, list, ASMUtil.inList());
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.eventhandler.Event": {
                Launch.LOGGER.info("Get Event.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("setCanceled")) {
                        Event.OverwriteSetCanceled(mn);
                        break;
                    }
                }
                break;
            }
            default: {
                compute = 0;
            }
        }


        if (!goodClass) {
            if (Launch.funny) {
                Iterator<MethodNode> iterator = cn.methods.listIterator();
                while (iterator.hasNext()) {
                    MethodNode mn = iterator.next();
                    int scan = ASMUtil.BadMethod(mn);
                    if (scan == 1) {
                        ASMUtil.FuckMethod(mn);
                        changed = true;
                    }
                    if (scan == 2) {
                        iterator.remove();
                        changed = true;
                    }
                }
            }
        }

        if (!isInterface(cn.access)) {
            if (isPrivate(cn.access)) {
                cn.access &= ~Opcodes.ACC_PRIVATE;
                cn.access |= Opcodes.ACC_PUBLIC;
                changed = true;
            }
            if (isProtected(cn.access)) {
                cn.access &= ~Opcodes.ACC_PROTECTED;
                cn.access |= Opcodes.ACC_PUBLIC;
                changed = true;
            }
            if (isFinal(cn.access)) {
                cn.access &= ~Opcodes.ACC_FINAL;
                changed = true;
            }
            if (!isPublic(cn.access)) {
                cn.access |= Opcodes.ACC_PUBLIC;
                changed = true;
            }
            for (FieldNode fn : cn.fields) {
                if (fn.name.equals("$VALUES")) continue;

                if (isPrivate(fn.access)) {
                    fn.access &= ~Opcodes.ACC_PRIVATE;
                    fn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
                if (isProtected(fn.access)) {
                    fn.access &= ~Opcodes.ACC_PROTECTED;
                    fn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
                if (isFinal(fn.access)) {
                    fn.access &= ~Opcodes.ACC_FINAL;
                    changed = true;
                }
                if (!isPublic(fn.access)) {
                    fn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
            }

            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("<clinit>")) continue;

                if (isPrivate(mn.access)) {
                    mn.access &= ~Opcodes.ACC_PRIVATE;
                    mn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
                if (isProtected(mn.access)) {
                    mn.access &= ~Opcodes.ACC_PROTECTED;
                    mn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
                if (isFinal(mn.access)) {
                    mn.access &= ~Opcodes.ACC_FINAL;
                    changed = true;
                }
                if (!isPublic(mn.access)) {
                    mn.access |= Opcodes.ACC_PUBLIC;
                    changed = true;
                }
            }
        }


        if (changed) {
            ClassWriter cw = new ClassWriter(compute);
            cn.accept(cw);
            transformed = cw.toByteArray();
            return transformed;
        } else {
            return basicClass;
        }

    }
}
