package main;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle("Born with the Stars");

        // Set Window Icon
        URL iconURL = Main.class.getResource("/icon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            window.setIconImage(icon.getImage());
        }

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Sizes the window to the GamePanel settings

        window.setLocationRelativeTo(null); // Centers window on screen
        window.setVisible(true);

        gamePanel.startGameThread();
        gamePanel.requestFocusInWindow();
    }
}