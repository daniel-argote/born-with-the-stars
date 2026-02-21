package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import main.GamePanel;

public class OBJ_Tipi extends Entity {

    public OBJ_Tipi(GamePanel gp, int x, int y) {
        this.worldX = x * gp.tileSize;
        this.worldY = y * gp.tileSize;
        this.name = "Tipi";
        this.type = TYPE_OBSTACLE;
        this.collision = true;
        
        // Hitbox (Center of the tile, slightly smaller)
        this.solidArea = new Rectangle(8, 8, gp.tileSize - 16, gp.tileSize - 16);
        this.solidAreaDefaultX = solidArea.x;
        this.solidAreaDefaultY = solidArea.y;
        this.life = 100; // Durable

        // Visuals
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        
        // Tipi Base (Cone/Triangle)
        int[] xPoints = {gp.tileSize/2, 8, gp.tileSize-8};
        int[] yPoints = {8, gp.tileSize-8, gp.tileSize-8};
        
        g2.setColor(new Color(210, 180, 140)); // Tan/Hide color
        g2.fillPolygon(xPoints, yPoints, 3);
        
        // Poles sticking out top
        g2.setColor(new Color(101, 67, 33)); // Dark Brown
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(gp.tileSize/2, 12, gp.tileSize/2 - 10, 2);
        g2.drawLine(gp.tileSize/2, 12, gp.tileSize/2 + 10, 2);
        
        // Door flap
        g2.setColor(new Color(80, 50, 20)); // Darker Brown
        g2.fillOval(gp.tileSize/2 - 6, gp.tileSize - 24, 12, 16);
        
        g2.dispose();
    }
}