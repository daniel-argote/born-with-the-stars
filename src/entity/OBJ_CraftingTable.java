package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class OBJ_CraftingTable extends Entity {

    public OBJ_CraftingTable(GamePanel gp, int x, int y) {
        this.worldX = x * gp.tileSize;
        this.worldY = y * gp.tileSize;
        this.name = "Crafting Table";
        this.collision = true;
        this.solidArea = new Rectangle(0, 0, gp.tileSize, gp.tileSize);
        this.type = TYPE_OBSTACLE;
        this.life = 100; 

        // Visuals
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        
        // Table top
        g2.setColor(new Color(139, 69, 19)); // Brown
        g2.fillRect(4, 16, gp.tileSize - 8, gp.tileSize - 32);
        
        // Legs
        g2.setColor(new Color(101, 67, 33)); // Darker Brown
        g2.fillRect(4, 16 + (gp.tileSize - 32), 8, 16);
        g2.fillRect(gp.tileSize - 12, 16 + (gp.tileSize - 32), 8, 16);
        g2.fillRect(4, 16, 8, 8); // Back legs hint
        g2.fillRect(gp.tileSize - 12, 16, 8, 8);

        // Tool on top (Hammer?)
        g2.setColor(Color.GRAY);
        g2.fillRect(20, 24, 24, 8);
        g2.setColor(Color.BLACK);
        g2.fillRect(40, 20, 8, 16);
        
        g2.dispose();
    }
}