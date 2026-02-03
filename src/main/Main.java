package main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle("Pre-NW");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Sizes the window to the GamePanel settings

        window.setLocationRelativeTo(null); // Centers window on screen
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}