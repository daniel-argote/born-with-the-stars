package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The base class for all moving things in the game world.
 * Holds position, movement, and collision data.
 */
public class Entity {

    // POSITION AND MOVEMENT
    public int worldX, worldY;
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
}