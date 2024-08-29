package kanade.kill.command;

import kanade.kill.Launch;
import kanade.kill.item.KillItem;
import kanade.kill.util.EntityUtil;
import miku.lib.utils.ObjectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandInfo extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "Info";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/Info";
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("worlds");
            ret.add("threads");
            ret.add("chunks");
            ret.add("isDead");
            ret.add("isProtected");
            ret.add("isModClass");
            if (Launch.client) {
                ret.add("player");
                ret.add("renderGlobal");
            }
        }
        return getListOfStringsMatchingLastWord(args, ret);
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        String target = args[0];
        switch (target) {
            case "threads": {
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    sender.sendMessage(new TextComponentString(thread.getName()));
                }
                break;
            }
            case "worlds": {
                sender.sendMessage(new TextComponentString("Server:"));
                for (WorldServer ws : DimensionManager.getWorlds()) {
                    sender.sendMessage(new TextComponentString(ws.getProviderName()));
                    sender.sendMessage(new TextComponentString(ws.getClass().getName()));
                    sender.sendMessage(new TextComponentString("loadedEntityList"));
                    sender.sendMessage(new TextComponentString(ws.entities.getClass().getName()));
                    sender.sendMessage(new TextComponentString(ws.entities.toString()));
                    sender.sendMessage(new TextComponentString("playerEntities"));
                    sender.sendMessage(new TextComponentString(ws.players.getClass().getName()));
                    sender.sendMessage(new TextComponentString(ws.players.toString()));
                }
                if (Launch.client) {
                    WorldClient wc = Minecraft.getMinecraft().world;
                    sender.sendMessage(new TextComponentString("Client:"));
                    sender.sendMessage(new TextComponentString(wc.getClass().getName()));
                    sender.sendMessage(new TextComponentString("loadedEntityList"));
                    sender.sendMessage(new TextComponentString(wc.entities.getClass().getName()));
                    sender.sendMessage(new TextComponentString(wc.entities.toString()));
                    sender.sendMessage(new TextComponentString("playerEntities"));
                    sender.sendMessage(new TextComponentString(wc.players.getClass().getName()));
                    sender.sendMessage(new TextComponentString(wc.players.toString()));
                }
                break;
            }
            case "chunks": {
                sender.sendMessage(new TextComponentString("Server:"));
                for (WorldServer ws : DimensionManager.getWorlds()) {
                    for (Chunk chunk : ((ChunkProviderServer) ws.chunkProvider).loadedChunks.values()) {
                        sender.sendMessage(new TextComponentString(chunk.getPos() + ":" + chunk.getClass().getName()));
                        for (ClassInheritanceMultiMap<Entity> map : chunk.entities) {
                            sender.sendMessage(new TextComponentString(map.getClass().getName()));
                            sender.sendMessage(new TextComponentString("Base:" + map.baseClass.getName()));
                            sender.sendMessage(new TextComponentString("Values:" + map.values));
                            sender.sendMessage(new TextComponentString("Map" + map.map));
                            sender.sendMessage(new TextComponentString("KnownKeys" + map.knownKeys));
                        }
                    }
                }
                if (Launch.client) {
                    sender.sendMessage(new TextComponentString("Client:"));
                    WorldClient wc = Minecraft.getMinecraft().world;
                    for (Chunk chunk : ((ChunkProviderClient) wc.chunkProvider).loadedChunks.values()) {
                        sender.sendMessage(new TextComponentString(chunk.getPos() + ":" + chunk.getClass().getName()));
                        for (ClassInheritanceMultiMap<Entity> map : chunk.entities) {
                            sender.sendMessage(new TextComponentString(map.getClass().getName()));
                            sender.sendMessage(new TextComponentString("Base:" + map.baseClass.getName()));
                            sender.sendMessage(new TextComponentString("Values:" + map.values));
                            sender.sendMessage(new TextComponentString("Map" + map.map));
                            sender.sendMessage(new TextComponentString("KnownKeys" + map.knownKeys));
                        }
                    }
                }
                break;
            }
            case "renderGlobal": {
                if (Launch.client) {
                    RenderGlobal renderGlobal = Minecraft.getMinecraft().RenderGlobal;
                    sender.sendMessage(new TextComponentString(renderGlobal.getClass().getName()));
                    for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobal.renderInfos) {
                        sender.sendMessage(new TextComponentString(info.getClass().getName()));
                    }
                }
                break;
            }
            case "player": {
                if (Launch.client) {
                    EntityPlayerSP player = Minecraft.getMinecraft().PLAYER;
                    sender.sendMessage(new TextComponentString(player.getName()));
                    sender.sendMessage(new TextComponentString("X:" + player.X));
                    sender.sendMessage(new TextComponentString("Y:" + player.Y));
                    sender.sendMessage(new TextComponentString("Z:" + player.Z));
                    sender.sendMessage(new TextComponentString("rotationYaw:" + player.rotationYaw % 360));
                    sender.sendMessage(new TextComponentString("rotationYawHead:" + player.rotationYawHead % 360));
                    sender.sendMessage(new TextComponentString("rotationPitch:" + player.rotationPitch % 360));
                    sender.sendMessage(new TextComponentString("ID:" + player.entityId));
                    sender.sendMessage(new TextComponentString("UUID:" + player.uuid));
                    sender.sendMessage(new TextComponentString("Permission:" + player.getPermissionLevel()));
                }
                break;
            }
            case "isDead": {
                try {
                    sender.sendMessage(new TextComponentString(String.valueOf(EntityUtil.isDead(getCommandSenderAsPlayer(sender)))));
                } catch (PlayerNotFoundException e) {
                    throw new CommandException("No player found!", e);
                }
                break;
            }
            case "isProtected": {
                try {
                    sender.sendMessage(new TextComponentString(String.valueOf(KillItem.inList(getCommandSenderAsPlayer(sender)))));
                } catch (PlayerNotFoundException e) {
                    throw new CommandException("No player found!", e);
                }
                break;
            }
            case "isModClass": {
                if (args.length < 2) {
                    throw new CommandException("Too few arguments!");
                }
                String cls = args[1];
                sender.sendMessage(new TextComponentString(String.valueOf(ObjectUtils.ModClass(cls))));
                break;
            }
        }
    }
}
