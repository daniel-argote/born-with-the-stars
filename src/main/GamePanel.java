package main;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Toolkit;
import entity.Player;
import tile.TileManager;
import ui.UI;

public class GamePanel extends JPanel implements Runnable {  
    // SCREEN SETTINGS
    final int originalTileSize = 128; // Your original asset size
    public final int tileSize = 64;   // The scaled-down size you wanted

    // To get a 1280x720 window:
    public final int maxScreenCol = 30; 
    public final int maxScreenRow = 17; 
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // WORLD SETTINGS (Adjust these to match your world_map.txt)
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;

    // FPS
    int FPS = 60;

    // SYSTEM
    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler();
    public CollisionChecker cChecker = new CollisionChecker(this);
    Thread gameThread;

    // Zoom Functionalilty
    public double scale = 1.0; 
    public final double MIN_SCALE = 0.5;
    public final double MAX_SCALE = 4.0;

    // ENTITY AND OBJECT
    public Player player = new Player(this, keyH);
    
    // Initiate UI
    public UI ui = new UI(this);

    public GamePanel() {
        ui = new UI(this);
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK); // Keeps those seams invisible
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.addMouseWheelListener(e -> {
            if (keyH.zPressed) {                                       // Only zoom if 'Z' is held down
                if (e.getWheelRotation() < 0) {
                    scale = Math.min(MAX_SCALE, scale + 0.1);
                } else {
                    scale = Math.max(MIN_SCALE, scale - 0.1);
                }
            } else {
                // This is where your future "Item Scrolling" code will go!
            }
        });
        try {
        // 1. Load the PNG asset
        BufferedImage cursorImg = ImageIO.read(getClass().getResourceAsStream("/player/cursor_arrow.png"));
        
        // 2. Create the Cursor
        // Point(0,0) makes the very tip of the arrowhead the "clickable" spot
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "Arrowhead Cursor");
            
        this.setCursor(customCursor);
        
    } catch (Exception e) {
        // FALLBACK: Use crosshair if the file is missing or broken
        System.out.println("Cursor image not found! Falling back to Crosshair.");
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        player.update();
    }

    public void paintComponent(Graphics g) {        
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Get current window size (Dynamic)
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        // Calculate dynamic center
        int currentScreenX = currentWidth / 2 - (tileSize / 2);
        int currentScreenY = currentHeight / 2 - (tileSize / 2);

        // DRAW TILES
        tileM.draw(g2);

        // DRAW PLAYER
        player.draw(g2);

        // In paintComponent(Graphics g), add this at the VERY end
        // You want the UI to be drawn ON TOP of the map and player
        ui.draw(g2);

        g2.dispose();
    }
}