package kanade.kill.command;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.ConfigUpdatePacket;
import kanade.kill.network.packets.KillAllEntities;
import kanade.kill.network.packets.Reset;
import kanade.kill.util.EntityUtil;
import kanade.kill.util.NativeMethods;
import kanade.kill.util.Util;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KanadeKillCommand extends CommandBase {
    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("config");
            ret.add("kill");
            ret.add("protected");
            ret.add("mode");
            ret.add("reset");
            ret.add("killall");
        } else {
            if (args.length == 2) {
                if (args[0].equals("config")) {
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
                } else if (args[0].equals("mode")) {
                    ret.add("timestop");
                    ret.add("timeback");
                    ret.add("Annihilation");
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
                                Config.forceRender = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "allPlayerProtect": {
                                Config.allPlayerProtect = Boolean.parseBoolean(arg2);
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                break;
                            }
                            case "disableParticle": {
                                Config.disableParticle = Boolean.parseBoolean(arg2);
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
                    synchronized (Util.tasks) {
                        Util.tasks.add(() -> {
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
                }
                default: {
                    sender.sendMessage(new TextComponentString("Unknown command."));
                    break;
                }
            }
        }
    }
}
