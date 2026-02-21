package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class OBJ_DroppedItem extends Entity {

    GamePanel gp;
    public Item item;

    public OBJ_DroppedItem(GamePanel gp, Item item, int x, int y) {
        this.gp = gp;
        this.worldX = x;
        this.worldY = y;
        this.item = item;
        this.name = item.name;
        this.type = TYPE_PICKUP;
        this.collision = false; // Player can walk through it
        this.solidArea = new Rectangle(0, 0, 48, 48);
        this.solidAreaDefaultX = 0;
        this.solidAreaDefaultY = 0;
        this.life = 1; // Ensure it isn't removed by the GamePanel update loop

        // Generate visual
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        
        // Use the Item's draw method
        item.draw(g2, 0, 0, gp.tileSize);
        
        g2.dispose();
    }

    @Override
    public void update() {
        // MAGNET EFFECT
        if (gp.player.skill.magnetUnlocked) {
            double dx = gp.player.worldX - worldX;
            double dy = gp.player.worldY - worldY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            // If within 4 tiles
            if (dist < gp.tileSize * 4) {
                // Move towards player
                double speed = 6; // Faster than player walking
                double angle = Math.atan2(dy, dx);
                
                worldX += Math.cos(angle) * speed;
                worldY += Math.sin(angle) * speed;
                
                // Auto-pickup if very close (handled in GamePanel collision, but visual snap helps)
                if (dist < gp.tileSize / 2) {
                    // GamePanel collision loop will pick it up
                }
            }
        }
    }
}