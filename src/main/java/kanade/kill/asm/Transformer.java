package kanade.kill.asm;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kanade.kill.Core;
import kanade.kill.Empty;
import kanade.kill.asm.injections.*;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.FieldInfo;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import scala.concurrent.util.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("SpellCheckingInspection")
public class Transformer implements IClassTransformer, Opcodes, ClassFileTransformer {
    private static final IClassNameTransformer classNameTransformer = (IClassNameTransformer) Unsafe.instance.getObjectVolatile(Launch.classLoader, EarlyFields.renameTransformer_offset);
    public static final boolean debug = System.getProperty("Debug") != null;
    public static final Transformer instance = new Transformer();
    private static final ObjectOpenHashSet<String> event_listeners = new ObjectOpenHashSet<>();
    private static final ObjectArrayList<FieldInfo> fields = new ObjectArrayList<>();
    private static final Set<String> transformedClasses = new HashSet<>();
    private Transformer(){}

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
    private static void save(byte[] clazz, String file) {
        if (!debug) {
            return;
        }
        try {
            Files.write(new File("transformedClasses" + File.separator + file + ".class").toPath(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<String> getEventListeners() {
        return Collections.unmodifiableSet(event_listeners);
    }

    public static List<FieldInfo> getFields() {
        return Collections.unmodifiableList(fields);
    }
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedClasses.contains(transformedName)){
            Core.LOGGER.info("Class " + transformedName + " is already transformed.");
            return basicClass;
        }
        if (name.equals("kanade.kill.ModMain")) {
            try {
                InputStream is = Empty.class.getResourceAsStream("/kanade/kill/ModMain.class");
                assert is != null;
                byte[] clazz = new byte[is.available()];
                is.read(clazz);
                is.close();
                return clazz;
            } catch (Throwable t) {
                return basicClass;
            }
        }
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        byte[] transformed;
        boolean changed = false, compute_all = false;
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

                if (path.startsWith("mods", path.lastIndexOf(File.separator) - 4)) {
                    goodClass = false;
                }
            } else {
                goodClass = false;
            }
        }
        switch (transformedName) {
            case "net.minecraftforge.common.DimensionManager": {
                changed = true;
                Core.LOGGER.info("Get DimensionManager.");

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
                Core.LOGGER.info("Get Entity.");
                Entity.AddField(cn);
                break;
            }
            case "net.minecraft.server.MinecraftServer": {
                changed = true;
                compute_all = true;
                Core.LOGGER.info("Get MinecraftServer.");
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
                Core.LOGGER.info("Get Minecraft.");
                Minecraft.AddField(cn);
                Minecraft.AddMethod(cn);
                for (MethodNode mn : cn.methods) {
                    switch (mn.name) {
                        case "func_147108_a": {
                            Minecraft.InjectDisplayGuiScreen(mn);
                            break;
                        }
                        case "func_71407_l": {
                            ASMUtil.InsertReturn(mn, Type.VOID_TYPE, null, -1, new FieldInsnNode(GETSTATIC, "kanade/kill/util/Util", "killing", "Z"));
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
                    }
                }
                break;
            }
            case "net.minecraft.item.ItemStack": {
                changed = true;
                Core.LOGGER.info("Get ItemStack.");
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
                Core.LOGGER.info("Get World.");
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
                Core.LOGGER.info("Get EntityLivingBase.");
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
                Core.LOGGER.info("Get EntityPlayer.");
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
                    }
                }
                break;
            }
            case "net.minecraft.entity.player.EntityPlayerSP": {
                changed = true;
                Core.LOGGER.info("Get EntityPlayerSP.");
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
                Core.LOGGER.info("Get EntityPlayerMP.");
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
                Core.LOGGER.info("Get NonNullList.");
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
                Core.LOGGER.info("Get WorldServer.");
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
                Core.LOGGER.info("Get WorldClient.");

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
                Core.LOGGER.info("Get ForgeHooks.");
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("onLivingUpdate")) {
                        ASMUtil.InsertReturn(mn, Type.BOOLEAN_TYPE, Boolean.FALSE, 0, ASMUtil.inList());
                    }
                }
                break;
            }
            case "net.minecraft.client.renderer.RenderGlobal": {
                changed = true;
                Core.LOGGER.info("Get RenderGlobal");
                RenderGlobal.AddField(cn);
                break;
            }
            case "net.minecraftforge.common.MinecraftForge": {
                changed = true;
                Core.LOGGER.info("Get MinecraftForge.");
                MinecraftForge.AddField(cn);
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<clinit>")) {
                        MinecraftForge.InjectClassConstructor(mn);
                        break;
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.server.FMLServerHandler": {
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
                        Core.LOGGER.info("Inject into " + mn.name);
                    }
                }
                break;
            }
            case "net.minecraftforge.fml.client.FMLClientHandler": {
                changed = true;
                FMLClientHandler.AddField(cn);
            }
        }


        Core.LOGGER.info("Examine class:" + transformedName);

        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    switch (fin.name) {
                        case "field_70128_L": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife.");
                                fin.name = "HatedByLife";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":isDead to HatedByLife.");
                                fin.name = "HatedByLife";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70725_aQ": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time.");
                                fin.name = "Death_Time";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":deathTime to Death_Time.");
                                fin.name = "Death_Time";
                                changed = true;
                            }
                            break;
                        }
                        case "EVENT_BUS": {
                            if (fin.owner.equals("net/minecraftforge/common/MinecraftForge")) {
                                if (fin.getOpcode() == GETSTATIC) {
                                    fin.name = "Event_bus";
                                    Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":EVENT_BUS to Event_bus.");
                                    changed = true;
                                } else if (goodClass) {
                                    fin.name = "Event_bus";
                                    Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":EVENT_BUS to Event_bus.");
                                    changed = true;
                                }
                            }
                            break;
                        }
                        case "field_71439_g": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":player to PLAYER.");
                                fin.name = "PLAYER";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":player to PLAYER.");
                                fin.name = "PLAYER";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70181_x": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_70181_x to motionY.");
                                fin.name = "motionY";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_70181_x to motionY.");
                                fin.name = "motionY";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70159_w": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_70159_w to motionX.");
                                fin.name = "motionX";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_70159_w to motionX.");
                                fin.name = "motionX";
                                changed = true;
                            }
                            break;
                        }
                        case "field_70179_y": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_70179_y to motionZ.");
                                fin.name = "motionZ";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_70179_y to motionZ.");
                                fin.name = "motionZ";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71424_I": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_71424_I to profiler.");
                                fin.name = "profiler";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_71424_I to profiler.");
                                fin.name = "profiler";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71460_t": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_71460_t to entityRenderer.");
                                fin.name = "entityRenderer";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_71460_t to entityRenderer.");
                                fin.name = "entityRenderer";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175616_W": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_175616_W to renderManager.");
                                fin.name = "renderManager";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_175616_W to renderManager.");
                                fin.name = "renderManager";
                                changed = true;
                            }
                            break;
                        }
                        case "field_175010_j": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_175010_j to renderManager.");
                                fin.name = "renderManager";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_175010_j to renderManager.");
                                fin.name = "renderManager";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71417_B": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_71417_B to mouseHelper.");
                                fin.name = "mouseHelper";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_71417_B to mouseHelper.");
                                fin.name = "mouseHelper";
                                changed = true;
                            }
                            break;
                        }
                        case "field_71441_e": {
                            if (fin.getOpcode() == GETFIELD) {
                                Core.LOGGER.info("Redirecting:GETFIELD:" + transformedName + ":" + mn.name + ":field_71441_e to WORLD.");
                                fin.name = "WORLD";
                                changed = true;
                            } else if (goodClass) {
                                Core.LOGGER.info("Redirecting:PUTFIELD:" + transformedName + ":" + mn.name + ":field_71441_e to WORLD.");
                                fin.name = "WORLD";
                                changed = true;
                            }
                            break;
                        }
                        case "INSTANCE": {
                            if (fin.owner.equals("net/minecraftforge/fml/client/FMLClientHandler")) {
                                if (fin.getOpcode() == GETSTATIC) {
                                    Core.LOGGER.info("Redirecting:GETSTATIC:" + transformedName + ":" + mn.name + ":INSTANCE to instance.");
                                    fin.name = "instance";
                                    changed = true;
                                } else if (goodClass) {
                                    Core.LOGGER.info("Redirecting:PUTSTATIC:" + transformedName + ":" + mn.name + ":INSTANCE to instance.");
                                    fin.name = "instance";
                                    changed = true;
                                }
                            }
                        }
                    }
                } else if (ain instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    switch (min.name) {
                        case "func_71381_h": {
                            if (goodClass) {
                                Core.LOGGER.info("Redirecting:INVOKEVIRTUAL:" + transformedName + ":" + mn.name + ":func_71381_h to SetIngameFocus.");
                                min.name = "SetIngameFocus";
                                changed = true;
                            }
                            break;
                        }
                        case "func_71364_i": {
                            if (goodClass) {
                                Core.LOGGER.info("Redirecting:INVOKEVIRTUAL:" + transformedName + ":" + mn.name + ":func_71364_i to SetIngameNotInFocus.");
                                min.name = "SetIngameNotInFocus";
                                changed = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (mn.localVariables != null && !goodClass) {
                for (LocalVariableNode lvn : mn.localVariables) {
                    if (lvn.desc.startsWith("net/minecraftforge/") && lvn.desc.contains("/event/")) {
                        Core.LOGGER.info("Find event listsner:" + transformedName);
                        event_listeners.add(cn.name.replace('/', '.'));
                        break;
                    }
                }
            }
        }
        if (!goodClass) {
            for (FieldNode fn : cn.fields) {
                if (Modifier.isStatic(fn.access)) {
                    if (!fn.desc.startsWith("L")) {
                        Core.LOGGER.info("Add field " + fn.name + " to reset list.");
                        fields.add(new FieldInfo(cn, fn));
                    } else {
                        if (fn.signature != null) {
                            if ((fn.signature.startsWith("Ljava/util/List") || fn.signature.startsWith("Ljava/util/Set") || fn.signature.startsWith("Ljava/util/Map")) && fn.signature.contains("net/minecraft/entity")) {
                                Core.LOGGER.info("Add field " + fn.name + " to reset list.");
                                fields.add(new FieldInfo(cn, fn));
                            }
                        }
                    }
                }
            }
        }

        if (changed) {
            ClassWriter cw = new ClassWriter(compute_all ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            transformed = cw.toByteArray();
            save(transformed, transformedName);
            transformedClasses.add(transformedName);
            return transformed;
        } else {
            transformedClasses.add(transformedName);
            return basicClass;
        }

    }

    @Override
    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) {
        s = s.replace('/','.');
        return transform(s,classNameTransformer.remapClassName(s),bytes);
    }
}
