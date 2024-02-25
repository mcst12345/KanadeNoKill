package kanade.kill.util.superRender;

import kanade.kill.Launch;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.*;

public class DeathWindow extends JFrame {
    private final Minecraft mc;
    public JPanel image;

    public DeathWindow() {
        this.setUndecorated(true);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.getContentPane().setLayout(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Minecraft mc = Minecraft.getMinecraft();
        this.mc = mc;
        this.setSize(mc.displayWidth + 20, mc.displayHeight + 20);
        Window mainWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
        if (mainWindow != null) {
            Point p = mainWindow.getLocation();
            this.setLocation((int) p.getX(), Launch.win ? (int) (p.getY() + 40) : (int) p.getY() + 80);
        } else {
            this.setLocation(Display.getX(), Launch.win ? Display.getY() + 40 : Display.getY() + 80);
        }
        this.image = new ImagePanel(mc);
        image.setBounds(0, 0, mc.displayWidth + 20, mc.displayHeight + 20);
        this.getContentPane().add(image);
        this.setVisible(true);
        this.setAlwaysOnTop(true);
    }

    public void update() {
        this.setLocation(Display.getX(), Launch.win ? Display.getY() + 40 : Display.getY() + 80);
        this.setSize(mc.displayWidth + 20, mc.displayHeight + 20);
        image.setBounds(0, 0, mc.displayWidth + 20, mc.displayHeight + 20);
    }
}
