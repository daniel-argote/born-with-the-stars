package tile;

import java.util.List;

public class TileData {
    public String project_name;
    public int tile_size;
    public List<TilePalette> palettes;

    public static class TilePalette {
        public String biome;
        public String sheet_path;
        public List<TileEntry> tiles;
    }

    public static class TileEntry {
        public int id;
        public String name;
        public boolean collision;
        public String terrain;
    }
}