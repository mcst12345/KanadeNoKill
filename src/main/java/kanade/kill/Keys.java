package kanade.kill;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class Keys {
    public static final KeyBinding SWITCH_TIME_POINT = new KeyBinding("key.kanade.switch", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_U, "key.category.kanade");
    public static final KeyBinding SAVE = new KeyBinding("key.kanade.save", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_I, "key.category.kanade");

    public static void init() {
        Launch.LOGGER.info("Registering keys.");
        ClientRegistry.registerKeyBinding(SWITCH_TIME_POINT);
        ClientRegistry.registerKeyBinding(SAVE);
    }
}
