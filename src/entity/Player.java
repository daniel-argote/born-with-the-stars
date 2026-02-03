package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.geom.AffineTransform;
import main.GamePanel;
import main.KeyHandler;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    public double rotationAngle = 0; // In degrees for banking effect

    public final int screenX;
    public final int screenY;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        // Center of the screen
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        // HITBOX: Crucial for CollisionChecker to work
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 32;
        solidArea.height = 32;

        setDefaultValues();
    }

    public void setDefaultValues() {
        // Starting position in the middle of your 50x50 map
        worldX = gp.tileSize * 25; 
        worldY = gp.tileSize * 25;
        speed = 4;
        direction = "down";
    }

    public void update() {
        
        // 1. TERRAIN SENSING
        int centerWorldX = (int)worldX + (gp.tileSize / 2);
        int centerWorldY = (int)worldY + (gp.tileSize / 2);

        int col = centerWorldX / gp.tileSize;
        int row = centerWorldY / gp.tileSize;

        // Check if player is on a "Coastline" tile (ID 1120 to 1183)
        if (col >= 0 && col < gp.maxWorldCol && row >= 0 && row < gp.maxWorldRow) {
            int tileNum = gp.tileM.mapTileNum[col][row];

            if (tileNum >= 1120 && tileNum <= 1183) {
                speed = 2; // Slow speed for sand
            } else {
                speed = 4; // Normal speed
            }
        }
        
        // 2. MOVEMENT & DIRECTION
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            
            if (keyH.upPressed && keyH.leftPressed) direction = "up-left";
            else if (keyH.upPressed && keyH.rightPressed) direction = "up-right";
            else if (keyH.downPressed && keyH.leftPressed) direction = "down-left";
            else if (keyH.downPressed && keyH.rightPressed) direction = "down-right";
            else if (keyH.upPressed) direction = "up";
            else if (keyH.downPressed) direction = "down";
            else if (keyH.leftPressed) direction = "left";
            else if (keyH.rightPressed) direction = "right";

            // Collision Check
            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn) {
                double moveSpeed = speed;
                // Diagonal normalization
                if ((keyH.upPressed || keyH.downPressed) && (keyH.leftPressed || keyH.rightPressed)) {
                    moveSpeed = speed * 0.707;
                }

                if (keyH.upPressed) worldY -= moveSpeed;
                if (keyH.downPressed) worldY += moveSpeed;
                if (keyH.leftPressed) worldX -= moveSpeed;
                if (keyH.rightPressed) worldX += moveSpeed;
            }
        }

        // 3. BANKING LOGIC (The Lean)
        double targetAngle = 0;
        if (keyH.leftPressed) targetAngle = -15; 
        if (keyH.rightPressed) targetAngle = 15;

        // Interpolate (Lerp) toward the target angle for smoothness
        rotationAngle += (targetAngle - rotationAngle) * 0.1;
    }

    public void draw(Graphics2D g2) {

        // 1. DIMENSIONS & CENTERING
        double playerSizeRatio = 0.75; 
        int zoomedSize = (int)(gp.tileSize * gp.scale * playerSizeRatio);
        int x = gp.screenWidth / 2 - (zoomedSize / 2);
        int y = gp.screenHeight / 2 - (zoomedSize / 2);

        // 2. DEFINE POLYGON POINTS BASED ON DIRECTION
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        switch (direction) {
            case "up":
                xPoints = new int[]{x, x + zoomedSize / 2, x + zoomedSize};
                yPoints = new int[]{y + zoomedSize, y, y + zoomedSize};
                break;
            case "down":
                xPoints = new int[]{x, x + zoomedSize / 2, x + zoomedSize};
                yPoints = new int[]{y, y + zoomedSize, y};
                break;
            case "left":
                xPoints = new int[]{x + zoomedSize, x, x + zoomedSize};
                yPoints = new int[]{y, y + zoomedSize / 2, y + zoomedSize};
                break;
            case "right":
                xPoints = new int[]{x, x + zoomedSize, x};
                yPoints = new int[]{y, y + zoomedSize / 2, y + zoomedSize};
                break;
            case "up-left":
                xPoints = new int[]{x + zoomedSize, x, x + zoomedSize / 2};
                yPoints = new int[]{y + zoomedSize / 2, y, y + zoomedSize};
                break;
            case "up-right":
                xPoints = new int[]{x, x + zoomedSize, x + zoomedSize / 2};
                yPoints = new int[]{y + zoomedSize / 2, y, y + zoomedSize};
                break;
            case "down-left":
                xPoints = new int[]{x + zoomedSize, x, x + zoomedSize / 2};
                yPoints = new int[]{y + zoomedSize / 2, y + zoomedSize, y};
                break;
            case "down-right":
                xPoints = new int[]{x, x + zoomedSize, x + zoomedSize / 2};
                yPoints = new int[]{y + zoomedSize / 2, y + zoomedSize, y};
                break;
        }

        // 3. COLOR & GRADIENT SETUP
        Color pearlPink = new Color(255, 240, 245); 
        Color pearlSeafoam = new Color(230, 255, 250); 
        
        GradientPaint pearlGradient = new GradientPaint(
            x, y, pearlPink, 
            x + zoomedSize, y + zoomedSize, pearlSeafoam, 
            true
        );

        // 4. DRAWING & ROTATION
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Save the canvas state before we tilt the player
        AffineTransform oldTransform = g2.getTransform();

        // Apply rotation around screen center
        g2.rotate(Math.toRadians(rotationAngle), gp.screenWidth / 2, gp.screenHeight / 2);

        // Fill Triangle
        g2.setPaint(pearlGradient);
        g2.fillPolygon(xPoints, yPoints, 3);
        
        // Draw Outline
        g2.setColor(new Color(200, 200, 200, 150));
        g2.drawPolygon(xPoints, yPoints, 3);

        // Restore canvas state (so UI/Map aren't tilted)
        g2.setTransform(oldTransform);
    }
}