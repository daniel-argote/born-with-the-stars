package main;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Toolkit;
import entity.*;
import tile.TileManager;
import ui.UI;
import main.PathFinder;

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
    public final int maxWorldCol = 100;
    public final int maxWorldRow = 100;
    public final int maxMap = 10;
    public int currentMap = 0;
    // For map transitions
    public int returnMap = 0, returnX = 0, returnY = 0;

    // FPS
    int FPS = 60;

    // SYSTEM
    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH;
    public CollisionChecker cChecker = new CollisionChecker(this);
    public PathFinder pFinder = new PathFinder(this);
    public EnvironmentManager environmentM = new EnvironmentManager(this);
    public QuestManager questM = new QuestManager(this);
    
    // SOUND
    public Sound music = new Sound();
    public Sound se = new Sound();
    
    Thread gameThread;

    // Zoom Functionalilty
    public double scale = 1.0; 
    public final double MIN_SCALE = 0.5;
    public final double MAX_SCALE = 4.0;

    // ENTITY AND OBJECT
    public Player player = new Player(this, keyH);
    public Entity currentSpeaker;
    public ArrayList<Entity> obj = new ArrayList<>();
    public ArrayList<Entity> projectiles = new ArrayList<>();
    public ArrayList<Entity> particleList = new ArrayList<>();
    
    // Initiate UI
    public UI ui = new UI(this);

    // GAME STATE
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int optionsState = 3;
    public final int craftingState = 4;
    public final int gameOverState = 5;
    public final int introState = 6;
    public final int saveState = 7;
    public final int loadState = 8;
    public final int saveNamingState = 9;
    public final int questState = 10;
    public final int dialogueState = 11;
    public final int tradeState = 12;
    public final int dreamState = 13;
    public final int skillState = 14;
    public int gameState = playState;

    public boolean debug = true; // Debug mode flag
    // MOUSE INPUT & TOOLTIP
    public int mouseX, mouseY;
    public int hoverCounter = 0;
    public String tooltipText = ""; // Text for the tooltip

    // FULL SCREEN
    public boolean fullScreenOn = false;

    // RESPAWN SYSTEM
    public class RespawnEntry {
        public int x, y;
        public String name;
        public long respawnTime;
        public RespawnEntry(int x, int y, String name, long time) {
            this.x = x; this.y = y; this.name = name; this.respawnTime = time;
        }
    }
    public ArrayList<RespawnEntry> respawnList = new ArrayList<>();

    public GamePanel() {
        gameState = titleState;
        this.keyH = new KeyHandler(this); // Pass the GamePanel instance to KeyHandler
        this.player = new Player(this, keyH); // Now you can safely create the Player with the KeyHandler
        this.ui = new UI(this);
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK); // Keeps those seams invisible
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                keyH.resetKeys();
            }
        });
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                hoverCounter = 0; // Reset timer on move

                if (gameState == titleState) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (ui.menuNewGameRect.contains(mx, my)) {
                        ui.commandNum = 0;
                    } else if (ui.menuLoadGameRect.contains(mx, my)) {
                        ui.commandNum = 1;
                    } else if (ui.menuTutorialRect.contains(mx, my)) {
                        ui.commandNum = 2;
                    } else if (ui.menuDevModeRect.contains(mx, my)) {
                        ui.commandNum = 3;
                    }
                } else if (gameState == pauseState) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (ui.pauseResumeRect.contains(mx, my)) {
                        ui.commandNum = 0;
                    } else if (ui.pauseSettingsRect.contains(mx, my)) {
                        ui.commandNum = 3;
                    } else if (ui.pauseSaveRect.contains(mx, my)) {
                        ui.commandNum = 1;
                    } else if (ui.pauseLoadRect.contains(mx, my)) {
                        ui.commandNum = 2;
                    } else if (ui.pauseQuitRect.contains(mx, my)) {
                        ui.commandNum = 4;
                    }
                } else if (gameState == craftingState) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (ui.craftArrowRect.contains(mx, my)) {
                        ui.commandNum = 0;
                    } else if (ui.craftPickaxeRect.contains(mx, my)) {
                        ui.commandNum = 1;
                    } else if (ui.craftFirePitRect.contains(mx, my)) {
                        ui.commandNum = 2;
                    }
                } else if (gameState == gameOverState) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (ui.gameOverLoadRect.contains(mx, my)) {
                        ui.commandNum = 0;
                    } else if (ui.gameOverSacrificeRect.contains(mx, my)) {
                        ui.commandNum = 1;
                    } else if (ui.gameOverQuitRect.contains(mx, my)) {
                        ui.commandNum = 2;
                    }
                } else if (gameState == optionsState) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (ui.optionsMusicVolumeRect.contains(mx, my)) {
                        ui.commandNum = 0;
                    } else if (ui.optionsSEVolumeRect.contains(mx, my)) {
                        ui.commandNum = 1;
                    } else if (ui.optionsAutoCollectRect.contains(mx, my)) {
                        ui.commandNum = 2;
                    } else if (ui.optionsShowTimerRect.contains(mx, my)) {
                        ui.commandNum = 3;
                    } else if (ui.optionsBackRect.contains(mx, my)) { 
                        ui.commandNum = 4;
                    }
                } else if (gameState == skillState) {
                    // Simple hover check for skill tree could go here
                    // For now we rely on keyboard navigation or simple clicks
                }
            }
        });
        this.addMouseWheelListener(e -> {
            if (keyH.zPressed) {                                       // Only zoom if 'Z' is held down
                if (e.getWheelRotation() < 0) {
                    scale = Math.min(MAX_SCALE, scale + 0.1);
                } else {
                    scale = Math.max(MIN_SCALE, scale - 0.1);
                }
            } else {
                // Inventory Scrolling
                if (gameState == playState) {
                    int slotSize = tileSize;
                    int frameX = getWidth() / 2 - (slotSize * 5);
                    int frameY = getHeight() - (slotSize + 20);
                    
                    // Check Hotbar
                    for(int i = 0; i < 10; i++) {
                        int x = frameX + (i * slotSize);
                        int y = frameY;
                        if(mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                            if (i < player.inventorySlots.size()) {
                                player.inventorySlots.get(i).selectedIndex += (e.getWheelRotation() > 0) ? 1 : -1;
                                // Bounds check handled in getSelectedItem() or we can do it here
                                InventorySlot slot = player.inventorySlots.get(i);
                                if (slot.selectedIndex < 0) slot.selectedIndex = slot.items.size() - 1;
                                if (slot.selectedIndex >= slot.items.size()) slot.selectedIndex = 0;
                                
                                // Auto-equip weapons/tools when scrolling
                                Item newItem = slot.getSelectedItem();
                                if (newItem != null && (newItem.type == Item.TYPE_SWORD || newItem.type == Item.TYPE_AXE || 
                                    newItem.type == Item.TYPE_BOW || newItem.type == Item.TYPE_PICKAXE)) {
                                    player.currentWeapon = newItem;
                                    System.out.println("Equipped: " + newItem.name);
                                }
                            }
                        }
                    }
                }
            }
        });
        
        // MOUSE CLICK LISTENER (For Inventory)
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                
                if (gameState == titleState) {
                    if (ui.menuNewGameRect.contains(mx, my)) {
                        System.out.println("NEW GAME");
                        gameState = introState;
                        ui.resetIntro();
                        player.spawnX = tileSize * 25;
                        player.spawnY = tileSize * 25;
                        player.setDefaultValues();
                        
                        // Enable Fog for New Game
                        tileM.resetFog(0, true);
                        
                        setupGame();
                        environmentM.setup();
                        tileM.updateFog(player.spawnX, player.spawnY, 12); // Large area for start
                    } else if (ui.menuLoadGameRect.contains(mx, my)) {
                        System.out.println("Load");
                        gameState = loadState;
                        ui.updateSaveFileList();
                    } else if (ui.menuTutorialRect.contains(mx, my)) {
                        System.out.println("Tutorial");
                        ui.showMessage("Tutorial coming soon!");
                    } else if (ui.menuDevModeRect.contains(mx, my)) {
                        System.out.println("DevMode");
                        gameState = playState;
                        player.spawnX = tileSize * 50; // Middle of 100x100 map
                        player.spawnY = tileSize * 50;
                        player.setDefaultValues();
                        player.ageScale = 1.0; // Adult size for Dev Mode
                        tileM.generateDevMap();
                        // Ensure we are on the main map
                        currentMap = 0;
                        setupGame();
                        environmentM.setup();
                    }
                    return;
                } else if (gameState == pauseState) {
                    if (ui.pauseResumeRect.contains(mx, my)) {
                        gameState = playState;
                    } else if (ui.pauseSaveRect.contains(mx, my)) {
                        gameState = saveState;
                        ui.updateSaveFileList();
                    } else if (ui.pauseLoadRect.contains(mx, my)) {
                        gameState = loadState;
                        ui.updateSaveFileList();
                    } else if (ui.pauseSettingsRect.contains(mx, my)) {
                        gameState = optionsState;
                        ui.commandNum = 0;
                    } else if (ui.pauseQuitRect.contains(mx, my)) {
                        gameState = titleState;
                        ui.commandNum = 0;
                    }
                    return;
                } else if (gameState == craftingState) {
                    if (ui.craftArrowRect.contains(mx, my)) {
                        player.craftItem("Arrow");
                    } else if (ui.craftPickaxeRect.contains(mx, my)) {
                        player.craftItem("Pickaxe");
                    } else if (ui.craftFirePitRect.contains(mx, my)) {
                        player.craftItem("Fire Pit");
                    }
                    return;
                } else if (gameState == gameOverState) {
                    if (ui.gameOverLoadRect.contains(mx, my)) {
                        if (loadGame()) {
                            gameState = playState;
                        } else {
                            gameState = playState;
                            player.setDefaultValues();
                            setupGame();
                        }
                    } else if (ui.gameOverSacrificeRect.contains(mx, my)) {
                        if (!player.inventorySlots.isEmpty()) {
                            int slotIndex = (int)(Math.random() * player.inventorySlots.size());
                            InventorySlot slot = player.inventorySlots.get(slotIndex);
                            int itemIndex = (int)(Math.random() * slot.items.size());
                            Item item = slot.items.get(itemIndex);
                            String itemName = item.name;
                            
                            slot.remove(item);
                            if (slot.items.isEmpty()) {
                                player.inventorySlots.remove(slotIndex);
                            }
                            
                            player.life = player.maxLife / 2;
                            player.energy = player.maxEnergy / 2;
                            gameState = playState;
                            ui.showMessage("Sacrificed " + itemName + "!");
                        } else {
                            ui.showMessage("Nothing to sacrifice!");
                        }
                    } else if (ui.gameOverQuitRect.contains(mx, my)) {
                        gameState = titleState;
                        ui.commandNum = 0;
                    }
                    return;
                } else if (gameState == optionsState) {
                    if (ui.optionsAutoCollectRect.contains(mx, my) && ui.commandNum == 2) {
                        player.autoCollect = !player.autoCollect;
                    } else if (ui.optionsShowTimerRect.contains(mx, my) && ui.commandNum == 3) {
                        ui.showStatusTimer = !ui.showStatusTimer;
                    } else if (ui.optionsBackRect.contains(mx, my) && ui.commandNum == 4) {
                        gameState = pauseState;
                        ui.commandNum = 3; // Highlight Settings
                    }
                    return;
                } else if (gameState == skillState) {
                    // Click to unlock logic could go here
                    if (ui.commandNum == 0) {
                        player.skill.unlock("Magnet");
                    }
                    return;
                } else if (gameState == loadState) {
                    // Check if clicked on a file slot
                    int slotHeight = tileSize;
                    int startY = screenHeight / 2 - (slotHeight * 2);
                    
                    for (int i = 0; i < ui.saveFiles.size(); i++) {
                        int y = startY + (i * slotHeight);
                        // Simple bounds check for the list item
                        if (mx >= screenWidth/2 - 200 && mx <= screenWidth/2 + 200 &&
                            my >= y - 20 && my <= y + 20) {
                            if (loadGame(ui.saveFiles.get(i))) {
                                gameState = playState;
                            }
                        }
                    }
                    return;
                } else if (gameState == saveState) {
                    // Check if clicked on a file slot
                    int slotHeight = tileSize;
                    int startY = screenHeight / 2 - (slotHeight * 2);
                    
                    // "Create New" is the first option (index 0)
                    // Existing files are indices 1 to size
                    int totalOptions = ui.saveFiles.size() + 1;

                    for (int i = 0; i < totalOptions; i++) {
                        int y = startY + (i * slotHeight);
                        if (mx >= screenWidth/2 - 200 && mx <= screenWidth/2 + 200 &&
                            my >= y - 20 && my <= y + 20) {
                            if (i == 0) { // Create New
                                gameState = saveNamingState;
                                ui.tempSaveName = "";
                            } else { // Overwrite
                                saveGame(ui.saveFiles.get(i - 1));
                                gameState = playState;
                            }
                        }
                    }
                    return;
                }

                // CLICK TO WALK (If T is held)
                if (keyH.tPressed) {
                    double screenOffsetX = getWidth() / 2 - (tileSize * scale) / 2;
                    double screenOffsetY = getHeight() / 2 - (tileSize * scale) / 2;
                    double worldX = ((mx - screenOffsetX) / scale) + player.worldX;
                    double worldY = ((my - screenOffsetY) / scale) + player.worldY;
                    player.setPath((int)worldX, (int)worldY);
                    return; // Skip inventory check if we are moving
                }

                // CLICK TO ATTACK / INTERACT
                // Translate screen click to world coordinates
                double screenOffsetX = getWidth() / 2 - (tileSize * scale) / 2;
                double screenOffsetY = getHeight() / 2 - (tileSize * scale) / 2;
                double worldX = ((mx - screenOffsetX) / scale) + player.worldX;
                double worldY = ((my - screenOffsetY) / scale) + player.worldY;

                for (int i = obj.size() - 1; i >= 0; i--) {
                    Entity entity = obj.get(i);
                    if (entity != null) {
                        // Check if click is within the entity's tile area
                        if (worldX >= entity.worldX && worldX < entity.worldX + tileSize &&
                            worldY >= entity.worldY && worldY < entity.worldY + tileSize) {
                            
                            if (entity instanceof OBJ_DroppedItem) {
                                double dist = Math.sqrt(Math.pow(player.worldX - entity.worldX, 2) + Math.pow(player.worldY - entity.worldY, 2));
                                if (dist < tileSize * 3) {
                                    if (player.pickUpItem(((OBJ_DroppedItem)entity).item)) {
                                        playSE(5);
                                        obj.remove(i);
                                        ui.showMessage("Picked up " + ((OBJ_DroppedItem)entity).item.name);
                                    } else {
                                        ui.showMessage("Inventory Full!");
                                    }
                                } else {
                                    ui.showMessage("Too far!");
                                }
                            } else {
                                player.attack(entity);
                            }
                            break; // Stop after interacting with one entity
                        }
                    }
                }

                // Calculate Inventory Position (Same math as UI.java)
                int slotSize = tileSize;
                int frameX = getWidth() / 2 - (slotSize * 5);
                int frameY = getHeight() - (slotSize + 20);
                
                // 1. Check Hotbar (Slots 0-9)
                for(int i = 0; i < 10; i++) {
                    int x = frameX + (i * slotSize);
                    int y = frameY;
                    if(mx >= x && mx < x + slotSize && my >= y && my < y + slotSize) {
                        player.selectItem(i);
                    }
                }
                
                // 2. Check Expanded Inventory (Slots 10+)
                if(ui.inventoryOn) {
                    for(int i = 10; i < player.inventorySlots.size(); i++) {
                        int col = i % 10;
                        int row = i / 10;
                        int x = frameX + (col * slotSize);
                        int y = frameY - (row * slotSize);
                        
                        if(mx >= x && mx < x + slotSize && my >= y && my < y + slotSize) {
                            player.selectItem(i);
                        }
                    }
                }
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
    environmentM.setup();
    }

    public void setupGame() {
        obj.clear();
        currentMap = 0;
        
        // Generate Interior Map (Map 1)
        tileM.generateTipiInterior(1);

        // Add test trees around the starting area (25, 25)
        obj.add(new OBJ_Environment(this, 23, 21, "Tree"));
        obj.add(new OBJ_Environment(this, 23, 22, "Tree"));
        obj.add(new OBJ_Environment(this, 24, 22, "Tree"));
        obj.add(new OBJ_Environment(this, 27, 20, "Tree"));
        obj.add(new OBJ_Environment(this, 28, 20, "Tree"));
        obj.add(new OBJ_Environment(this, 29, 20, "Tree"));
        
        // Scatter more trees in the starting area for atmosphere
        for (int i = 0; i < 12; i++) {
            int x = 20 + (int)(Math.random() * 15);
            int y = 20 + (int)(Math.random() * 15);
            // Avoid spawning directly on player (25,25)
            if (Math.abs(x - 25) > 2 || Math.abs(y - 25) > 2) {
                obj.add(new OBJ_Environment(this, x, y, "Tree"));
            }
        }

        // Dense patch of trees nearby
        for (int i = 0; i < 15; i++) {
            obj.add(new OBJ_Environment(this, 30 + (i % 3), 25 + (i / 3), "Tree"));
        }
        // More bushes
        obj.add(new OBJ_Environment(this, 22, 25, "Berry Bush"));
        obj.add(new OBJ_Environment(this, 23, 26, "Berry Bush"));
        obj.add(new OBJ_Environment(this, 26, 24, "Berry Bush"));

        // Add Wild Animals
        obj.add(new OBJ_Animal(this, "Rabbit", 26, 22));
        obj.add(new OBJ_Animal(this, "Beaver", 22, 23));
        obj.add(new OBJ_Animal(this, "Deer", 24, 24));
        obj.add(new OBJ_Animal(this, "Bear", 28, 24));

        // Add trees near Dev Mode start (50, 50)
        obj.add(new OBJ_Environment(this, 49, 48, "Tree"));
        obj.add(new OBJ_Environment(this, 51, 48, "Tree"));
        obj.add(new OBJ_Environment(this, 48, 50, "Tree"));
        obj.add(new OBJ_Environment(this, 52, 50, "Tree"));
        obj.add(new OBJ_Environment(this, 50, 52, "Tree"));

        // Add animals near Dev Mode start (50, 50)
        obj.add(new OBJ_Animal(this, "Rabbit", 50, 49));
        obj.add(new OBJ_Animal(this, "Deer", 53, 51));
        obj.add(new OBJ_Animal(this, "Bear", 47, 52));
        obj.add(new OBJ_Animal(this, "Beaver", 51, 46));

        // Add Bushes
        obj.add(new OBJ_Environment(this, 54, 48, "Berry Bush"));
        obj.add(new OBJ_Environment(this, 55, 49, "Berry Bush"));
        obj.add(new OBJ_Environment(this, 46, 51, "Berry Bush"));

        // Add Rocks
        obj.add(new OBJ_Environment(this, 55, 52, "Rock"));
        obj.add(new OBJ_Environment(this, 45, 48, "Rock"));
        
        // Add Flint Veins
        obj.add(new OBJ_Environment(this, 56, 52, "Flint Vein"));
        obj.add(new OBJ_Environment(this, 44, 48, "Flint Vein"));

        // Add loose Stone (so player can craft first pickaxe)
        obj.add(new OBJ_DroppedItem(this, player.createItem("Stone"), 50 * tileSize, 51 * tileSize));
        obj.add(new OBJ_DroppedItem(this, player.createItem("Stone"), 51 * tileSize, 51 * tileSize));
        obj.add(new OBJ_DroppedItem(this, player.createItem("Stone"), 50 * tileSize, 52 * tileSize));
        
        // Add Quest Giver NPC
        obj.add(new OBJ_NPC(this, "Old Man", 25, 23));
        
        // Add Crafting Table
        obj.add(new OBJ_CraftingTable(this, 26, 23));
        
        // Add Native Village (Tipis) - Randomly spawned around a center
        int villageX = 40;
        int villageY = 40;
        obj.add(new OBJ_FirePit(this, villageX, villageY)); // Central fire

        for (int i = 0; i < 6; i++) {
            // Random position within 6 tiles of center
            int x = villageX + (int)(Math.random() * 12) - 6;
            int y = villageY + (int)(Math.random() * 12) - 6;
            
            // Avoid spawning directly on the fire
            if (Math.abs(x - villageX) < 2 && Math.abs(y - villageY) < 2) continue;

            obj.add(new OBJ_Tipi(this, x, y));
        }

        // Add Village Chief (Near the fire)
        obj.add(new OBJ_NPC(this, "Chief", villageX, villageY + 2));
        
        // Add an exit trigger inside the Tipi (Map 1)
        // We'll place it at 25, 28 (matching the door in generateTipiInterior)
        // We can use a generic object or just handle it via tile interaction, but let's put a "mat" there

        // playMusic(0); // Uncomment to play theme music on start
        
        environmentM.setup();
        
        // STARTING QUEST
        questM.questList.clear();
        Quest q = new Quest("Gather Wood", "Collect 3 pieces of Wood.", Quest.TYPE_COLLECT, "Wood", 3);
        Item reward = player.createItem("Potion");
        q.rewardItem = reward;
        questM.addQuest(q);
    }

    public void enterTipi(int tipiX, int tipiY) {
        // Save current position
        returnMap = currentMap;
        returnX = tipiX; // Return to the Tipi's door
        returnY = tipiY;
        
        // Teleport to Interior
        currentMap = 1;
        player.worldX = tileSize * 25; // Center of the room
        player.worldY = tileSize * 25;
        
        ui.showMessage("Entered Tipi");
    }

    public void returnToWorld() {
        currentMap = returnMap;
        player.worldX = returnX;
        player.worldY = returnY;
        ui.showMessage("Left Tipi");
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void toggleFullScreen() {
        fullScreenOn = !fullScreenOn;
        System.out.println("Fullscreen toggled: " + fullScreenOn);
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
        if (gameState == playState) {
            player.update();
            ui.update();
            environmentM.update();
            
            // Update Objects (Animation & Death)
            for (int i = 0; i < obj.size(); i++) {
                if (obj.get(i) != null) {
                    if (obj.get(i).map == currentMap) {
                        obj.get(i).update();
                        if (obj.get(i).life <= 0) {
                            obj.remove(i);
                            i--;
                        }
                    }
                }
            }
            
            // Update Projectiles
            for (int i = 0; i < projectiles.size(); i++) {
                if (projectiles.get(i) != null) {
                    if (projectiles.get(i).map == currentMap) {
                        projectiles.get(i).update();
                    }
                }
            }
            
            // Update Particles
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    if (particleList.get(i).map == currentMap) {
                        particleList.get(i).update();
                    }
                }
            }

            // RESPAWN CHECK
            long currentTime = System.currentTimeMillis();
            Iterator<RespawnEntry> iter = respawnList.iterator();
            while(iter.hasNext()) {
                RespawnEntry entry = iter.next();
                if(currentTime > entry.respawnTime) {
                    // Respawn logic based on name
                    int col = entry.x / tileSize;
                    int row = entry.y / tileSize;
                    // Note: Respawn currently assumes Map 0. 
                    // If you want respawns on other maps, RespawnEntry needs a 'map' field.

                    // Check if player is standing here to prevent getting stuck
                    int playerCol = (player.worldX + tileSize/2) / tileSize;
                    int playerRow = (player.worldY + tileSize/2) / tileSize;
                    if (col == playerCol && row == playerRow) {
                        entry.respawnTime += 1000; // Delay 1 second
                        continue;
                    }

                    if(entry.name.equals("Tree")) obj.add(new OBJ_Environment(this, col, row, "Tree"));
                    else if(entry.name.equals("Rock")) obj.add(new OBJ_Environment(this, col, row, "Rock"));
                    else if(entry.name.equals("Berry Bush")) obj.add(new OBJ_Environment(this, col, row, "Berry Bush"));
                    iter.remove();
                }
            }

            // TOOLTIP LOGIC
            if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed || player.onPath) {
                hoverCounter = 0;
            } else {
                hoverCounter++;
            }

            if (hoverCounter >= 60) { // 1 second (60fps * 1)
                updateTooltip();
            } else {
                tooltipText = "";
            }
        }
        if (gameState == pauseState) {
            // Pause logic (if any)
        }
        if (gameState == craftingState) {
            // Crafting logic
        }
        if (gameState == introState) {
            ui.update();
        }
        if (gameState == saveState) {
            // Save screen logic
        }
        if (gameState == loadState) {
            // Load screen logic
        }
        if (gameState == saveNamingState) {
            // Save naming logic
        }
        if (gameState == skillState) {
            // Skill screen logic
        }
        if (gameState == dreamState) {
            if (keyH.enterPressed) {
                gameState = playState;
                player.wakeUp();
                keyH.enterPressed = false;
            }
        }
    }

    public void scheduleRespawn(Entity e, long delay) {
        respawnList.add(new RespawnEntry(e.worldX, e.worldY, e.name, System.currentTimeMillis() + delay));
        System.out.println("Scheduled respawn for " + e.name + " in " + (delay/1000) + "s");
    }

    public void updateTooltip() {
        // Calculate world coordinates from mouse screen coordinates
        double screenOffsetX = getWidth() / 2 - (tileSize * scale) / 2;
        double screenOffsetY = getHeight() / 2 - (tileSize * scale) / 2;
        double worldX = ((mouseX - screenOffsetX) / scale) + player.worldX;
        double worldY = ((mouseY - screenOffsetY) / scale) + player.worldY;

        // 1. Check Entities (Top Layer)
        for (Entity e : obj) {
            if (e != null) {
                // Simple bounding box check (Must be on same map)
                if (e.map == currentMap && worldX >= e.worldX && worldX < e.worldX + tileSize &&
                    worldY >= e.worldY && worldY < e.worldY + tileSize) {
                    if (e.type == Entity.TYPE_OBSTACLE || e.type == Entity.TYPE_NPC) {
                        tooltipText = e.name + " (HP: " + e.life + ")";
                    } else {
                        tooltipText = e.name;
                    }
                    return;
                }
            }
        }

        // 2. Check Tiles (Bottom Layer)
        int col = (int)(worldX / tileSize);
        int row = (int)(worldY / tileSize);

        if (col >= 0 && col < maxWorldCol && row >= 0 && row < maxWorldRow) {
            if (tileM.fogMap[currentMap][col][row]) {
                tooltipText = "Unknown";
            } else {
                int tileNum = tileM.mapTileNum[currentMap][col][row];
                tooltipText = tileM.getTileName(tileNum);
            }
        } else {
            tooltipText = "";
        }
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

        // DRAW OBJECTS
        for (Entity e : obj) {
            if (e != null && e.map == currentMap) e.draw(g2, this);
        }
        
        // DRAW PROJECTILES
        for (int i = 0; i < projectiles.size(); i++) {
            if (projectiles.get(i) != null && projectiles.get(i).map == currentMap) {
                projectiles.get(i).draw(g2, this);
            }
        }
        
        // DRAW PARTICLES
        for (int i = 0; i < particleList.size(); i++) {
            if (particleList.get(i) != null && particleList.get(i).map == currentMap) {
                particleList.get(i).draw(g2, this);
            }
        }

        // DRAW PLAYER
        player.draw(g2);
        
        // DRAW LIGHTING/DARKNESS
        environmentM.draw(g2);

        // DRAW FOG (Top Layer)
        tileM.drawFog(g2);

        // In paintComponent(Graphics g), add this at the VERY end
        // You want the UI to be drawn ON TOP of the map and player
        ui.draw(g2);

        g2.dispose();
    }

    // --- SAVE & LOAD SYSTEM ---
    public String currentSaveFileName = "born_with_stars_save.dat";

    public void saveGame() {
        saveGame(currentSaveFileName);
    }

    public void saveGame(String fileName) {
        saveGame(new File(System.getProperty("user.home"), fileName));
        currentSaveFileName = fileName;
    }

    public void saveGame(File saveFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            
            bw.write("worldX:" + player.worldX);
            bw.newLine();
            bw.write("worldY:" + player.worldY);
            bw.newLine();
            bw.write("life:" + player.life);
            bw.newLine();
            bw.write("maxLife:" + player.maxLife);
            bw.newLine();
            bw.write("energy:" + player.energy);
            bw.newLine();
            bw.write("maxEnergy:" + player.maxEnergy);
            bw.newLine();
            bw.write("direction:" + player.direction);
            bw.newLine();

            for (InventorySlot slot : player.inventorySlots) {
                for (Item item : slot.items) {
                    bw.write("item:" + item.name + "," + item.amount);
                    bw.newLine();
                }
            }

            // Save Objects (Trees, Animals, Dropped Items)
            for (Entity e : obj) {
                if (e instanceof OBJ_DroppedItem) {
                    OBJ_DroppedItem di = (OBJ_DroppedItem)e;
                    bw.write("obj:item," + di.item.name + "," + di.item.amount + "," + e.worldX + "," + e.worldY);
                } else if (e instanceof OBJ_Animal) {
                    bw.write("obj:animal," + e.name + "," + e.worldX + "," + e.worldY + "," + e.life);
                } else {
                    // Obstacles (Tree, Rock, etc)
                    bw.write("obj:obstacle," + e.name + "," + e.worldX + "," + e.worldY + "," + e.life);
                }
                bw.newLine();
            }
            
            System.out.println("Game Saved to: " + saveFile.getAbsolutePath());
            ui.showMessage("Game Saved!");
            ui.showSaveIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadGame() {
        return loadGame(currentSaveFileName);
    }

    public boolean loadGame(String fileName) {
        return loadGame(new File(System.getProperty("user.home"), fileName));
    }

    public boolean loadGame(File saveFile) {
        if (!saveFile.exists()) {
            System.out.println("No save file found!");
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            player.inventorySlots.clear();
            obj.clear(); // Clear existing objects to restore state from save
            boolean hasData = false;
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    hasData = true;
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (key.equals("worldX")) player.worldX = Integer.parseInt(value);
                    if (key.equals("worldY")) player.worldY = Integer.parseInt(value);
                    if (key.equals("life")) player.life = Integer.parseInt(value);
                    if (key.equals("maxLife")) player.maxLife = Integer.parseInt(value);
                    if (key.equals("energy")) player.energy = Integer.parseInt(value);
                    if (key.equals("maxEnergy")) player.maxEnergy = Integer.parseInt(value);
                    if (key.equals("direction")) player.direction = value;
                    if (key.equals("item")) {
                        String[] itemParts = value.split(",");
                        Item newItem = player.createItem(itemParts[0]);
                        if (itemParts.length > 1) {
                            newItem.amount = Integer.parseInt(itemParts[1]);
                        }
                        player.pickUpItem(newItem); // Use pickUpItem to sort into slots
                    }
                    if (key.equals("obj")) {
                        String[] objParts = value.split(",");
                        String type = objParts[0];
                        String name = objParts[1];
                        
                        if (type.equals("item")) {
                            int amount = Integer.parseInt(objParts[2]);
                            int x = Integer.parseInt(objParts[3]);
                            int y = Integer.parseInt(objParts[4]);
                            Item item = player.createItem(name);
                            item.amount = amount;
                            obj.add(new OBJ_DroppedItem(this, item, x, y));
                        } else if (type.equals("animal")) {
                            int x = Integer.parseInt(objParts[2]);
                            int y = Integer.parseInt(objParts[3]);
                            int life = Integer.parseInt(objParts[4]);
                            // Animal constructor takes grid coords, so divide by tileSize
                            OBJ_Animal animal = new OBJ_Animal(this, name, x / tileSize, y / tileSize);
                            animal.life = life;
                            obj.add(animal);
                        } else if (type.equals("obstacle")) {
                            int x = Integer.parseInt(objParts[2]);
                            int y = Integer.parseInt(objParts[3]);
                            int life = Integer.parseInt(objParts[4]);
                            int col = x / tileSize;
                            int row = y / tileSize;
                            
                            if (name.equals("Tree")) {
                                OBJ_Environment tree = new OBJ_Environment(this, col, row, "Tree");
                                tree.life = life;
                                obj.add(tree);
                            } else if (name.equals("Rock")) {
                                OBJ_Environment rock = new OBJ_Environment(this, col, row, "Rock");
                                rock.life = life;
                                obj.add(rock);
                            } else if (name.equals("Berry Bush") || name.equals("Bush")) {
                                OBJ_Environment bush = new OBJ_Environment(this, col, row, "Berry Bush");
                                bush.life = life;
                                obj.add(bush);
                            } else if (name.equals("Flint Vein")) {
                                OBJ_Environment flint = new OBJ_Environment(this, col, row, "Flint Vein");
                                flint.life = life;
                                obj.add(flint);
                            } else if (name.equals("Fire Pit")) {
                                OBJ_FirePit firePit = new OBJ_FirePit(this, col, row);
                                firePit.life = life;
                                obj.add(firePit);
                            } else if (name.equals("Tipi")) {
                                OBJ_Tipi tipi = new OBJ_Tipi(this, col, row);
                                tipi.life = life;
                                obj.add(tipi);
                            } else if (name.equals("Old Man") || name.equals("Chief")) {
                                OBJ_NPC npc = new OBJ_NPC(this, name, col, row);
                                npc.life = life;
                                obj.add(npc);
                            }
                        }
                    }
                }
            }
            
            // Validate Coordinates
            if (player.worldX < 0 || player.worldY < 0 || 
                player.worldX >= maxWorldCol * tileSize || player.worldY >= maxWorldRow * tileSize) {
                System.out.println("Save file contained invalid coordinates! Resetting.");
                return false;
            }

            if (!hasData) {
                System.out.println("Save file was empty or invalid!");
                return false;
            }

            currentSaveFileName = saveFile.getName();
            System.out.println("Game Loaded!");
            ui.showMessage("Game Loaded!");
            return true;
        } catch (Exception e) {
            System.out.println("Load failed!");
            e.printStackTrace();
            return false;
        }
    }
    
    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }
    public void stopMusic() {
        music.stop();
    }
    public void playSE(int i) {
        se.setFile(i);
        se.play();
    }
}