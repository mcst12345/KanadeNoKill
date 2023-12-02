package kanade.kill.command;

import kanade.kill.Config;
import kanade.kill.util.Util;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class KanadeKillCommand extends CommandBase {
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
        if (args.length < 2) {
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
                }
            }
        }
    }
}
