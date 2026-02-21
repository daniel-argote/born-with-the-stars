package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class OBJ_SpiritAnimal extends Entity {

    GamePanel gp;

    public OBJ_SpiritAnimal(GamePanel gp) {
        this.gp = gp;
        this.name = "Spirit Companion";
        this.speed = 3;
        this.type = TYPE_NPC;
        this.solidArea = new Rectangle(0, 0, 0, 0); // No collision
        this.collision = false;
        this.invincible = true;
        this.life = 100;
        
        // Start at player position
        this.worldX = gp.player.worldX;
        this.worldY = gp.player.worldY;
        
        generateVisuals();
    }
    
    private void generateVisuals() {
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        
        // Spirit Wisp / Wolf-ish shape
        g2.setColor(new Color(135, 206, 250)); // Light Sky Blue
        g2.fillOval(12, 12, 40, 40); 
        
        // Ears
        int[] xPoints = {16, 24, 12};
        int[] yPoints = {20, 20, 4};
        g2.fillPolygon(xPoints, yPoints, 3);
        
        int[] xPoints2 = {48, 40, 52};
        int[] yPoints2 = {20, 20, 4};
        g2.fillPolygon(xPoints2, yPoints2, 3);

        // Eyes
        g2.setColor(Color.WHITE);
        g2.fillOval(20, 24, 8, 8);
        g2.fillOval(36, 24, 8, 8);
        
        g2.dispose();
    }

    @Override
    public void update() {
        // Follow Player
        int targetX = gp.player.worldX;
        int targetY = gp.player.worldY;
        
        // Calculate distance
        double dx = targetX - worldX;
        double dy = targetY - worldY;
        double dist = Math.sqrt(dx*dx + dy*dy);
        
        if (dist > gp.tileSize * 15) {
            // Teleport if too far
            worldX = targetX;
            worldY = targetY;
        } else if (dist > gp.tileSize * 1.5) {
            // Move towards player
            double angle = Math.atan2(dy, dx);
            worldX += Math.cos(angle) * speed;
            worldY += Math.sin(angle) * speed;
        }
        
        // Particle effect trail
        if (gp.gameState == gp.playState && Math.random() < 0.1) {
             Color color = new Color(135, 206, 250);
             gp.particleList.add(new Particle(gp, this, color, 4, 1, 20, 0, 0));
        }
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Draw with transparency
        float alpha = 0.7f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        super.draw(g2, gp);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}