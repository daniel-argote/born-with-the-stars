package main;

import entity.Entity;
import entity.Player;

public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {

        // 1. Calculate the hitbox edges in the world (as pixels)
        int entityLeftWorldX = (int)entity.worldX + entity.solidArea.x;
        int entityRightWorldX = (int)entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = (int)entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = (int)entity.worldY + entity.solidArea.y + entity.solidArea.height;

        // 2. Convert those pixel coordinates into Map Columns and Rows
        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;
        
        // HEIGHT CHECK SETUP
        // Calculate the height of the tile the entity is currently standing on (center point)
        int currCol = (entity.worldX + entity.solidArea.x + entity.solidArea.width/2) / gp.tileSize;
        int currRow = (entity.worldY + entity.solidArea.y + entity.solidArea.height/2) / gp.tileSize;
        int currentHeight = 0;
        if(currCol >= 0 && currCol < gp.maxWorldCol && currRow >= 0 && currRow < gp.maxWorldRow) {
            currentHeight = gp.tileM.tileHeightMap[gp.currentMap][currCol][currRow];
        }

        // 3. HARD BOUNDARY CHECK: Prevent going off the map (0-49 for a 50x50 map)
        if (entityLeftCol < 0 || entityRightCol >= gp.maxWorldCol || 
            entityTopRow < 0 || entityBottomRow >= gp.maxWorldRow) {
            
            entity.collisionOn = true;
            return; // Exit immediately so we don't crash on the tile check below
        }

        // 4. TILE COLLISION CHECK based on direction
        switch (entity.direction) {
            case "up":
            case "up-left":
            case "up-right":
                entityTopRow = (entityTopWorldY - (int)entity.speed) / gp.tileSize;
                // Double check top boundary for the prediction
                if (entityTopRow < 0) { entity.collisionOn = true; break; }
                
                tileNum1 = gp.tileM.mapTileNum[gp.currentMap][entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[gp.currentMap][entityRightCol][entityTopRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                checkElevation(entity, entityLeftCol, entityTopRow, entityRightCol, entityTopRow, currentHeight);
                break;

            case "down":
            case "down-left":
            case "down-right":
                entityBottomRow = (entityBottomWorldY + (int)entity.speed) / gp.tileSize;
                if (entityBottomRow >= gp.maxWorldRow) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[gp.currentMap][entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[gp.currentMap][entityRightCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                checkElevation(entity, entityLeftCol, entityBottomRow, entityRightCol, entityBottomRow, currentHeight);
                break;

            case "left":
                entityLeftCol = (entityLeftWorldX - (int)entity.speed) / gp.tileSize;
                if (entityLeftCol < 0) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[gp.currentMap][entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[gp.currentMap][entityLeftCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                checkElevation(entity, entityLeftCol, entityTopRow, entityLeftCol, entityBottomRow, currentHeight);
                break;

            case "right":
                entityRightCol = (entityRightWorldX + (int)entity.speed) / gp.tileSize;
                if (entityRightCol >= gp.maxWorldCol) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[gp.currentMap][entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[gp.currentMap][entityRightCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                checkElevation(entity, entityRightCol, entityTopRow, entityRightCol, entityBottomRow, currentHeight);
                break;
        }
    }
    
    private void checkElevation(Entity entity, int col1, int row1, int col2, int row2, int currentHeight) {
        if (entity.collisionOn) return; // Already hit a wall

        int h1 = gp.tileM.tileHeightMap[gp.currentMap][col1][row1];
        int h2 = gp.tileM.tileHeightMap[gp.currentMap][col2][row2];
        int targetHeight = Math.max(h1, h2);

        if (targetHeight > currentHeight) {
            double entityZ = 0;
            if (entity instanceof Player) {
                entityZ = ((Player)entity).z;
            }
            // 40 is the visual height of one level. If jump is lower than height diff, block.
            if (entityZ < (targetHeight - currentHeight) * 40) {
                entity.collisionOn = true;
            }
        }
    }

    public int checkObject(Entity entity, boolean player) {
        int index = 999;

        for (int i = 0; i < gp.obj.size(); i++) {
            if (gp.obj.get(i) != null) {
                // Only check objects on the same map
                if (gp.obj.get(i).map != gp.currentMap) continue;

                // Get entity's solid area position
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                // Get object's solid area position
                gp.obj.get(i).solidArea.x = gp.obj.get(i).worldX + gp.obj.get(i).solidArea.x;
                gp.obj.get(i).solidArea.y = gp.obj.get(i).worldY + gp.obj.get(i).solidArea.y;

                switch(entity.direction) {
                    case "up": entity.solidArea.y -= entity.speed; break;
                    case "down": entity.solidArea.y += entity.speed; break;
                    case "left": entity.solidArea.x -= entity.speed; break;
                    case "right": entity.solidArea.x += entity.speed; break;
                    case "up-left": 
                        entity.solidArea.y -= entity.speed; 
                        entity.solidArea.x -= entity.speed; 
                        break;
                    case "up-right": 
                        entity.solidArea.y -= entity.speed; 
                        entity.solidArea.x += entity.speed; 
                        break;
                    case "down-left": 
                        entity.solidArea.y += entity.speed; 
                        entity.solidArea.x -= entity.speed; 
                        break;
                    case "down-right": 
                        entity.solidArea.y += entity.speed; 
                        entity.solidArea.x += entity.speed; 
                        break;
                }

                if (entity.solidArea.intersects(gp.obj.get(i).solidArea)) {
                    if (gp.obj.get(i) != entity) {
                        if (gp.obj.get(i).collision) {
                            entity.collisionOn = true;
                        }
                        if (player) {
                            index = i;
                        }
                    }
                }

                // Reset
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                gp.obj.get(i).solidArea.x = gp.obj.get(i).solidAreaDefaultX;
                gp.obj.get(i).solidArea.y = gp.obj.get(i).solidAreaDefaultY;
            }
        }
        return index;
    }

    public boolean checkPlayer(Entity entity) {
        boolean contactPlayer = false;

        // Get entity's solid area position
        entity.solidArea.x = entity.worldX + entity.solidArea.x;
        entity.solidArea.y = entity.worldY + entity.solidArea.y;

        // Get player's solid area position
        gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;

        switch(entity.direction) {
            case "up": entity.solidArea.y -= entity.speed; break;
            case "down": entity.solidArea.y += entity.speed; break;
            case "left": entity.solidArea.x -= entity.speed; break;
            case "right": entity.solidArea.x += entity.speed; break;
            case "up-left": 
                entity.solidArea.y -= entity.speed; 
                entity.solidArea.x -= entity.speed; 
                break;
            case "up-right": 
                entity.solidArea.y -= entity.speed; 
                entity.solidArea.x += entity.speed; 
                break;
            case "down-left": 
                entity.solidArea.y += entity.speed; 
                entity.solidArea.x -= entity.speed; 
                break;
            case "down-right": 
                entity.solidArea.y += entity.speed; 
                entity.solidArea.x += entity.speed; 
                break;
        }

        if (entity.solidArea.intersects(gp.player.solidArea)) {
            entity.collisionOn = true;
            contactPlayer = true;
        }

        // Reset
        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        gp.player.solidArea.x = gp.player.solidAreaDefaultX;
        gp.player.solidArea.y = gp.player.solidAreaDefaultY;

        return contactPlayer;
    }
}