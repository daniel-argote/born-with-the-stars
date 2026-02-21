package tile;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import javax.imageio.ImageIO;
import main.GamePanel;

public class TileManager {

    GamePanel gp;
    public Map<Integer, Tile> tileCache; 
    public int[][][] mapTileNum;           
    public boolean[][][] fogMap;
    public int[][][] tileHeightMap;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.tileCache = new HashMap<>();
        
        // Initialize map array based on world size
        mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        fogMap = new boolean[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        tileHeightMap = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];

        // loadAllBiomes(); // Disabled for now
        generateProceduralTiles(); // Use programmatic assets
        loadMap("/maps/world_map.txt", 0);
    }

    private void loadAllBiomes() {
        String[] biomeFiles = {
            "biome_ground.png",                 // ID: 0-63
            "biome_deepOcean_blue.png",         // ID: 1056-1119
            "biome_deepOcean_darkBlue.png",     // ID: 800-863
            "biome_deepOcean_reallyDarkBlue.png",// ID: 864-927
            "biome_deepOcean_greenish.png",     // ID: 928-991
            "biome_deepOcean_greener.png",      // ID: 992-1055
            "biome_coastline.png",              // ID: 1120-1183
            "biome_fog.png"                     // ID: 1184-1247
        };

        int[] startIds = {0, 1056, 800, 864, 928, 992, 1120, 1184};

        for (int i = 0; i < biomeFiles.length; i++) {
            sliceSheet("/tiles/" + biomeFiles[i], startIds[i]);
        }
    }

    private void sliceSheet(String path, int startId) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.out.println("Could not find: " + path);
                return;
            }
            BufferedImage masterSheet = ImageIO.read(is);

            int currentId = startId;
            int tileArtSize = 124; 
            int border = 4;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    
                    int x = (col * 128) + border - 2;
                    int y = (row * 128) + border - 2;

                    int safeX = Math.max(0, Math.min(x, 1024 - tileArtSize));
                    int safeY = Math.max(0, Math.min(y, 1024 - tileArtSize));

                    BufferedImage tileImage = masterSheet.getSubimage(safeX, safeY, tileArtSize, tileArtSize);

                    // COLLISION: Blue Ocean variants (800-1119) are now swimmable (no collision)
                    boolean hasCollision = false;
                    // if (currentId >= 800 && currentId <= 1119) {    
                    //     hasCollision = true;
                    // }  

                    Tile newTile = new Tile(currentId, tileImage, hasCollision);
                    tileCache.put(currentId, newTile);

                    currentId++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateProceduralTiles() {
        // Generate Grass (0-63)
        generateRange(0, 63, new Color(10, 80, 30), "grass", false); // Much darker, richer green
        
        // Generate Deep Ocean variants (800-1119) - Solid
        generateRange(800, 1119, new Color(0, 0, 139), "water", false);
        
        // Generate Coastline (1120-1183)
        generateRange(1120, 1183, new Color(240, 230, 140), "sand", false);
        
        // Generate Fog (1184-1247) - Set collision to TRUE for Tipi walls/Void
        generateRange(1184, 1247, new Color(80, 80, 80), "fog", true);
    }

    private void generateRange(int startId, int endId, Color baseColor, String type, boolean collision) {
        Random random = new Random();
        int size = 128; // Standard tile size

        for (int i = startId; i <= endId; i++) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            // Vary the base color slightly for natural texture
            int r = Math.min(255, Math.max(0, baseColor.getRed() + random.nextInt(20) - 10));
            int g = Math.min(255, Math.max(0, baseColor.getGreen() + random.nextInt(20) - 10));
            int b = Math.min(255, Math.max(0, baseColor.getBlue() + random.nextInt(20) - 10));
            Color tileColor = new Color(r, g, b);

            // Base Fill
            g2.setColor(tileColor);
            g2.fillRect(0, 0, size, size);

            // Add subtle noise for blending/texture
            for (int k = 0; k < 100; k++) {
                g2.setColor(new Color(Math.max(0, tileColor.getRed()-10), Math.max(0, tileColor.getGreen()-10), Math.max(0, tileColor.getBlue()-10), 50));
                int nx = random.nextInt(size);
                int ny = random.nextInt(size);
                g2.fillRect(nx, ny, 2, 2);
            }

            // Add Details
            if (type.equals("grass")) {
                g2.setColor(tileColor.brighter());
                // Random blades
                for (int j = 0; j < 12; j++) {
                    int x = random.nextInt(size);
                    int y = random.nextInt(size);
                    int[] xPoints = {x, x + 5, x - 5};
                    int[] yPoints = {y, y + 15, y + 15};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }
                // Random clumps
                for (int j = 0; j < 5; j++) {
                    g2.fillOval(random.nextInt(size), random.nextInt(size), 6, 6);
                }
            } else if (type.equals("water")) {
                g2.setColor(baseColor.brighter());
                g2.setStroke(new BasicStroke(2));
                for (int j = 0; j < 3; j++) {
                    int x = random.nextInt(size - 20);
                    int y = random.nextInt(size);
                    g2.drawLine(x, y, x + 20, y);
                }
            } else if (type.equals("sand")) {
                g2.setColor(new Color(210, 180, 140)); // Darker sand
                for (int j = 0; j < 20; j++) {
                    int x = random.nextInt(size);
                    int y = random.nextInt(size);
                    g2.fillRect(x, y, 2, 2);
                }
            }

            g2.dispose();

            tileCache.put(i, new Tile(i, img, collision));
        }
    }

    public void loadMap(String filePath, int map) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("Map file not found: " + filePath);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            Random random = new Random();

            while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                String[] numbers = line.split(" ");

                while (col < gp.maxWorldCol && col < numbers.length) {
                    int num = Integer.parseInt(numbers[col]);
                    
                    // Replace placeholder '2' with random grass variant (0-63)
                    if (num == 2) {
                        num = random.nextInt(64);
                    }
                    
                    mapTileNum[map][col][row] = num;
                    col++;
                }
                if (col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateTipiInterior(int map) {
        // 1. Fill with "Void" (Fog or solid color)
        for(int i=0; i<gp.maxWorldCol; i++) {
            for(int j=0; j<gp.maxWorldRow; j++) {
                mapTileNum[map][i][j] = 1184; // Fog ID
                fogMap[map][i][j] = false; // No fog of war inside
            }
        }
        
        // 2. Create a circular room (Grass floor for now)
        int centerX = 25;
        int centerY = 25;
        int radius = 4;
        
        for(int i = centerX - radius; i <= centerX + radius; i++) {
            for(int j = centerY - radius; j <= centerY + radius; j++) {
                if (Math.sqrt(Math.pow(i-centerX, 2) + Math.pow(j-centerY, 2)) < radius) {
                    mapTileNum[map][i][j] = 0; // Grass/Floor
                }
            }
        }
        
        // 3. Add Entrance/Exit tile at bottom
        mapTileNum[map][centerX][centerY + radius] = 0; 
    }

    public void exportMapToConsole() {
        System.out.println("--- MAP DATA START ---");
        for (int row = 0; row < gp.maxWorldRow; row++) {
            // Only exports Map 0
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < gp.maxWorldCol; col++) {
                line.append(mapTileNum[0][col][row]);
                if (col < gp.maxWorldCol - 1) {
                    line.append(" ");
                }
            }
            System.out.println(line.toString());
        }
        System.out.println("--- MAP DATA END ---");
    }

    public void draw(Graphics2D g2) {
        // Keep pixels sharp (no blur)
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        // Disable standard AA for the map to prevent "fuzzy" grass edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Use this to prevent small gaps/lines between tiles when zooming
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // 1. Calculate the new drawing size for tiles
        // Add +1 to overlap tiles slightly and prevent "seams" or black lines between them
        int zoomedSize = (int)Math.ceil(gp.tileSize * gp.scale) + 1;

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[gp.currentMap][worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;

            // 2. ZOOM-AWARE CAMERA MATH
            // We calculate the distance from player to tile, then scale THAT distance
            double screenOffsetX = gp.getWidth() / 2 - (gp.tileSize * gp.scale) / 2;
            double screenOffsetY = gp.getHeight() / 2 - (gp.tileSize * gp.scale) / 2;
            int screenX = (int)((worldX - gp.player.worldX) * gp.scale + screenOffsetX);
            int screenY = (int)((worldY - gp.player.worldY) * gp.scale + screenOffsetY);

            // 3. OPTIMIZED VIEW FRUSTUM
            // We only draw tiles that are actually visible on the screen at the current zoom
            double screenRangeX = gp.getWidth() / 2.0;
            double screenRangeY = gp.getHeight() / 2.0;

            if (worldX + gp.tileSize * 2 > gp.player.worldX - (screenRangeX / gp.scale) &&
                worldX - gp.tileSize * 2 < gp.player.worldX + (screenRangeX / gp.scale) &&
                worldY + gp.tileSize * 2 > gp.player.worldY - (screenRangeY / gp.scale) &&
                worldY - gp.tileSize * 2 < gp.player.worldY + (screenRangeY / gp.scale)) {

                Tile t = tileCache.get(tileNum);
                if (t != null) {
                    g2.drawImage(t.image, screenX, screenY, zoomedSize, zoomedSize, null);
                }
            }

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public void drawFog(Graphics2D g2) {
        int zoomedSize = (int)Math.ceil(gp.tileSize * gp.scale);
        
        // Dark Grey, ~90% opaque (230/255)
        g2.setColor(new Color(80, 80, 80, 230));
        // Dark Grey, ~95% opaque (242/255)
        g2.setColor(new Color(80, 80, 80, 242));

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;

            double screenOffsetX = gp.getWidth() / 2 - (gp.tileSize * gp.scale) / 2;
            double screenOffsetY = gp.getHeight() / 2 - (gp.tileSize * gp.scale) / 2;
            int screenX = (int)((worldX - gp.player.worldX) * gp.scale + screenOffsetX);
            int screenY = (int)((worldY - gp.player.worldY) * gp.scale + screenOffsetY);

            double screenRangeX = gp.getWidth() / 2.0;
            double screenRangeY = gp.getHeight() / 2.0;

            if (worldX + gp.tileSize * 2 > gp.player.worldX - (screenRangeX / gp.scale) &&
                worldX - gp.tileSize * 2 < gp.player.worldX + (screenRangeX / gp.scale) &&
                worldY + gp.tileSize * 2 > gp.player.worldY - (screenRangeY / gp.scale) &&
                worldY - gp.tileSize * 2 < gp.player.worldY + (screenRangeY / gp.scale)) {

                if (fogMap[gp.currentMap][worldCol][worldRow]) {
                    g2.fillRect(screenX, screenY, zoomedSize, zoomedSize);
                }
            }

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public void generateDevMap() {
        for (int i = 0; i < gp.maxWorldCol; i++) {
            for (int j = 0; j < gp.maxWorldRow; j++) {
                mapTileNum[0][i][j] = 0; // Fill with Grass (ID 0)
                fogMap[0][i][j] = false;  // No Fog in Dev Mode
                tileHeightMap[0][i][j] = 0; // Reset height
            }
        }
        
        // Create a raised plateau (Height 1)
        for (int i = 30; i < 40; i++) {
            for (int j = 20; j < 30; j++) {
                tileHeightMap[0][i][j] = 1;
                mapTileNum[0][i][j] = 1120; // Use Sand (Coastline) to visualize the high ground
            }
        }

        // Clear fog around the starting area (50, 50)
        updateFog(gp.tileSize * 50, gp.tileSize * 50);
    }

    public void resetFog(int map, boolean state) {
        for (int i = 0; i < gp.maxWorldCol; i++) {
            for (int j = 0; j < gp.maxWorldRow; j++) {
                fogMap[map][i][j] = state;
            }
        }
    }

    public void updateFog(int worldX, int worldY) {
        updateFog(worldX, worldY, 3);
    }

    public void updateFog(int worldX, int worldY, int radius) {
        int playerCol = worldX / gp.tileSize;
        int playerRow = worldY / gp.tileSize;

        for (int i = playerCol - radius; i <= playerCol + radius; i++) {
            for (int j = playerRow - radius; j <= playerRow + radius; j++) {
                if (i >= 0 && i < gp.maxWorldCol && j >= 0 && j < gp.maxWorldRow) {
                    fogMap[gp.currentMap][i][j] = false;
                }
            }
        }
    }

    public String getTileName(int id) {
        if (id >= 0 && id <= 63) return "Grassland";
        if (id >= 800 && id <= 1119) return "Deep Ocean";
        if (id >= 1120 && id <= 1183) return "Coastline";
        if (id >= 1184 && id <= 1247) return "Fog";
        return "Unknown Terrain";
    }
}