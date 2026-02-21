package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class OBJ_FirePit extends Entity {

    GamePanel gp;

    public OBJ_FirePit(GamePanel gp, int col, int row) {
        this.gp = gp;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = "Fire Pit";
        this.collision = true;
        this.solidArea = new Rectangle(0, 0, gp.tileSize, gp.tileSize);
        this.type = TYPE_OBSTACLE;
        this.life = 5;
        this.lightRadius = 350;

        // Visuals
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        
        // Stones ring
        g2.setColor(Color.GRAY);
        g2.fillOval(4, 4, gp.tileSize - 8, gp.tileSize - 8);
        // Fire center
        g2.setColor(Color.ORANGE);
        g2.fillOval(16, 16, gp.tileSize - 32, gp.tileSize - 32);
        
        g2.dispose();
    }

    @Override
    public void update() {
        // Flicker effect
        lightRadius = 350 + (int)(Math.random() * 10);

        // Generate particles
        if (gp.gameState == gp.playState) {
            if (Math.random() < 0.02) { // Reduced chance for less intense effect
                Color color = new Color(255, 150, 0); // Orange
                if(Math.random() > 0.5) color = new Color(255, 50, 0); // Redder
                
                int size = 6;
                int speed = 1;
                int maxLife = 40;
                gp.particleList.add(new Particle(gp, this, color, size, speed, maxLife, (int)(Math.random()*2)-1, -1));
            }
        }
    }
}