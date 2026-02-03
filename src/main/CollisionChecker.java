package main;

import entity.Entity;

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
                
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                break;

            case "down":
            case "down-left":
            case "down-right":
                entityBottomRow = (entityBottomWorldY + (int)entity.speed) / gp.tileSize;
                if (entityBottomRow >= gp.maxWorldRow) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                break;

            case "left":
                entityLeftCol = (entityLeftWorldX - (int)entity.speed) / gp.tileSize;
                if (entityLeftCol < 0) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                break;

            case "right":
                entityRightCol = (entityRightWorldX + (int)entity.speed) / gp.tileSize;
                if (entityRightCol >= gp.maxWorldCol) { entity.collisionOn = true; break; }

                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tileCache.get(tileNum1).collision || gp.tileM.tileCache.get(tileNum2).collision) {
                    entity.collisionOn = true;
                }
                break;
        }
    }
}