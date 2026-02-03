package utils;

// Helper class to generate tile data for a specific biome tile sheet
public class TileDataGenerator {
    public static void main(String[] args) {
        String sheetName = "biome_ground";
        int tileSize = 128;
        int gridCount = 8;
        int startingId = 0; // Starting ID for this specific biome

        System.out.println("\"" + sheetName + "\": {");
        System.out.println("  \"sheet_path\": \"res/tiles/" + sheetName + ".png\",");
        System.out.println("  \"tiles\": [");

        for (int row = 0; row < gridCount; row++) {
            for (int col = 0; col < gridCount; col++) {
                int id = startingId + (row * gridCount) + col;
                int x = col * tileSize;
                int y = row * tileSize;

                System.out.printf("    {\"id\": %d, \"name\": \"%s_%d\", \"x\": %d, \"y\": %d, \"collision\": true}%s\n",
                        id, sheetName, id, x, y, (id == startingId + 63 ? "" : ","));
            }
        }

        System.out.println("  ]\n}");
    }
}