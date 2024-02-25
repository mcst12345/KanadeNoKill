package kanade.kill;

import javax.swing.*;
import java.awt.*;

public class RenderTest {
    public static void main(String[] args) {
        new Window();
    }

    public static class Window extends JFrame {
        public Window() {
            this.setUndecorated(true);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setLayout(null);
            setLocationRelativeTo(null);
            setSize(400, 400);
            setResizable(false);
            getContentPane().setLayout(null);
            JPanel panel = new ImagePanel();
            panel.setBounds(0, -0, 400, 400);
            panel.setLayout(null);
            getContentPane().add(panel);
            this.setLocation(0, 0);
            setVisible(true);
        }
    }

    public static class ImagePanel extends JPanel {
        public void paint(Graphics g) {
            super.paint(g);
            this.setLocation(0, 40);
            ImageIcon icon = new ImageIcon("/TEST.png");
            g.drawImage(icon.getImage(), 0, 0, 400, 400, this);
        }
    }
}
