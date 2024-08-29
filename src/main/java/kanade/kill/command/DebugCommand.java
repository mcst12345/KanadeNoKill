package kanade.kill.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class DebugCommand extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "KanadeDebug";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "For debugging. Always changing. No persist usage.";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
    }
}
