package tile;

import java.awt.image.BufferedImage;

public class Tile {
    public int id;
    public BufferedImage image;
    public boolean collision = false;

    // This constructor matches your TileManager's sliceSheet logic
    public Tile(int id, BufferedImage image, boolean collision) {
        this.id = id;
        this.image = image;
        this.collision = collision;
    }

    // Helper method for the draw call in TileManager
    public BufferedImage getImage() {
        return image;
    }
}