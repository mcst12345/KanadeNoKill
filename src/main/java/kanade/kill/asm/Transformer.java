package kanade.kill.asm;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kanade.kill.Empty;
import kanade.kill.Launch;
import kanade.kill.asm.injections.*;
import kanade.kill.classload.KanadeClassLoader;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.FieldInfo;
import kanade.kill.util.Util;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import scala.concurrent.util.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.reflect.Modifier.*;
import static kanade.kill.Launch.Debug;

@SuppressWarnings("SpellCheckingInspection")
public class Transformer implements IClassTransformer, Opcodes, ClassFileTransformer {
    private static final IClassNameTransformer classNameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(kanade.kill.Launch.classLoader, EarlyFields.renameTransformer_offset);
    public static final Transformer instance = new Transformer();
    private static final ObjectOpenHashSet<String> event_listeners = new ObjectOpenHashSet<>();
    private static final Queue<FieldInfo> fields = new LinkedBlockingQueue<>();
    private Transformer(){}

    public static final Set<String> Kanade = new HashSet<>();



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


    public static Set<String> getEventListeners() {
        return Collections.unmodifiableSet(event_listeners);
    }

    public static Queue getFields() {
        return fields;
    }

    private static final Set<String> modClasses = new HashSet<>();

    static {
        Kanade.add("kanade.kill.Core");
        Kanade.add("kanade.kill.reflection.EarlyMethods");
        Kanade.add("kanade.kill.reflection.ReflectionUtil");
        Kanade.add("kanade.kill.reflection.EarlyFields");
        Kanade.add("kanade.kill.asm.ASMUtil");
        Kanade.add("kanade.kill.asm.injections.DimensionManager");
        Kanade.add("kanade.kill.asm.injections.Entity");
        Kanade.add("kanade.kill.asm.injections.EntityLivingBase");
        Kanade.add("kanade.kill.asm.injections.EntityPlayer");
        Kanade.add("kanade.kill.asm.injections.FMLClientHandler");
        Kanade.add("kanade.kill.asm.injections.ForgeHooksClient");
        Kanade.add("kanade.kill.asm.injections.ItemStack");
        Kanade.add("kanade.kill.asm.injections.Minecraft");
        Kanade.add("kanade.kill.asm.injections.MinecraftForge");
        Kanade.add("kanade.kill.asm.injections.MinecraftServer");
        Kanade.add("kanade.kill.asm.injections.NonNullList");
        Kanade.add("kanade.kill.asm.injections.RenderGlobal");
        Kanade.add("kanade.kill.asm.injections.ServerCommandManager");
        Kanade.add("kanade.kill.asm.injections.World");
        Kanade.add("kanade.kill.asm.injections.WorldClient");
        Kanade.add("kanade.kill.asm.injections.WorldServer");
        Kanade.add("kanade.kill.asm.Transformer");
        Kanade.add("kanade.kill.util.TransformerList");
        Kanade.add("kanade.kill.thread.TransformersCheckThread");
        Kanade.add("kanade.kill.thread.ClassLoaderCheckThread");
        Kanade.add("kanade.kill.classload.KanadeClassLoader");
        Kanade.add("kanade.kill.util.FieldInfo");
        Kanade.add("kanade.kill.util.KanadeSecurityManager");
        Kanade.add("kanade.kill.thread.SecurityManagerCheckThread");
        Kanade.add("kanade.kill.thread.KillerThread");
        Kanade.add("kanade.kill.thread.GuiThread");
        Kanade.add("kanade.kill.AgentMain");
        Kanade.add("kanade.kill.Attach");
        Kanade.add("kanade.kill.ModMain");
        Kanade.add("kanade.kill.item.KillItem");
        Kanade.add("kanade.kill.item.DeathItem");
        Kanade.add("kanade.kill.reflection.LateFields");
        Kanade.add("kanade.kill.network.packets.KillAllEntities");
        Kanade.add("kanade.kill.network.NetworkHandler");
        Kanade.add("kanade.kill.command.KanadeKillCommand");
        Kanade.add("kanade.kill.ClientMain");
    }

    public static boolean isModClass(String s) {
        return modClasses.contains(s);
    }

    private static boolean Redirect(ClassNode cn, boolean goodClass, String transformedName) {
        if (Debug) {
            Launch.LOGGER.info("Examine class:" + cn.name);
        }
        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    switch (fin.name) {
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
                                fin.name = "motionY";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "motionY";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70159_w": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "motionX";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "motionX";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70179_y": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "motionZ";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "motionZ";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71424_I": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "profiler";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "profiler";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71460_t": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "entityRenderer";
                                changed = true;
                            } else if (goodClass) {
                                fin.name = "entityRenderer";
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
                        case "field_76645_j":
                        case "field_72996_f": {
                            if (fin.getOpcode() == GETFIELD) {
                                fin.name = "entities";
                                changed = true;
                            } else if (goodClass) {
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
                    }
                }
            }
            if (!goodClass) {
                Type type = Type.getReturnType(mn.desc);
                if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                    if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>") || Modifier.isAbstract(mn.access) || Modifier.isNative(mn.access))) {
                        ASMUtil.InsertReturn1(mn, type);
                        if (mn.name.startsWith("func_")) {
                            ASMUtil.InsertReturn2(mn, type);
                        }
                    }
                }
                if (mn.localVariables != null) {
                    for (LocalVariableNode lvn : mn.localVariables) {
                        if (lvn.desc.startsWith("net/minecraftforge/") && lvn.desc.contains("/event/")) {
                            kanade.kill.Launch.LOGGER.info("Find event listsner:" + transformedName);
                            if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                                ASMUtil.InsertReturn2(mn, type);
                            }
                            event_listeners.add(cn.name.replace('/', '.'));
                            break;
                        }
                    }
                }
                if (type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY) {
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            if (min.owner.startsWith("org/lwjgl") || min.owner.startsWith("net/minecraft/client/renderer")) {
                                Launch.LOGGER.info("Find render method.");
                                ASMUtil.InsertReturn3(mn, type);
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (Kanade.contains(name)) {
            try (InputStream is = Empty.class.getResourceAsStream('/' + name.replace('.', '/') + ".class")) {
                assert is != null;
                //6 lines below are from Apache common io.
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final byte[] buffer = new byte[8024];
                int n;
                while (-1 != (n = is.read(buffer))) {
                    output.write(buffer, 0, n);
                }
                return output.toByteArray();
            } catch (Throwable t) {
                return basicClass;
            }
        }
        if (basicClass == null) {
            return null;
        }
        byte[] originalBytes = null;
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
        ClassReader cr = new ClassReader(originalBytes != null ? originalBytes : basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        ClassNode changedClass = null;
        if (originalBytes != null && !(originalBytes == basicClass)) {
            cr = new ClassReader(originalBytes);
            changedClass = new ClassNode();
            cr.accept(changedClass, 0);
        }
        byte[] transformed;
        boolean changed, compute_all = false;
        boolean goodClass = true;
        if (name.equals(transformedName)) {
            if (Util.ModClass(name)) {
                goodClass = false;
                modClasses.add(name);
            }
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
            cn.methods.addAll(methods);
            final List<FieldNode> fields = new ArrayList<>(changedClass.fields);
            for (FieldNode fn1 : changedClass.fields) {
                for (FieldNode fn2 : cn.fields) {
                    if (fn1.name.equals(fn2.name) && fn1.desc.equals(fn2.desc)) {
                        fields.remove(fn1);
                    }
                }
            }
            cn.fields.addAll(fields);
        }

        changed = Redirect(cn, goodClass, transformedName);

        switch (transformedName) {
            case "net.minecraftforge.common.DimensionManager": {

                changed = true;
                kanade.kill.Launch.LOGGER.info("Get DimensionManager.");

                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("setWorld")) {
                        DimensionManager.InjectSetWorld(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraft.entity.Entity": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get Entity.");
                Entity.AddField(cn);
                break;
            }
            case "net.minecraft.server.MinecraftServer": {
                changed = true;
                compute_all = true;
                kanade.kill.Launch.LOGGER.info("Get MinecraftServer.");
                MinecraftServer.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "<init>": {
                            MinecraftServer.InjectConstructor(mn);
                            break;
                        }
                        case "func_71217_p": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
                            MinecraftServer.InjectTick(mn);
                            break;
                        }
                        case "run": {
                            MinecraftServer.InjectRun(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.client.Minecraft": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get Minecraft.");
                Minecraft.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_147108_a": {
                            Minecraft.InjectDisplayGuiScreen(mn);
                            break;
                        }
                        case "func_71411_J":
                        case "func_71407_l": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "net/minecraft/client/Minecraft", "dead", "Z"));
                            break;
                        }
                        case "func_71381_h":
                        case "func_71364_i": {
                            ASMUtil.clearMethod(mn);
                            break;
                        }
                        case "func_71384_a": {
                            Minecraft.InjectInit(mn);
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
                    }
                }
                Minecraft.AddMethod(cn);
                break;
            }
            case "net.minecraft.item.ItemStack": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get ItemStack.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("func_190926_b")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.NoRemove());
                        break;
                    }
                }
                ItemStack.AddMethod(cn);
                break;
            }
            case "net.minecraft.world.World": {
                changed = true;
                compute_all = true;
                kanade.kill.Launch.LOGGER.info("Get World.");
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
                            World.InjectUpdateEntities(mn);
                            break;
                        }
                    }
                }
                break;
            }
            case "net.minecraft.entity.EntityLivingBase": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get EntityLivingBase.");
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
                            ASMUtil.InsertReturn(mn, Type.FLOAT_TYPE, 20.0f, 0, ASMUtil.inList());
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
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayer": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get EntityPlayer.");
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
                    }
                }
                break;
            }
            case "net.minecraftforge.client.ForgeHooksClient": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get ForgeHooksClient.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("drawScreen")) {
                        ForgeHooksClient.InjectDrawScreen(mn);
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerSP": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get EntityPlayerSP.");
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
                kanade.kill.Launch.LOGGER.info("Get EntityPlayerMP.");
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
                kanade.kill.Launch.LOGGER.info("Get NonNullList.");
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
                kanade.kill.Launch.LOGGER.info("Get WorldServer.");
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
                        case "func_72847_b":
                        case "func_72960_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.inList());
                            break;
                        }
                        case "func_72923_a": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, 1, ASMUtil.isDead());
                            break;
                        }
                    }

                }
                break;
            }
            case "net.minecraft.world.WorldClient": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get WorldClient.");

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

                break;

            }
            case "net.minecraftforge.common.ForgeHooks": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get ForgeHooks.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("onLivingUpdate")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.RenderGlobal": {
                changed = true;
                kanade.kill.Launch.LOGGER.info("Get RenderGlobal");
                RenderGlobal.AddField(cn);
                break;
            }
            case "net.minecraft.client.renderer.entity.RenderManager": {
                changed = true;
                compute_all = true;
                kanade.kill.Launch.LOGGER.info("Get RenderManager");
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
                kanade.kill.Launch.LOGGER.info("Get MinecraftForge.");
                MinecraftForge.AddField(cn);
                break;
            }
            case "net.minecraftforge.fml.server.FMLServerHandler": {
                kanade.kill.Launch.LOGGER.info("Get FMLServerHandler.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("finishServerLoading")) {
                        for (int i = mn.instructions.size() - 1; i >= 0; i--) {
                            AbstractInsnNode ain = mn.instructions.get(i);
                            if (ain instanceof InsnNode) {
                                InsnNode in = (InsnNode) ain;
                                if (in.getOpcode() == RETURN) {
                                    mn.instructions.insert(mn.instructions.get(i - 1), new MethodInsnNode(INVOKESTATIC, "kanade/kill/util/Util", "save", "()V", false));
                                    break;
                                }
                            }
                        }
                        kanade.kill.Launch.LOGGER.info("Inject into " + mn.name);
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.client.FMLClientHandler": {
                kanade.kill.Launch.LOGGER.info("Get FMLClientHandler.");
                changed = true;
                FMLClientHandler.AddField(cn);
                break;
            }
            case "net.minecraft.command.ServerCommandManager": {
                kanade.kill.Launch.LOGGER.info("Get ServerCommandManager.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<init>")) {
                        ServerCommandManager.InjectConstructor(mn);
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.common.eventhandler.EventBus": {
                kanade.kill.Launch.LOGGER.info("Get EventBus.");
                changed = true;
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("post")) {
                        EventBus.InjectPost(mn);
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
            }
        }


        if (!goodClass) {
            for (FieldNode fn : cn.fields) {
                if (Modifier.isStatic(fn.access)) {
                    fields.add(new FieldInfo(cn, fn));
                }
            }

            if (kanade.kill.Launch.funny) {
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
            ClassWriter cw = new ClassWriter(compute_all ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            transformed = cw.toByteArray();
            return transformed;
        } else {
            return basicClass;
        }

    }

    @Override
    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) {
        s = s.replace('/', '.');
        return transform(s, classNameTransformer.remapClassName(s), bytes);
    }
}
