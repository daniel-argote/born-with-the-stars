package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;
import main.GamePanel;

public class OBJ_Environment extends Entity {

    public OBJ_Environment(GamePanel gp, int col, int row, String type) {
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = type;
        this.type = TYPE_OBSTACLE;
        this.collision = true;
        
        // Default solid area (can be overridden in setup)
        this.solidArea = new Rectangle(0, 0, gp.tileSize, gp.tileSize);
        
        generateProperties(gp);
        
        this.solidAreaDefaultX = solidArea.x;
        this.solidAreaDefaultY = solidArea.y;
    }

    private void generateProperties(GamePanel gp) {
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();
        Random random = new Random();

        switch (name) {
            case "Tree":
                life = 10;
                // Trunk
                int trunkR = Math.min(255, Math.max(0, 100 + random.nextInt(40) - 20));
                int trunkG = Math.min(255, Math.max(0, 60 + random.nextInt(20) - 10));
                int trunkB = Math.min(255, Math.max(0, 20 + random.nextInt(10) - 5));
                g2.setColor(new Color(trunkR, trunkG, trunkB));
                g2.fillRect(gp.tileSize/2 - 6, gp.tileSize/2, 12, 32);
                // Leaves
                int leafR = Math.min(255, Math.max(0, 34 + random.nextInt(40) - 20));
                int leafG = Math.min(255, Math.max(0, 139 + random.nextInt(60) - 30));
                int leafB = Math.min(255, Math.max(0, 34 + random.nextInt(40) - 20));
                g2.setColor(new Color(leafR, leafG, leafB));
                int[] xPoints = {gp.tileSize/2, 4, gp.tileSize-4};
                int[] yPoints = {4, gp.tileSize/2 + 10, gp.tileSize/2 + 10};
                g2.fillPolygon(xPoints, yPoints, 3);
                break;

            case "Rock":
                life = 8;
                solidArea = new Rectangle(4, 8, gp.tileSize - 8, gp.tileSize - 12);
                g2.setColor(Color.GRAY);
                g2.fillOval(4, 8, gp.tileSize - 8, gp.tileSize - 12);
                break;
                
            case "Berry Bush":
                life = 1;
                solidArea = new Rectangle(4, 4, gp.tileSize - 8, gp.tileSize - 8);
                // Trunk
                g2.setColor(new Color(101, 67, 33));
                g2.fillRect(gp.tileSize/2 - 6, gp.tileSize - 24, 12, 24);
                // Foliage
                g2.setColor(new Color(34, 139, 34));
                g2.fillOval(10, 10, gp.tileSize - 20, gp.tileSize - 40);
                g2.fillOval(6, 30, 20, 20);
                g2.fillOval(gp.tileSize - 26, 30, 20, 20);
                // Berries
                g2.setColor(new Color(80, 60, 100));
                g2.fillOval(25, 25, 8, 8);
                g2.fillOval(35, 35, 8, 8);
                g2.fillOval(20, 40, 8, 8);
                g2.fillOval(40, 20, 8, 8);
                break;
                
            case "Flint Vein":
                life = 6;
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(4, 8, gp.tileSize - 8, gp.tileSize - 12);
                g2.setColor(Color.BLACK);
                g2.fillOval(20, 20, 10, 10);
                g2.fillOval(40, 50, 12, 8);
                g2.fillOval(50, 25, 8, 8);
                break;
        }
        g2.dispose();
    }

    @Override
    public void getDrops(GamePanel gp) {
        if (name.equals("Tree")) {
            gp.player.dropItem(gp.player.createItem("Wood"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Branch"), worldX, worldY);
        } else if (name.equals("Berry Bush")) {
            gp.player.dropItem(gp.player.createItem("Berry"), worldX, worldY);
        } else if (name.equals("Rock")) {
            int amount = 2 + (int)(Math.random() * 3); // Drop 2 to 4 stones
            for (int i = 0; i < amount; i++) {
                gp.player.dropItem(gp.player.createItem("Stone"), worldX, worldY);
            }
            // 50% chance for Flint
            if (Math.random() < 0.5) gp.player.dropItem(gp.player.createItem("Flint"), worldX, worldY);
        } else if (name.equals("Flint Vein")) {
            gp.player.dropItem(gp.player.createItem("Flint"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Stone"), worldX, worldY);
        }
    }
}