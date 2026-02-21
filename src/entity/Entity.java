package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import main.GamePanel;

/**
 * The base class for all moving things in the game world.
 * Holds position, movement, and collision data.
 */
public class Entity {

    // POSITION AND MOVEMENT
    public int worldX, worldY;
    public int map = 0; // 0: World Map, 1: Indoor, etc.
    public int speed;

    // SPRITES & ANIMATION
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction = "down"; // Default starting direction
    
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // COLLISION
    // This defines the "solid" part of the character.
    // For 128px tiles, we start with a full 128x128 rectangle.
    public Rectangle solidArea = new Rectangle(0, 0, 128, 128);
    
    // Default coordinates for the solidArea within the 128x128 sprite
    public int solidAreaDefaultX, solidAreaDefaultY;
    
    public boolean collisionOn = false;

    // CHARACTER STATUS
    public int maxLife;
    public int life;

    // OBJECT PROPERTIES
    public String name;
    public boolean collision = false;
    public int type; // 0 = player, 1 = npc, 2 = monster, 3 = obstacle

    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_OBSTACLE = 1;
    public static final int TYPE_PICKUP = 2;
    public static final int TYPE_NPC = 3;

    // VISUAL EFFECTS
    public boolean invincible = false;
    public int invincibleCounter = 0;
    public int actionLockCounter = 0;
    
    // LIGHTING
    public int lightRadius = 0;
    
    // DIALOGUE
    public boolean talkable = false;
    public void speak() {}

    public void getDrops(GamePanel gp) {
        // Default: drop nothing
    }

    public void update() {
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 20) { // Shake for 20 frames (~0.3s)
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        double screenOffsetX = gp.getWidth() / 2 - (gp.tileSize * gp.scale) / 2;
        double screenOffsetY = gp.getHeight() / 2 - (gp.tileSize * gp.scale) / 2;
        int screenX = (int)((worldX - gp.player.worldX) * gp.scale + screenOffsetX);
        int screenY = (int)((worldY - gp.player.worldY) * gp.scale + screenOffsetY);

        double screenRangeX = gp.getWidth() / 2.0;
        double screenRangeY = gp.getHeight() / 2.0;

        // Optimization: Only draw if visible on screen
        if (worldX + gp.tileSize * 2 > gp.player.worldX - (screenRangeX / gp.scale) &&
            worldX - gp.tileSize * 2 < gp.player.worldX + (screenRangeX / gp.scale) &&
            worldY + gp.tileSize * 2 > gp.player.worldY - (screenRangeY / gp.scale) &&
            worldY - gp.tileSize * 2 < gp.player.worldY + (screenRangeY / gp.scale)) {
            
            if (invincible) {
                // Shake effect
                screenX += Math.random() * 10 - 5;
                screenY += Math.random() * 10 - 5;
            }
            int zoomedSize = (int)(gp.tileSize * gp.scale);
            g2.drawImage(down1, screenX, screenY, zoomedSize, zoomedSize, null);
        }
    }
}