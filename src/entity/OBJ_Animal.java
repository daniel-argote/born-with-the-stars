package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;
import main.GamePanel;

public class OBJ_Animal extends Entity {

    GamePanel gp;
    public int size;
    public Color color;
    
    // AI Settings
    public String behavior = "neutral"; // neutral, flee, aggressive
    public int detectionRange = 0;
    public int wanderSpeed = 1;
    public int runSpeed = 2;
    public int agitatedCounter = 0;

    public OBJ_Animal(GamePanel gp, String name, int x, int y) {
        this.gp = gp;
        this.name = name;
        this.worldX = x * gp.tileSize;
        this.worldY = y * gp.tileSize;
        this.type = TYPE_NPC;
        
        // Default hitbox for animals
        this.solidArea = new Rectangle(8, 16, 48, 32);
        this.solidAreaDefaultX = solidArea.x;
        this.solidAreaDefaultY = solidArea.y;
        this.collision = true;

        setAnimalStats();
        
        // Randomize starting direction so they don't all move "down" initially
        Random random = new Random();
        int i = random.nextInt(100) + 1;
        if (i <= 25) direction = "up";
        else if (i <= 50) direction = "down";
        else if (i <= 75) direction = "left";
        else direction = "right";
    }

    public void setAnimalStats() {
        Random random = new Random();
        switch (name) {
            case "Rabbit":
                speed = 2;
                wanderSpeed = 2;
                runSpeed = 4;
                maxLife = 4;
                life = maxLife;
                size = 24 + random.nextInt(9); // 24 to 32
                color = Color.LIGHT_GRAY;
                behavior = "flee";
                detectionRange = 5;
                break;
            case "Beaver":
                speed = 1;
                wanderSpeed = 1;
                runSpeed = 2;
                maxLife = 6;
                life = maxLife;
                size = 40 + random.nextInt(9); // 40 to 48
                color = new Color(139, 69, 19); // Brown
                behavior = "neutral";
                break;
            case "Deer":
                speed = 3;
                wanderSpeed = 2;
                runSpeed = 5; // Faster than player (4)
                maxLife = 10;
                life = maxLife;
                size = 50 + random.nextInt(15); // 50 to 64
                color = new Color(205, 133, 63); // Peru
                behavior = "flee";
                detectionRange = 6;
                break;
            case "Bear":
                speed = 1;
                wanderSpeed = 1;
                runSpeed = 4;
                maxLife = 20;
                life = maxLife;
                size = 70 + random.nextInt(21); // 70 to 90
                color = new Color(30, 30, 30); // Dark Grey
                if (random.nextInt(100) < 20) { // 20% chance to be aggressive
                    behavior = "aggressive";
                } else {
                    behavior = "flee";
                }
                detectionRange = 4;
                break;
        }
        speed = wanderSpeed;
    }

    public void setAction() {
        
        // 1. CHECK DISTANCE TO PLAYER
        int dx = gp.player.worldX - worldX;
        int dy = gp.player.worldY - worldY;
        double dist = Math.sqrt(dx*dx + dy*dy);
        boolean playerInRange = dist < gp.tileSize * detectionRange;

        if (playerInRange && !behavior.equals("neutral")) {
            agitatedCounter = 120; // Stay agitated for 2 seconds (60fps * 2)
        }

        if (agitatedCounter > 0 && !behavior.equals("neutral")) {
            agitatedCounter--;
            speed = runSpeed;
            actionLockCounter++;
            
            // React faster when engaged (every 15 frames = 0.25s)
            if (actionLockCounter > 15) {
                Random random = new Random();
                
                // Rabbits are skittish and dumb: 30% chance to move randomly (panic)
                if (name.equals("Rabbit") && random.nextInt(100) < 30) {
                    int i = random.nextInt(4);
                    if (i == 0) direction = "up";
                    if (i == 1) direction = "down";
                    if (i == 2) direction = "left";
                    if (i == 3) direction = "right";
                } else if (behavior.equals("flee")) {
                    // Move AWAY from player
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) direction = "left"; 
                        else direction = "right";
                    } else {
                        if (dy > 0) direction = "up"; 
                        else direction = "down";
                    }
                }
                else if (behavior.equals("aggressive")) {
                    // Move TOWARDS player
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) direction = "right"; 
                        else direction = "left";
                    } else {
                        if (dy > 0) direction = "down"; 
                        else direction = "up";
                    }
                }
                actionLockCounter = 0;
            }
        } else {
            // RANDOM WANDER (Default)
            speed = wanderSpeed;
            actionLockCounter++;
            if (actionLockCounter == 120) { 
                Random random = new Random();
                int i = random.nextInt(140) + 1; // Increased range for idle chance

                if (i <= 25) direction = "up";
                else if (i <= 50) direction = "down";
                else if (i <= 75) direction = "left";
                else if (i <= 100) direction = "right";
                else direction = "idle"; // ~28% chance to stop/idle

                actionLockCounter = 0;
            }
        }
    }

    @Override
    public void update() {
        setAction();

        // TERRAIN SPEED CHECK
        int col = (worldX + solidArea.x + solidArea.width / 2) / gp.tileSize;
        int row = (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize;

        if (col >= 0 && col < gp.maxWorldCol && row >= 0 && row < gp.maxWorldRow) {
            int tileNum = gp.tileM.mapTileNum[gp.currentMap][col][row];
            
            // Water (800-1119)
            if (tileNum >= 800 && tileNum <= 1119) {
                speed = 1; 
            } 
            // Coastline/Sand (1120-1183)
            else if (tileNum >= 1120 && tileNum <= 1183) {
                if (speed > 1) speed--;
            }
        }

        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        gp.cChecker.checkPlayer(this);

        if (!collisionOn) {
            switch (direction) {
                case "up": worldY -= speed; break;
                case "down": worldY += speed; break;
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }
        super.update(); // Handle invincibility frames
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        double screenOffsetX = gp.screenWidth / 2 - (gp.tileSize * gp.scale) / 2;
        double screenOffsetY = gp.screenHeight / 2 - (gp.tileSize * gp.scale) / 2;
        int screenX = (int)((worldX - gp.player.worldX) * gp.scale + screenOffsetX);
        int screenY = (int)((worldY - gp.player.worldY) * gp.scale + screenOffsetY);

        // Optimization: Only draw if visible on screen
        if (worldX + gp.tileSize * 2 > gp.player.worldX - (gp.player.screenX / gp.scale) && // Use player.screenX for culling approx is fine
            worldX - gp.tileSize * 2 < gp.player.worldX + (gp.player.screenX / gp.scale) &&
            worldY + gp.tileSize * 2 > gp.player.worldY - (gp.player.screenY / gp.scale) &&
            worldY - gp.tileSize * 2 < gp.player.worldY + (gp.player.screenY / gp.scale)) {

            if (invincible) {
                screenX += Math.random() * 10 - 5;
                screenY += Math.random() * 10 - 5;
            }

            g2.setColor(color);
            int zoomedSize = (int)(size * gp.scale);
            
            // Upside down triangle
            // Top-Left, Top-Right, Bottom-Center
            int[] xPoints = {screenX, screenX + zoomedSize, screenX + zoomedSize / 2};
            int[] yPoints = {screenY, screenY, screenY + zoomedSize};
            g2.fillPolygon(xPoints, yPoints, 3);
        }
    }

    @Override
    public void getDrops(GamePanel gp) {
        if (name.equals("Rabbit")) {
            gp.player.dropItem(gp.player.createItem("Rabbit Meat (Raw)"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Rabbit Fur"), worldX, worldY);
        } else if (name.equals("Beaver")) {
            gp.player.dropItem(gp.player.createItem("Beaver Meat (Raw)"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Beaver Fur"), worldX, worldY);
        } else if (name.equals("Deer")) {
            gp.player.dropItem(gp.player.createItem("Venison (Raw)"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Deer Fur"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Bone"), worldX, worldY);
        } else if (name.equals("Bear")) {
            gp.player.dropItem(gp.player.createItem("Bear Meat (Raw)"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Bear Meat (Raw)"), worldX, worldY); // Extra meat
            gp.player.dropItem(gp.player.createItem("Bear Fur"), worldX, worldY);
            gp.player.dropItem(gp.player.createItem("Bone"), worldX, worldY);
        }
    }
}