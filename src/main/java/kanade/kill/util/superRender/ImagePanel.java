package kanade.kill.util.superRender;

import kanade.kill.Launch;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private final Minecraft mc;

    public ImagePanel(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        ImageIcon icon = new ImageIcon(Launch.deathImage);
        g.drawImage(icon.getImage(), 0, 0, mc.displayWidth + 20, mc.displayHeight + 20, this);
    }
}
