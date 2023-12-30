package kanade.kill.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class Gui extends GuiScreen {
    public boolean close = false;

    private final ITextComponent causeOfDeath;
    private int enableButtonsTimer;

    public Gui() {
        this.causeOfDeath = new TextComponentString("");
        this.allowUserInput = true;
    }

    public void initGui() {
        this.buttonList.clear();
        this.enableButtonsTimer = 0;

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));

        if (this.mc.getSession() == null) {
            (this.buttonList.get(1)).enabled = false;
        }

        for (GuiButton guibutton : this.buttonList) {
            guibutton.enabled = false;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
            this.mc.displayGuiScreen(guiyesno);
            guiyesno.setButtonDelay(20);
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
            Minecraft.dead = false;
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    public void updateScreen() {
        super.updateScreen();
        ++this.enableButtonsTimer;

        if (this.enableButtonsTimer == 20) {
            for (GuiButton guibutton : this.buttonList) {
                guibutton.enabled = true;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(this.fontRenderer, I18n.format("deathScreen.title"), this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        this.drawCenteredString(this.fontRenderer, this.causeOfDeath.getFormattedText(), this.width / 2, 85, 16777215);

        this.drawCenteredString(this.fontRenderer, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + this.mc.PLAYER.getScore(), this.width / 2, 100, 16777215);

        if (mouseY > 85 && mouseY < 85 + this.fontRenderer.FONT_HEIGHT) {
            ITextComponent itextcomponent = this.getClickedComponentAt(mouseX);

            if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
                this.handleComponentHover(itextcomponent, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Nullable
    public ITextComponent getClickedComponentAt(int p_184870_1_) {
        int i = this.mc.fontRenderer.getStringWidth(this.causeOfDeath.getFormattedText());
        int j = this.width / 2 - i / 2;
        int k = this.width / 2 + i / 2;
        int l = j;

        if (p_184870_1_ >= j && p_184870_1_ <= k) {
            for (ITextComponent itextcomponent : this.causeOfDeath) {
                l += this.mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(itextcomponent.getUnformattedComponentText(), false));

                if (l > p_184870_1_) {
                    return itextcomponent;
                }
            }

        }
        return null;
    }
}
