package kanade.kill.command;

import kanade.kill.Config;
import kanade.kill.item.KillItem;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.ConfigUpdatePacket;
import kanade.kill.util.EntityUtil;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KanadeKillCommand extends CommandBase {
    @Override
    @Nonnull
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("config");
            ret.add("kill");
            ret.add("protected");
            ret.add("mode");
        } else {
            if (args.length == 2) {
                if (args[0].equals("config")) {
                    ret.add("allReturn");
                    ret.add("disableEvent");
                    ret.add("guiProtect");
                    ret.add("coreDumpAttack");
                    ret.add("forceRender");
                    ret.add("disableParticle");
                    ret.add("renderProtection");
                    ret.add("allPlayerProtect");
                    ret.add("fieldReset");
                    ret.add("particleEffect");
                } else if (args[0].equals("mode")) {
                    ret.add("timestop");
                    ret.add("Annihilation");
                }
            } else {
                if (args[0].equals("config")) {
                    ret.add("true");
                    ret.add("false");
                }
            }
        }

        return getListOfStringsFromIterableMatchingLastWord(args, ret);
    }
    @Override
    @Nonnull
    public String getCommandName() {
        return "KanadeKill";
    }

    @Override
    @Nonnull
    public String getCommandUsage(@Nonnull ICommandSender sender) {
        return "/KanadeKill <action> <args>";
    }

    @Override
    public void processCommand(@Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("/KanadeKill <action> <args>");
        } else {
            String action = args[0];
            switch (action) {
                case "config": {
                    if (args.length < 3) {
                        sender.addChatMessage(new ChatComponentText("Wrong usage. /KanadeKill config <name> <value>"));
                    } else {
                        String arg1 = args[1];
                        String arg2 = args[2];
                        switch (arg1) {
                            case "allReturn": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.allReturn = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "disableEvent": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.disableEvent = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "guiProtect": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.guiProtect = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "coreDumpAttack": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.coreDumpAttack = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "forceRender": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.forceRender = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "allPlayerProtect": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.allPlayerProtect = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "disableParticle": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.disableParticle = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "renderProtection": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.renderProtection = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "fieldReset": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.fieldReset = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "particleEffect": {
                                NetworkHandler.INSTANCE.sendMessageToAll(new ConfigUpdatePacket(arg1, Boolean.parseBoolean(arg2)));
                                Config.particleEffect = Boolean.parseBoolean(arg2);
                                break;
                            }
                            default: {
                                sender.addChatMessage(new ChatComponentText("Config " + arg1 + " isn't found!"));
                                return;
                            }
                        }
                        sender.addChatMessage(new ChatComponentText("Config " + arg1 + " is updated."));
                    }
                    break;
                }
                case "kill": {
                    if (args.length < 3) {
                        try {
                            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                            EntityUtil.Kill(player, true);
                        } catch (PlayerNotFoundException e) {
                            sender.addChatMessage(new ChatComponentText("No player found. Don't use this in server console."));
                        }
                    } else {
                        Entity entity = getPlayer(sender, args[2]);
                        EntityUtil.Kill(entity, true);
                    }
                }
                case "protected": {
                    try {
                        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                        boolean b = KillItem.inList(player);
                        ChatComponentText message = new ChatComponentText("Current player is protected:" + b);
                        sender.addChatMessage(message);
                    } catch (PlayerNotFoundException e) {
                        sender.addChatMessage(new ChatComponentText("No player found. Don't use this in server console."));
                    }
                }
                case "mode": {
                    try {
                        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                        String arg1 = args[1];
                        switch (arg1) {
                            case "timestop": {
                                NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket("kanade.kill.item.KillItem", "mode", 1), player);
                                sender.addChatMessage(new ChatComponentText("Set item shift-right-click mode to timestop"));
                                break;
                            }
                            case "Annihilation": {
                                NetworkHandler.INSTANCE.sendMessageToPlayer(new ConfigUpdatePacket("kanade.kill.item.KillItem", "mode", 0), player);
                                sender.addChatMessage(new ChatComponentText("Set item shift-right-click mode to Annihilation"));
                                break;
                            }
                            default: {
                                sender.addChatMessage(new ChatComponentText("Unknown mode.."));
                            }
                        }
                    } catch (PlayerNotFoundException e) {
                        sender.addChatMessage(new ChatComponentText("No player found. Don't use this in server console."));
                    }
                    break;
                }
                default: {
                    sender.addChatMessage(new ChatComponentText("Unknown command."));
                }
            }
        }
    }
}
