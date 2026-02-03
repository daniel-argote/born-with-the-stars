package tile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import main.GamePanel;

public class TileManager {

    GamePanel gp;
    public Map<Integer, Tile> tileCache; 
    public int[][] mapTileNum;           

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.tileCache = new HashMap<>();
        
        // Initialize map array based on world size
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        loadAllBiomes();
        loadMap("/maps/world_map.txt");
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

                    // COLLISION: Blue Ocean variants (800-1119) are solid
                    boolean hasCollision = false;
                    if (currentId >= 800 && currentId <= 1119) {    
                        hasCollision = true;
                    }  

                    Tile newTile = new Tile(currentId, tileImage, hasCollision);
                    tileCache.put(currentId, newTile);

                    currentId++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("Map file not found: " + filePath);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                String[] numbers = line.split(" ");

                while (col < gp.maxWorldCol && col < numbers.length) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
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

    public void draw(Graphics2D g2) {
        // Keep pixels sharp (no blur)
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        // Disable standard AA for the map to prevent "fuzzy" grass edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Use this to prevent small gaps/lines between tiles when zooming
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // 1. Calculate the new drawing size for tiles
        int zoomedSize = (int)Math.ceil(gp.tileSize * gp.scale);

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;

            // 2. ZOOM-AWARE CAMERA MATH
            // We calculate the distance from player to tile, then scale THAT distance
            int screenX = (int)((worldX - gp.player.worldX) * gp.scale + gp.player.screenX);
            int screenY = (int)((worldY - gp.player.worldY) * gp.scale + gp.player.screenY);

            // 3. OPTIMIZED VIEW FRUSTUM
            // We only draw tiles that are actually visible on the screen at the current zoom
            if (worldX + gp.tileSize > gp.player.worldX - (gp.player.screenX / gp.scale) &&
                worldX - gp.tileSize < gp.player.worldX + (gp.player.screenX / gp.scale) &&
                worldY + gp.tileSize > gp.player.worldY - (gp.player.screenY / gp.scale) &&
                worldY - gp.tileSize < gp.player.worldY + (gp.player.screenY / gp.scale)) {

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
}