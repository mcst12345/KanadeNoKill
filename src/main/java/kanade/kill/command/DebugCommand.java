package kanade.kill.command;

import kanade.kill.ModMain;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

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

    public static ResourceLocation rl = ModMain.kill_item.getRegistryName();

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        String s = args[0];
        rl = new ResourceLocation(s);
        //ModelResourceLocation model = new ModelResourceLocation(s, "inventory");
        //Util.setItemModel(ModMain.kill_item,0,model,true);
    }
}
