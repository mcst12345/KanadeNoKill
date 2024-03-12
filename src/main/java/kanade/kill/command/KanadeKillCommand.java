package kanade.kill.command;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.*;
import kanade.kill.reflection.EarlyFields;
import kanade.kill.util.ClassUtil;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.ThreadUtil;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.GameData;
import scala.concurrent.util.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
public class KanadeKillCommand extends CommandBase {
    private static final Set<String> FuckedMods = new HashSet<>();
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("config");
            ret.add("kill");
            ret.add("protected");
            ret.add("mode");
            ret.add("reset");
            ret.add("killall");
            ret.add("fuckmod");
            ret.add("tick");
            ret.add("threads");
            ret.add("killthreads");
        } else {
            if (args.length == 2) {
                switch (args[0]) {
                    case "config":
                        ret.add("allReturn");
                        ret.add("disableEvent");
                        ret.add("guiProtect");
                        ret.add("coreDumpAttack");
                        ret.add("forceRender");
                        ret.add("allPlayerProtect");
                        ret.add("disableParticle");
                        ret.add("renderProtection");
                        ret.add("fieldReset");
                        ret.add("particleEffect");
                        ret.add("SuperAttack");
                        ret.add("redefineAttack");
                        ret.add("outScreenRender");
                        ret.add("CrystalBeam");
                        ret.add("SuperMode");
                        ret.add("particleType");
                        break;
                    case "mode":
                        ret.add("timestop");
                        ret.add("timeback");
                        ret.add("Annihilation");
                        break;
                    case "fuckmod":
                        Map<String, ModContainer> namedMods = (Map<String, ModContainer>) Unsafe.instance.getObjectVolatile(Loader.instance(), EarlyFields.namedMods_offset);
                        Launch.LOGGER.info("Find " + namedMods.keySet().size() + " mods.");
                        ret.addAll(namedMods.keySet());
                        break;
                }
            } else {
                if (args[0].equals("config")) {
                    ret.add("true");
                    ret.add("false");
                }
            }
        }

        return getListOfStringsMatchingLastWord(args, ret);
    }

    @Override
    @Nonnull
    public String getName() {
        return "KanadeKill";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/KanadeKill <action> <args>";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("/KanadeKill <action> <args>");
        } else {
            String action = args[0];
            switch (action) {
                case "threads": {
                    ThreadUtil.printThreads();
                    break;
                }
                case "config": {
                    String arg1 = args[1];
                    if (args.length < 3) {
                        sender.sendMessage(new TextComponentString("Wrong usage. /KanadeKill config <name> <value>"));
                    } else {
                        String arg2 = args[2];
                        switch (arg1) {
                            case "allReturn": {
                                Config.allReturn = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "disableEvent": {
                                Config.disableEvent = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "guiProtect": {
                                Config.guiProtect = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "coreDumpAttack": {
                                Config.coreDumpAttack = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "forceRender": {
                                if (Config.outScreenRender && Boolean.parseBoolean(arg2)) {
                                    sender.sendMessage(new TextComponentString("forceRender is incompatible with outScreenRender!"));
                                    break;
                                }
                                Config.forceRender = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "allPlayerProtect": {
                                Config.allPlayerProtect = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "renderProtection": {
                                Config.renderProtection = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "fieldReset": {
                                Config.fieldReset = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "particleEffect": {
                                Config.particleEffect = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "SuperAttack": {
                                Config.SuperAttack = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "redefineAttack": {
                                Config.redefineAttack = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "outScreenRender": {
                                if (Config.forceRender && Boolean.parseBoolean(arg2)) {
                                    sender.sendMessage(new TextComponentString("outScreenRender is incompatible with forceRender!"));
                                    break;
                                }
                                Config.outScreenRender = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "CrystalBeam": {
                                Config.CrystalBeam = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "particleType": {
                                try {
                                    EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                                    NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket(arg1, Integer.parseInt(arg2)), player);
                                } catch (PlayerNotFoundException e){
                                    sender.sendMessage(new TextComponentString("No player found. Don't use this in server console."));
                                }
                                break;
                            }
                            case "SuperMode": {
                                boolean neo = Boolean.parseBoolean(arg2);
                                if (neo == Config.SuperMode) {
                                    break;
                                }
                                Config.SuperAttack = neo;
                                NetworkHandler.INSTANCE.sendMessageToAll(new UpdateSuperMode(neo));
                                break;
                            }
                            default: {
                                sender.sendMessage(new TextComponentString("Config " + arg1 + " isn't found!"));
                                return;
                            }
                        }
                        sender.sendMessage(new TextComponentString("Config " + arg1 + " is updated."));
                    }
                    break;
                }
                case "kill": {
                    if (args.length < 3) {
                        try {
                            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                            EntityUtil.Kill(player, true);
                        } catch (PlayerNotFoundException e) {
                            sender.sendMessage(new TextComponentString("No player found. Don't use this in server console."));
                        }
                    } else {
                        Entity entity = getEntity(server, sender, args[2]);
                        EntityUtil.Kill(entity, true);
                    }
                    break;
                }
                case "protected": {
                    try {
                        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                        boolean b = KillItem.inList(player);
                        TextComponentString message = new TextComponentString("Current player is protected:" + b);
                        sender.sendMessage(message);
                    } catch (PlayerNotFoundException e) {
                        sender.sendMessage(new TextComponentString("No player found. Don't use this in server console."));
                    }
                    break;
                }
                case "mode": {
                    try {
                        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                        String arg1 = args[1];
                        switch (arg1) {
                            case "timeback": {
                                NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket("kanade.kill.item.KillItem", "mode", 2), player);
                                sender.sendMessage(new TextComponentString("Set item shift-right-click mode to timeback"));
                                break;
                            }
                            case "timestop": {
                                NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket("kanade.kill.item.KillItem", "mode", 1), player);
                                sender.sendMessage(new TextComponentString("Set item shift-right-click mode to timestop"));
                                break;
                            }
                            case "Annihilation": {
                                NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket("kanade.kill.item.KillItem", "mode", 0), player);
                                sender.sendMessage(new TextComponentString("Set item shift-right-click mode to Annihilation"));
                                break;
                            }
                            default: {
                                sender.sendMessage(new TextComponentString("Unknown mode.."));
                            }
                        }
                    } catch (PlayerNotFoundException e) {
                        sender.sendMessage(new TextComponentString("No player found. Don't use this in server console."));
                    }
                    break;
                }
                case "reset": {
                    Launch.LOGGER.info("Resetting...");
                    NativeMethods.Reset();
                    KillItem.list.clear();
                    EntityUtil.Dead.clear();
                    EntityUtil.blackHolePlayers.clear();
                    for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                        player.Inventory = new InventoryPlayer(player);
                        NetworkHandler.INSTANCE.sendMessageToPlayer(new Reset(), player);
                    }
                    break;
                }
                case "killall": {
                    synchronized (kanade.kill.asm.hooks.MinecraftServer.futureTaskQueue) {
                        kanade.kill.asm.hooks.MinecraftServer.AddTask(() -> {
                            List<Entity> targets = new ArrayList<>();
                            for (int id : DimensionManager.getIDs()) {
                                WorldServer world = DimensionManager.getWorld(id);
                                targets.addAll(world.entities);
                            }
                            EntityUtil.Kill(targets);
                        });
                    }
                    if (server.isCallingFromMinecraftThread()) {
                        NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillAllEntities());
                    }
                    break;
                }
                case "killthreads": {
                    int count = ThreadUtil.FuckThreads();
                    sender.sendMessage(new TextComponentString("Killed " + count + " threads."));
                    break;
                }
                case "fuckmod": {
                    if (args.length < 2) {
                        sender.sendMessage(new TextComponentString("Usage: /KanadeKill fuckmod <modid>"));
                        break;
                    }
                    String modid = args[1];
                    if (FuckedMods.contains(modid)) {
                        sender.sendMessage(new TextComponentString("Mod " + modid + " is already fucked."));
                    }
                    Map<String, ModContainer> namedMods = (Map<String, ModContainer>) Unsafe.instance.getObjectVolatile(Loader.instance(), EarlyFields.namedMods_offset);
                    ModContainer mod = namedMods.get(modid);
                    if (mod == null) {
                        sender.sendMessage(new TextComponentString("Mod " + modid + " not found!"));
                        break;
                    }
                    GameData.entityRegistry.getValuesCollection().forEach(entityEntry -> {
                        if (entityEntry.getRegistryName() != null && entityEntry.getRegistryName().getNamespace().equals(modid)) {
                            try {
                                Class<? extends Entity> fake = (Class<? extends Entity>) ClassUtil.generateTempClass(entityEntry.cls.getName());
                                entityEntry.cls = fake;
                                entityEntry.factory = new EntityEntryBuilder.ConstructorFactory<Entity>(fake) {
                                    @Override
                                    protected String describeEntity() {
                                        return String.valueOf(entityEntry.getRegistryName());
                                    }
                                };
                            } catch (ClassNotFoundException e) {
                                Launch.LOGGER.error("The fuck?", e);
                            }
                        }
                    });
                    FuckedMods.add(modid);
                    break;
                }
                case "tick": {
                    int tick = Integer.parseInt(args[1]);
                    NetworkHandler.INSTANCE.sendMessageToAllPlayer(new UpdateTickCount(tick));
                    break;
                }
                default: {
                    sender.sendMessage(new TextComponentString("Unknown command."));
                    break;
                }
            }
        }
    }
}
