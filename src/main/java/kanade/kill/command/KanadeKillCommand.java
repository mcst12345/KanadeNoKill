package kanade.kill.command;

import kanade.kill.Config;
import kanade.kill.item.KillItem;
import kanade.kill.util.Util;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

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

        return ret;
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
            String arg1 = args[1];
            switch (action) {
                case "config": {
                    if (args.length < 3) {
                        sender.sendMessage(new TextComponentString("Wrong usage. /KanadeKill config <name> <value>"));
                    } else {
                        String arg2 = args[2];
                        switch (arg1) {
                            case "allReturn": {
                                Config.allReturn = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "disableEvent": {
                                Config.disableEvent = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "guiProtect": {
                                Config.guiProtect = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "coreDumpAttack": {
                                Config.coreDumpAttack = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "forceRender": {
                                Config.forceRender = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "allPlayerProtect": {
                                Config.allPlayerProtect = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "disableParticle": {
                                Config.disableParticle = Boolean.parseBoolean(arg2);
                                break;
                            }
                            case "renderProtection": {
                                Config.renderProtection = Boolean.parseBoolean(arg2);
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
                            Util.Kill(player);
                        } catch (PlayerNotFoundException e) {
                            sender.sendMessage(new TextComponentString("No player found. Don't use this in server console."));
                        }
                    } else {
                        Entity entity = getEntity(server, sender, args[2]);
                        Util.Kill(entity);
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
                    switch (arg1) {
                        case "timestop": {
                            KillItem.mode = 1;
                            sender.sendMessage(new TextComponentString("Set item shift-right-click mode to timestop"));
                            break;
                        }
                        case "Annihilation": {
                            sender.sendMessage(new TextComponentString("Set item shift-right-click mode to Annihilation"));
                            KillItem.mode = 0;
                            break;
                        }
                        default: {
                            sender.sendMessage(new TextComponentString("Unknown mode.."));
                        }
                    }
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
