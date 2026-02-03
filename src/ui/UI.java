package ui;

import main.GamePanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

public class UI {

    GamePanel gp;
    Font arial_20;
    DecimalFormat df = new DecimalFormat("#.##");
    
    // Toggle variables
    public boolean showUI = true;
    private boolean mKeyLock = false;

    // Animation variables
    private double currentOffset = 200; // Start off-screen
    private int targetOffset = 200;
    private double animationSpeed = 15.0; // Adjust for faster/slower slide

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_20 = new Font("Arial", Font.PLAIN, 20);
    }

    public void draw(Graphics2D g2) {
        
        // 1. HANDLE TOGGLE INPUT
        if (gp.keyH.mPressed) {
            if (!mKeyLock) {
                showUI = !showUI;
                targetOffset = showUI ? 0 : 250; // 250px is enough to hide it
                mKeyLock = true;
            }
        } else {
            mKeyLock = false;
        }

        // 2. SMOOTH THE ANIMATION
        // This moves currentOffset toward targetOffset a little bit every frame
        // LERP: moves 10% of the remaining distance every frame
        double lerpFactor = 0.1; 
        currentOffset += (targetOffset - currentOffset) * lerpFactor;

        // 3. DRAW EVERYTHING IF TOGGLED ON
        drawCoordinates(g2);
        drawMiniMap(g2);
    }

    private void drawCoordinates(Graphics2D g2) {
        g2.setFont(arial_20);
        g2.setColor(Color.WHITE);

        String text = "X: " + df.format(gp.player.worldX) + 
                      " Y: " + df.format(gp.player.worldY) + 
                      " | Zoom: " + df.format(gp.scale);
        
        // Shadow for readability
        g2.setColor(Color.BLACK);
        g2.drawString(text, 22, 42);
        g2.setColor(Color.WHITE);
        g2.drawString(text, 20, 40);
    }

    private void drawMiniMap(Graphics2D g2) {
        int mapWidth = 150;
        int mapHeight = 150;
        int mapX = (int)(gp.screenWidth - mapWidth - 20 + currentOffset);
        int mapY = 20;

        // Background
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(mapX, mapY, mapWidth, mapHeight);
        
        // Mini-map logic (your existing loop)
        int pixelSize = mapWidth / gp.maxWorldCol; 
        for (int i = 0; i < gp.maxWorldCol; i++) {
            for (int j = 0; j < gp.maxWorldRow; j++) {
                int tileNum = gp.tileM.mapTileNum[i][j];
                
                if (tileNum >= 0 && tileNum <= 63) g2.setColor(new Color(34, 139, 34)); // Grass
                else if (tileNum >= 800 && tileNum <= 1119) g2.setColor(new Color(0, 0, 139)); // Ocean
                else if (tileNum >= 1120 && tileNum <= 1183) g2.setColor(new Color(238, 214, 175)); // Coast
                else g2.setColor(Color.GRAY);

                g2.fillRect(mapX + (i * pixelSize), mapY + (j * pixelSize), pixelSize, pixelSize);
            }
        }

        // Player dot
        int playerMapX = (int)(mapX + (gp.player.worldX / gp.tileSize) * pixelSize);
        int playerMapY = (int)(mapY + (gp.player.worldY / gp.tileSize) * pixelSize);
        g2.setColor(Color.RED);
        g2.fillRect(playerMapX - 1, playerMapY - 1, 3, 3);
    }
}