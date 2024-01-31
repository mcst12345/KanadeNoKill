package kanade.kill.util;

import net.minecraft.client.settings.GameSettings;

public class ClientFakeObjects {
    private static final GameSettings gameSettings = new GameSettings();

    public static GameSettings fakeGameSettings() {
        return gameSettings;
    }
}
