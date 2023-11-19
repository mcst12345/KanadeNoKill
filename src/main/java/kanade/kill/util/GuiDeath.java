package kanade.kill.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;

public class GuiDeath extends GuiGameOver {
    public boolean close = false;

    public GuiDeath() {
        super(null);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                close = true;
                this.mc.PLAYER.respawnPlayer();
                this.mc.displayGuiScreen(null);
                break;
            case 1:

                if (this.mc.WORLD.getWorldInfo().isHardcoreModeEnabled()) {
                    close = true;
                    this.mc.displayGuiScreen(new GuiMainMenu());
                } else {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (result) {
            if (this.mc.WORLD != null) {
                this.mc.WORLD.sendQuittingDisconnectingPacket();
            }

            this.mc.loadWorld(null);
            close = true;
            this.mc.displayGuiScreen(new GuiMainMenu());
        } else {
            this.mc.PLAYER.respawnPlayer();
            close = true;
            this.mc.displayGuiScreen(null);
        }
    }
}
