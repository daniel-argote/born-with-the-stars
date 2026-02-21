package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.io.File;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import main.GamePanel;
import entity.Item;
import entity.InventorySlot;
import entity.Quest;
import entity.Entity;
import entity.OBJ_NPC;

public class UI {

    GamePanel gp;
    Graphics2D g2;
    Font arial_40, arial_80B;
    
    // MINI-MAP STATES
    public boolean miniMapOn = true;      // Controlled by 'M'
    public boolean miniMapFullView = false; // Controlled by 'N'
    public float transition = 0; // Controls the sliding animation
    public boolean showStatusTimer = true; // Configurable setting
    public boolean showDevLegend = false; // Controlled by 'H'
    public boolean inventoryOn = false; // Controlled by 'B'
    public int commandNum = 0;
    public boolean messageOn = false;
    public Rectangle menuNewGameRect = new Rectangle();
    public Rectangle menuLoadGameRect = new Rectangle();
    public Rectangle menuTutorialRect = new Rectangle();
    public Rectangle menuDevModeRect = new Rectangle();
    public Rectangle pauseResumeRect = new Rectangle();
    public Rectangle pauseSaveRect = new Rectangle();
    public Rectangle pauseLoadRect = new Rectangle();
    public Rectangle pauseSettingsRect = new Rectangle();
    public Rectangle pauseQuitRect = new Rectangle();
    public Rectangle craftArrowRect = new Rectangle();
    public Rectangle craftPickaxeRect = new Rectangle();
    public Rectangle craftFirePitRect = new Rectangle();
    public Rectangle gameOverLoadRect = new Rectangle();
    public Rectangle gameOverSacrificeRect = new Rectangle();
    public Rectangle gameOverQuitRect = new Rectangle();
    public Rectangle optionsMusicVolumeRect = new Rectangle();
    public Rectangle optionsSEVolumeRect = new Rectangle();
    public Rectangle optionsAutoCollectRect = new Rectangle();
    public Rectangle optionsShowTimerRect = new Rectangle();
    public Rectangle optionsBackRect = new Rectangle();
    public String message = "";
    public int messageCounter = 0;
    public String tempSaveName = "";
    public ArrayList<String> saveFiles = new ArrayList<>();
    public String currentDialogue = "";
    public boolean saveIconOn = false;
    public int saveIconCounter = 0;
    public Entity npc;
    
    // DREAM
    public String currentDreamTitle = "";
    public String currentDreamDesc = "";
    
    // INTRO
    public int introCounter = 0;
    
    // COLOR PALETTE
    public final Color cosmicBlack = new Color(10, 15, 30); 
    public final Color auroraTeal = new Color(0, 255, 240); 
    public final Color glacierMint = new Color(150, 255, 235); 
    public final Color starWhite = new Color(230, 255, 255); 
    public final Color nebulaPurple = new Color(45, 40, 80);

    public class Star {
        int x, y;
        Color color;
        double depth;
        public Star(int x, int y, Color color, double depth) {
            this.x = x; this.y = y; this.color = color;
            this.depth = depth;
        }
    }
    ArrayList<Star> stars = new ArrayList<>();
    int lastWidth = 0;
    int lastHeight = 0;

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_80B = new Font("Arial", Font.BOLD, 80);
        
        generateStars();
    }

    public void showMessage(String text) {
        message = text;
        messageOn = true;
        messageCounter = 120; // 2 seconds at 60 FPS
    }

    public void showSaveIcon() {
        saveIconOn = true;
        saveIconCounter = 120; // 2 seconds
    }

    public void generateStars() {
        stars.clear();
        int w = gp.getWidth();
        int h = gp.getHeight();
        
        if (w == 0 || h == 0) {
            w = gp.maxScreenCol * gp.tileSize;
            h = gp.maxScreenRow * gp.tileSize;
        }
        
        for (int i = 0; i < 200; i++) {
            int x = (int)(Math.random()*w);
            int y = (int)(Math.random()*h);
            Color c = starWhite;
            double r = Math.random();
            if (r < 0.15) {
                if (r < 0.08) c = auroraTeal;
                else c = glacierMint;
            }
            double depth = Math.random(); // 0.0 to 1.0 for parallax layer
            stars.add(new Star(x, y, c, depth));
        }
    }

    public void update() {
        if (gp.gameState == gp.introState) {
            introCounter++;
            if (introCounter > 1900) { // Reduced to match shorter text duration
                gp.gameState = gp.playState;
            }
        }

        // ANIMATION LOGIC
        if (miniMapOn && transition < 1) transition += 0.05f;
        if (!miniMapOn && transition > 0) transition -= 0.05f;
        
        if (messageOn) {
            messageCounter--;
            if (messageCounter <= 0) {
                messageOn = false;
            }
        }
        
        if (saveIconOn) {
            saveIconCounter--;
            if (saveIconCounter <= 0) {
                saveIconOn = false;
            }
        }
    }

    public void resetIntro() {
        introCounter = 0;
    }

    public void toggleMiniMap() {
        miniMapOn = !miniMapOn;
    }

    public void toggleViewMode() {
        miniMapFullView = !miniMapFullView;
    }

    public void toggleInventory() {
        inventoryOn = !inventoryOn;
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.WHITE);

        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        } else {
            // Draw HUD elements
            drawCoordinates(g2);
            drawPlayerLife(g2);
            drawPlayerEnergy(g2);
            drawPlayerStatus(g2);
            drawEquippedItem(g2);
            drawMiniMap(g2);
            drawInventory(g2);
            drawActiveQuest(g2);
            if (showDevLegend) {
                drawDevLegend(g2);
            }
        }
        
        // MESSAGE
        if (messageOn) {
            g2.setFont(g2.getFont().deriveFont(30F));
            g2.drawString(message, gp.tileSize / 2, gp.tileSize * 5);
        }

        if (gp.gameState == gp.pauseState) {
            drawPauseScreen();
        }
        if (gp.gameState == gp.optionsState) {
            drawOptionsScreen();
        }
        if (gp.gameState == gp.craftingState) {
            drawCraftingScreen();
        }
        if (gp.gameState == gp.gameOverState) {
            drawGameOverScreen();
        }
        if (gp.gameState == gp.introState) {
            drawIntroScreen(g2);
        }
        if (gp.gameState == gp.saveState) {
            drawSaveSelectScreen();
        }
        if (gp.gameState == gp.saveNamingState) {
            drawSaveNamingScreen();
        }
        if (gp.gameState == gp.loadState) {
            drawLoadScreen();
        }
        if (gp.gameState == gp.questState) {
            drawQuestLog();
        }
        if (gp.gameState == gp.dialogueState) {
            drawDialogueScreen();
        }
        if (gp.gameState == gp.tradeState) {
            drawTradeScreen();
        }
        if (gp.gameState == gp.dreamState) {
            drawDreamScreen();
        }
        if (gp.gameState == gp.skillState) {
            drawSkillScreen();
        }
        
        // TOOLTIP (Always on top)
        if (gp.gameState == gp.playState && gp.tooltipText != null && !gp.tooltipText.isEmpty()) {
            drawTooltip();
        }
        
        if (saveIconOn) {
            drawSaveIcon(g2);
        }
    }

    public void drawCoordinates(Graphics2D g2) {
        g2.setFont(g2.getFont().deriveFont(18F));
        g2.setColor(Color.WHITE);

        int worldCol = (int)gp.player.worldX / gp.tileSize;
        int worldRow = (int)gp.player.worldY / gp.tileSize;

        String text = "Sector: " + worldCol + " x " + worldRow;
        
        // Shadow for readability
        g2.setColor(Color.BLACK);
        g2.drawString(text, 22, 42);
        g2.setColor(Color.WHITE);
        g2.drawString(text, 20, 40);
        
        // Day/Night Indicator
        int textWidth = g2.getFontMetrics().stringWidth(text);
        drawDayNightIndicator(g2, 20 + textWidth + 25, 35);
    }

    public void drawDayNightIndicator(Graphics2D g2, int x, int y) {
        int r = 8; // Radius
        
        int time = gp.environmentM.dayCounter;
        boolean isDay = (time > 3600 && time < 24000);
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(2));
        
        if (isDay) {
            // Sun: Circle with rays
            g2.drawOval(x - r, y - r, r * 2, r * 2);
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45);
                int x1 = (int)(x + Math.cos(angle) * (r));
                int y1 = (int)(y + Math.sin(angle) * (r));
                int x2 = (int)(x + Math.cos(angle) * (r + 4));
                int y2 = (int)(y + Math.sin(angle) * (r + 4));
                g2.drawLine(x1, y1, x2, y2);
            }
        } else {
            // Moon: Crescent
            Area moon = new Area(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            Area shadow = new Area(new Ellipse2D.Double(x - r + 4, y - r - 2, r * 2, r * 2));
            moon.subtract(shadow);
            g2.fill(moon);
        }
    }

    public void drawPlayerLife(Graphics2D g2) {
        int x = 20;
        int y = 60;
        int width = 200; // Width of the bar
        int height = 20;
        
        // Background (Dark Gray)
        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x, y, width, height);
        
        // Health (Red)
        g2.setColor(new Color(255, 0, 30));
        double oneScale = (double)width / gp.player.maxLife;
        double hpBarValue = oneScale * gp.player.life;
        g2.fillRect(x, y, (int)hpBarValue, height);
        
        // Outline (White)
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(x, y, width, height);
    }

    public void drawPlayerEnergy(Graphics2D g2) {
        int x = 20;
        int y = 85; // Below life bar
        int width = 200;
        int height = 20;
        
        // Background (Dark Gray)
        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x, y, width, height);
        
        // Energy (Yellow)
        g2.setColor(new Color(255, 200, 0));
        double oneScale = (double)width / gp.player.maxEnergy;
        double barValue = oneScale * gp.player.energy;
        g2.fillRect(x, y, (int)barValue, height);
        
        // Outline (White)
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(x, y, width, height);
    }

    public void drawPlayerStatus(Graphics2D g2) {
        int x = 20;
        int y = 125;
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(16F));
        g2.drawString("Status:", x, y);
        
        if (gp.player.wet) {
            // Draw Water Icon (Teardrop)
            g2.setColor(new Color(0, 191, 255)); // Deep Sky Blue
            int iconX = x + 70;
            int iconY = y - 12;
            
            int[] xPoints = {iconX, iconX - 6, iconX + 6};
            int[] yPoints = {iconY, iconY + 10, iconY + 10};
            g2.fillPolygon(xPoints, yPoints, 3); // Top triangle
            g2.fillOval(iconX - 6, iconY + 6, 12, 12); // Bottom circle

            if (showStatusTimer && gp.player.wetCounter < 600) {
                g2.setColor(Color.WHITE);
                int seconds = (int)Math.ceil(gp.player.wetCounter / 60.0);
                g2.drawString(seconds + "s", iconX + 15, y);
            }
        } else {
            g2.drawString("Normal", x + 70, y);
        }
        
        // Draw Buffs
        y += 25;
        if (gp.player.strengthBuffTimer > 0) {
            g2.setColor(new Color(255, 100, 100));
            g2.drawString("STR UP (" + (gp.player.strengthBuffTimer/60) + "s)", x, y);
            y += 25;
        }
        if (gp.player.speedBuffTimer > 0) {
            g2.setColor(new Color(100, 255, 100));
            g2.drawString("SPD UP (" + (gp.player.speedBuffTimer/60) + "s)", x, y);
        }
    }

    public void drawEquippedItem(Graphics2D g2) {
        int x = 20;
        int y = 160;
        int size = gp.tileSize;
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(16F));
        g2.drawString("Equipped:", x, y - 8);
        
        drawSlot(x, y, size, gp.player.currentWeapon, -1);
    }

    public void drawActiveQuest(Graphics2D g2) {
        // Find first incomplete quest
        Quest active = null;
        for(Quest q : gp.questM.questList) {
            if(!q.completed) {
                active = q;
                break;
            }
        }
        
        if(active != null) {
            int x = 20;
            int y = 260; // Moved down to avoid collision with Equipped Item slot
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16F));
            g2.drawString("Quest: " + active.name, x, y);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14F));
            g2.drawString(active.description + " (" + active.currentAmount + "/" + active.targetAmount + ")", x, y + 20);
        }
    }

    public void drawDevLegend(Graphics2D g2) {
        int x = gp.getWidth() - 220;
        int y = 240;
        int lineHeight = 18;
        
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(x - 10, y - 20, 210, 280, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
        g2.drawString("CONTROLS (H)", x, y);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12F));
        y += 25; g2.drawString("WASD: Move", x, y);
        y += lineHeight; g2.drawString("ENTER: Interact/Attack", x, y);
        y += lineHeight; g2.drawString("SPACE: Jump", x, y);
        y += lineHeight; g2.drawString("F: Sleep (Near Fire)", x, y);
        y += lineHeight; g2.drawString("C: Crafting", x, y);
        y += lineHeight; g2.drawString("Q: Quest Log", x, y);
        y += lineHeight; g2.drawString("M: Toggle Map", x, y);
        y += lineHeight; g2.drawString("N: Map View Mode", x, y);
        y += lineHeight; g2.drawString("Z: Hold to Zoom", x, y);
        y += lineHeight; g2.drawString("T: Hold + Click to Walk", x, y);
        y += lineHeight; g2.drawString("K: Toggle Day/Night", x, y);
        y += lineHeight; g2.drawString("P: Export Map", x, y);
        y += lineHeight; g2.drawString("V: Toggle Auto-Collect", x, y);
    }

    public void drawMiniMap(Graphics2D g2) {
        // 1. DIMENSIONS
        int mmSize = 200; 
        int mmX = gp.getWidth() - mmSize - 20; // Target X position (Dynamic)
        int mmY = 20; // Fixed Y position at the top

        if (transition <= 0) return;

        // 3. HORIZONTAL SLIDE MATH
        // When transition is 1, animatedX = mmX (fully visible)
        // When transition is 0, animatedX = gp.getWidth() (fully off-screen)
        int animatedX = (int) (mmX + ((gp.getWidth() - mmX) * (1 - transition)));

        // 4. DRAW THE WINDOW
        drawSubWindow(animatedX, mmY, mmSize, mmSize);
        
        // Clip the graphics inside the moving window
        g2.setClip(animatedX, mmY, mmSize, mmSize);

        if (miniMapFullView) {
            // --- GLOBAL VIEW ---
            float scale = (float)mmSize / gp.maxWorldCol; 
            for (int i = 0; i < gp.maxWorldCol; i++) {
                for (int j = 0; j < gp.maxWorldRow; j++) {
                    if (gp.tileM.fogMap[gp.currentMap][i][j]) {
                        g2.setColor(new Color(40, 40, 40)); // Dark Gray for Fog
                    } else {
                        int tileNum = gp.tileM.mapTileNum[gp.currentMap][i][j];
                        setMiniMapColor(tileNum);
                    }
                    
                    g2.fillRect(animatedX + (int)(i * scale), mmY + (int)(j * scale), (int)scale + 1, (int)scale + 1);
                }
            }
            
            // Player Dot (Global)
            double worldWidth = gp.maxWorldCol * gp.tileSize;
            double worldHeight = gp.maxWorldRow * gp.tileSize;
            int pX = animatedX + (int)((gp.player.worldX / worldWidth) * mmSize);
            int pY = mmY + (int)((gp.player.worldY / worldHeight) * mmSize);
            g2.setColor(Color.WHITE);
            g2.fillRect(pX - 2, pY - 2, 4, 4);

        } else {
            // --- LOCAL VIEW ---
            int mmTileSize = 6;
            int playerCol = (int)gp.player.worldX / gp.tileSize;
            int playerRow = (int)gp.player.worldY / gp.tileSize;
            int range = 15;

            for (int i = playerCol - range; i < playerCol + range; i++) {
                for (int j = playerRow - range; j < playerRow + range; j++) {
                    if (i >= 0 && i < gp.maxWorldCol && j >= 0 && j < gp.maxWorldRow) {
                        if (gp.tileM.fogMap[gp.currentMap][i][j]) {
                            g2.setColor(new Color(40, 40, 40));
                        } else {
                            setMiniMapColor(gp.tileM.mapTileNum[gp.currentMap][i][j]);
                        }
                        
                        int drawX = animatedX + (mmSize / 2) + (i - playerCol) * mmTileSize;
                        int drawY = mmY + (mmSize / 2) + (j - playerRow) * mmTileSize;
                        g2.fillRect(drawX, drawY, mmTileSize, mmTileSize);
                    }
                }
            }
            // Static Player Dot (Dead Center)
            g2.setColor(Color.WHITE);
            g2.fillRect(animatedX + (mmSize / 2) - 2, mmY + (mmSize / 2) - 2, 4, 4);
        }

        g2.setClip(null);
    }

    public void drawInventory(Graphics2D g2) {
        // Center the hotbar at the bottom
        int slotSize = gp.tileSize;
        int frameX = gp.getWidth() / 2 - (slotSize * 5);
        int frameY = gp.getHeight() - (slotSize + 20);

        // Draw Hotbar (First 10 slots)
        for (int i = 0; i < 10; i++) {
            Item item = null;
            if (i < gp.player.inventorySlots.size()) {
                item = gp.player.inventorySlots.get(i).getSelectedItem();
            }
            drawSlot(frameX + (i * slotSize), frameY, slotSize, item, i);
        }

        // Draw Expanded Inventory (Rows above hotbar)
        if (inventoryOn) {
            for (int i = 10; i < gp.player.inventorySlots.size(); i++) {
                int col = i % 10;
                int row = i / 10; // 1, 2, 3...
                
                int x = frameX + (col * slotSize);
                int y = frameY - (row * slotSize);
                
                Item item = gp.player.inventorySlots.get(i).getSelectedItem();
                drawSlot(x, y, slotSize, item, i);
            }
        }
        
        // Draw Equipped Highlight (Last to ensure it's on top)
        if (gp.player.currentWeapon != null) {
            // Find which slot contains the current weapon
            int index = -1;
            for(int i=0; i<gp.player.inventorySlots.size(); i++) {
                if(gp.player.inventorySlots.get(i).items.contains(gp.player.currentWeapon)) {
                    index = i;
                    break;
                }
            }
            
            if (index != -1) {
                if (index < 10 || inventoryOn) {
                    int x, y;
                    if (index < 10) {
                        x = frameX + (index * slotSize);
                        y = frameY;
                    } else {
                        int col = index % 10;
                        int row = index / 10;
                        x = frameX + (col * slotSize);
                        y = frameY - (row * slotSize);
                    }
                    g2.setColor(new Color(255, 215, 0)); // Gold
                    g2.setStroke(new java.awt.BasicStroke(3));
                    g2.drawRect(x, y, slotSize, slotSize);
                }
            }
        }
        
        // Draw Hover Popups (Items in the same category)
        for (int i = 0; i < 10; i++) {
            int x = frameX + (i * slotSize);
            int y = frameY;
            
            // Check if mouse is over this slot
            if (gp.mouseX >= x && gp.mouseX < x + slotSize && gp.mouseY >= y && gp.mouseY < y + slotSize) {
                // Or if key is held
                boolean showPopup = true; // Mouse hover implies show
                
                if (showPopup && i < gp.player.inventorySlots.size()) {
                    InventorySlot slot = gp.player.inventorySlots.get(i);
                    if (slot.items.size() > 1) {
                        int popupY = y - (slot.items.size() * 25) - 10;
                        
                        // Draw background
                        g2.setColor(new Color(0,0,0,200));
                        g2.fillRoundRect(x, popupY, 150, slot.items.size() * 25 + 5, 10, 10);
                        
                        for(int j=0; j<slot.items.size(); j++) {
                            g2.setColor(Color.WHITE);
                            if(j == slot.selectedIndex) g2.setColor(Color.YELLOW);
                            g2.setFont(g2.getFont().deriveFont(14F));
                            g2.drawString(slot.items.get(j).name, x + 10, popupY + 20 + (j * 25));
                        }
                    }
                }
            }
        }
    }

    private void drawSlot(int x, int y, int size, Item item, int slotIndex) {
        // Background
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x, y, size, size);
        
        // Outline
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(x, y, size, size);

        // Top Left: Hotbar Key (Only for first 10 slots)
        if (slotIndex >= 0 && slotIndex < 10) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
            g2.setColor(Color.LIGHT_GRAY);
            int keyNum = (slotIndex + 1) % 10;
            g2.drawString(String.valueOf(keyNum), x + 5, y + 15);
        }

        // Draw Item Icon
        if (item != null) {
            item.draw(g2, x, y, size);
            
            // Durability Bar
            if (item.type == Item.TYPE_SWORD || item.type == Item.TYPE_AXE || 
                item.type == Item.TYPE_PICKAXE || item.type == Item.TYPE_BOW) {
                
                int barWidth = size - 10;
                int barHeight = 5;
                int barX = x + 5;
                int barY = y + size - 10;

                // Background
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(barX, barY, barWidth, barHeight);

                // Current Durability
                double percent = (double)item.durability / item.maxDurability;
                g2.setColor(percent > 0.5 ? Color.GREEN : (percent > 0.25 ? Color.YELLOW : Color.RED));
                g2.fillRect(barX, barY, (int)(barWidth * percent), barHeight);
            }

            // Bottom Right: Quantity
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
            g2.setColor(Color.WHITE); // Ensure color is white for quantity
            String qty = item.amount > 1 ? String.valueOf(item.amount) : "";
            int strWidth = g2.getFontMetrics().stringWidth(qty);
            g2.drawString(qty, x + size - strWidth - 5, y + size - 5);
        }
    }

    // Make sure you have your drawSubWindow method for that cool rounded-box look:
    public void drawSubWindow(int x, int y, int width, int height) {
        Color c = new Color(0, 0, 0, 210);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255, 255, 255);
        g2.setColor(c);
        g2.setStroke(new java.awt.BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    private void setMiniMapColor(int tileNum) {
        // Basic terrain colors
        if (tileNum >= 0 && tileNum <= 63) {
            g2.setColor(new Color(34, 139, 34)); // Grass Green
        } else if (tileNum >= 800 && tileNum <= 1119) {
            g2.setColor(Color.BLUE); // Water Blue
        } else if (tileNum >= 1120 && tileNum <= 1183) {
            g2.setColor(Color.YELLOW); // Sand Yellow
        } else {
            g2.setColor(Color.GRAY); // Mountains/Stone
        }
    }

    public void drawPauseScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        int x;
        int y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 80F));
        
        text = "PAUSED";
        x = getXforCenteredText(text);
        y = gp.getHeight() / 2 - 100;
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
        
        // MENU OPTIONS
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        
        text = "Resume";
        x = getXforCenteredText(text);
        y += 80;
        g2.drawString(text, x, y);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        pauseResumeRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 0) g2.drawString(">", x - 40, y);
        
        text = "Save Game";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        pauseSaveRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 1) g2.drawString(">", x - 40, y);
        
        text = "Load Game";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        pauseLoadRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 2) g2.drawString(">", x - 40, y);

        text = "Settings";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        pauseSettingsRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 3) g2.drawString(">", x - 40, y);
        
        text = "Quit to Title";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        pauseQuitRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 4) g2.drawString(">", x - 40, y);
    }

    public void drawOptionsScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 80F));
        String text = "SETTINGS";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));

        // Option 0: Music Volume
        text = "Music";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        optionsMusicVolumeRect.setBounds(x, y - 30, 200, 40); // Approx hit box
        if (commandNum == 0) g2.drawString(">", x - 40, y);
        
        // Draw Music Bar
        g2.drawRect(x, y + 10, 120, 24); // 5 segments * 24 width
        int volumeWidth = 24 * gp.music.volumeScale;
        g2.fillRect(x, y + 10, volumeWidth, 24);

        // Option 1: SE Volume
        text = "Sound Effects";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        optionsSEVolumeRect.setBounds(x, y - 30, 300, 40);
        if (commandNum == 1) g2.drawString(">", x - 40, y);
        
        // Draw SE Bar
        g2.drawRect(x, y + 10, 120, 24);
        volumeWidth = 24 * gp.se.volumeScale;
        g2.fillRect(x, y + 10, volumeWidth, 24);

        // Option 2: Auto Collect
        text = "Auto Collect";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        optionsAutoCollectRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 2) g2.drawString(">", x - 40, y);

        // Draw Checkbox/Value
        g2.drawRect(x + 250, y - 30, 30, 30);
        if (gp.player.autoCollect) {
            g2.fillRect(x + 254, y - 26, 22, 22);
        }

        // Option 3: Show Timers
        text = "Show Timers";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        optionsShowTimerRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 3) g2.drawString(">", x - 40, y);

        // Draw Checkbox/Value
        g2.drawRect(x + 250, y - 30, 30, 30);
        if (showStatusTimer) {
            g2.fillRect(x + 254, y - 26, 22, 22);
        }

        // Option 4: Back
        text = "Back";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        optionsBackRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 4) g2.drawString(">", x - 40, y);
    }

    public void drawCraftingScreen() {
        // Background
        drawSubWindow(gp.getWidth() / 2 - 300, gp.getHeight() / 2 - 200, 600, 400);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        String text = "CRAFTING";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 150;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        int startX = gp.getWidth() / 2 - 250;
        int startY = gp.getHeight() / 2 - 80;
        int lineHeight = 50;

        // Recipe 1: Arrows
        if (gp.player.hasItem("Branch", 1) && gp.player.hasItem("Flint", 1)) g2.setColor(Color.WHITE);
        else g2.setColor(Color.GRAY);
        g2.drawString("Arrows (x5)", startX, startY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        g2.drawString("Req: 1 Branch, 1 Flint", startX + 200, startY);
        craftArrowRect.setBounds(startX - 25, startY - 25, 500, 50);
        if (commandNum == 0) {
            g2.setColor(Color.WHITE);
            g2.drawString(">", startX - 25, startY);
        }

        // Recipe 2: Pickaxe
        if (gp.player.hasItem("Stone", 3) && gp.player.hasItem("Branch", 2)) g2.setColor(Color.WHITE);
        else g2.setColor(Color.GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        g2.drawString("Pickaxe", startX, startY + lineHeight);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        g2.drawString("Req: 3 Stone, 2 Branch", startX + 200, startY + lineHeight);
        craftPickaxeRect.setBounds(startX - 25, startY + lineHeight - 25, 500, 50);
        if (commandNum == 1) {
            g2.setColor(Color.WHITE);
            g2.drawString(">", startX - 25, startY + lineHeight);
        }

        // Recipe 3: Fire Pit
        if (gp.player.hasItem("Wood", 2) && gp.player.hasItem("Stone", 3)) g2.setColor(Color.WHITE);
        else g2.setColor(Color.GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        g2.drawString("Fire Pit", startX, startY + lineHeight * 2);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        g2.drawString("Req: 2 Wood, 3 Stone", startX + 200, startY + lineHeight * 2);
        craftFirePitRect.setBounds(startX - 25, startY + lineHeight * 2 - 25, 500, 50);
        if (commandNum == 2) {
            g2.setColor(Color.WHITE);
            g2.drawString(">", startX - 25, startY + lineHeight * 2);
        }
    }

    public void craftSelected() {
        if (commandNum == 0) {
            gp.player.craftItem("Arrow");
        }
        if (commandNum == 1) {
            gp.player.craftItem("Pickaxe");
        }
        if (commandNum == 2) {
            gp.player.craftItem("Fire Pit");
        }
    }

    public void drawGameOverScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        int x;
        int y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 110F));
        
        text = "GAME OVER";
        // Shadow
        g2.setColor(Color.BLACK);
        x = getXforCenteredText(text);
        y = gp.getHeight() / 2 - 50;
        g2.drawString(text, x + 5, y + 5);
        // Main
        g2.setColor(Color.RED);
        g2.drawString(text, x, y);
        
        // MENU OPTIONS
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        
        // Define options
        String text1 = "Return to Last Save";
        String text2 = "Sacrifice Random Item (Revive)";
        String text3 = "Quit to Title";
        
        // Calculate max width to align left
        int w1 = (int)g2.getFontMetrics().getStringBounds(text1, g2).getWidth();
        int w2 = (int)g2.getFontMetrics().getStringBounds(text2, g2).getWidth();
        int w3 = (int)g2.getFontMetrics().getStringBounds(text3, g2).getWidth();
        
        int maxWidth = Math.max(w1, Math.max(w2, w3));
        int menuX = gp.getWidth() / 2 - maxWidth / 2;
        
        y += 120;
        g2.drawString(text1, menuX, y);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text1, g2);
        gameOverLoadRect.setBounds(menuX, (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 0) g2.drawString(">", menuX - 40, y);
        
        y += 60;
        g2.drawString(text2, menuX, y);
        bounds = g2.getFontMetrics().getStringBounds(text2, g2);
        gameOverSacrificeRect.setBounds(menuX, (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 1) g2.drawString(">", menuX - 40, y);
        
        y += 60;
        g2.drawString(text3, menuX, y);
        bounds = g2.getFontMetrics().getStringBounds(text3, g2);
        gameOverQuitRect.setBounds(menuX, (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 2) g2.drawString(">", menuX - 40, y);
    }

    public int getXforCenteredText(String text) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.getWidth() / 2 - length / 2;
    }
     public void drawTitleScreen() {
        // Background
        g2.setColor(cosmicBlack);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        // Nebula Gradient
        RadialGradientPaint p = new RadialGradientPaint(
             new Point2D.Float(gp.getWidth()/2, gp.getHeight()/2), 
             gp.getHeight(),
             new float[] {0.0f, 1.0f},
             new Color[] {nebulaPurple, cosmicBlack}
        );
        g2.setPaint(p);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        // Title Text
        g2.setColor(auroraTeal);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
        String text = "Born with the Stars";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;
        g2.drawString(text, x, y);

        // Menu Options
        g2.setColor(glacierMint);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

        text = "New Game";
        x = getXforCenteredText(text);
        y += gp.tileSize * 4;
        g2.drawString(text, x, y);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        menuNewGameRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 0) {
            g2.setColor(auroraTeal); g2.drawString(">", x - gp.tileSize, y); g2.setColor(glacierMint);
        }

        text = "Load Game";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        menuLoadGameRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 1) {
            g2.setColor(auroraTeal); g2.drawString(">", x - gp.tileSize, y); g2.setColor(glacierMint);
        }

        text = "Tutorial";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        menuTutorialRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 2) {
            g2.setColor(auroraTeal); g2.drawString(">", x - gp.tileSize, y); g2.setColor(glacierMint);
        }

         text = "Dev Mode";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        bounds = g2.getFontMetrics().getStringBounds(text, g2);
        menuDevModeRect.setBounds((int)(x + bounds.getX()), (int)(y + bounds.getY()), (int)bounds.getWidth(), (int)bounds.getHeight());
        if (commandNum == 3) {
            g2.setColor(auroraTeal); g2.drawString(">", x - gp.tileSize, y); g2.setColor(glacierMint);
        }

    }

    public void drawIntroScreen(Graphics2D g2) {
        if (gp.getWidth() != lastWidth || gp.getHeight() != lastHeight) {
            lastWidth = gp.getWidth();
            lastHeight = gp.getHeight();
            generateStars();
        }

        // 1. Draw Sky
        g2.setColor(new Color(5, 5, 12));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        // Calculate closure early to use for blur effect
        float closure = getEyelidClosure();

        // Parallax Offset based on mouse position
        double pX = (gp.mouseX - gp.getWidth() / 2.0) * 0.05;
        double pY = (gp.mouseY - gp.getHeight() / 2.0) * 0.05;

        // 2. Draw Stars
        for (Star s : stars) {
            // Twinkle effect
            if (Math.random() > 0.05) {
                if (closure > 0.05f) {
                    // Blur effect: Larger, semi-transparent ovals when eyes are not fully open
                    int size = 2 + (int)(closure * 10); 
                    int alpha = Math.max(0, Math.min(255, 50 + (int)(205 * (1.0f - closure))));
                    
                    int drawX = (int)(s.x + (pX * s.depth));
                    int drawY = (int)(s.y + (pY * s.depth));

                    Color c = s.color;
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                    g2.fillOval(drawX - size/2, drawY - size/2, size, size);
                } else {
                    int drawX = (int)(s.x + (pX * s.depth));
                    int drawY = (int)(s.y + (pY * s.depth));
                    g2.setColor(s.color);
                    g2.fillRect(drawX, drawY, 2, 2);
                }
            }
        }
        
        // 3. Eyelids (Blinking Effect)
        
        g2.setColor(Color.BLACK);
        
        double width = gp.getWidth();
        double height = gp.getHeight();
        double midY = height / 2.0;
        
        // Move the corners of the eyelids instead of just curving the center
        double topLidEdge = midY * closure;
        double bottomLidEdge = height - (midY * closure);
        double curveAmount = height * 0.2 * (1.0 - closure); // Subtle curve

        // Top Lid
        Path2D topLid = new Path2D.Double();
        topLid.moveTo(0, 0);
        topLid.lineTo(width, 0);
        topLid.lineTo(width, topLidEdge);
        topLid.quadTo(width / 2.0, topLidEdge - curveAmount, 0, topLidEdge);
        topLid.closePath();
        g2.fill(topLid);

        // Bottom Lid
        Path2D bottomLid = new Path2D.Double();
        bottomLid.moveTo(0, height);
        bottomLid.lineTo(width, height);
        bottomLid.lineTo(width, bottomLidEdge);
        bottomLid.quadTo(width / 2.0, bottomLidEdge + curveAmount, 0, bottomLidEdge);
        bottomLid.closePath();
        g2.fill(bottomLid);

        // 4. Title Text (0 - 300)
        if (introCounter < 300) {
            int alpha = 255;
            if (introCounter > 180) {
                alpha = (int)(255 * (1 - (introCounter - 180) / 120.0)); // 2 sec fade
                if (alpha < 0) alpha = 0;
            }
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
            String text = "Born with the Stars";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;
            g2.drawString(text, x, y);
        }

        // 5. Dialogue
        if (introCounter > 700 && introCounter < 1190) {
            String fullText = "";
            String currentText = "";
            int startCounter = 0;

            if (introCounter < 920) {
                fullText = "Mother: \"He is handsome and strong like his father.\"";
                startCounter = 700;
            } else if (introCounter > 940) {
                fullText = "Father: \"I expect he will be clever and stubborn like his mother.\"";
                startCounter = 940;
            }
            
            if (!fullText.isEmpty()) {
                // Typing effect: 1 char every 3 frames
                int charIndex = (introCounter - startCounter) / 3;
                if (charIndex > fullText.length()) charIndex = fullText.length();
                currentText = fullText.substring(0, charIndex);

                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
                int x = getXforCenteredText(fullText); // Center based on full text so it doesn't jump
                int y = gp.getHeight() - gp.tileSize * 2;
                
                // Box
                int boxWidth = gp.getWidth() - (gp.tileSize * 4);
                int boxHeight = gp.tileSize * 2;
                int boxX = gp.getWidth()/2 - boxWidth/2;
                int boxY = y - gp.tileSize;
                drawSubWindow(boxX, boxY, boxWidth, boxHeight);
                
                g2.setColor(Color.WHITE);
                g2.drawString(currentText, x, y);
            }
        }

        // 6. "8 Years Later" Fade
        if (introCounter > 1240) {
            // Fade to black
            int alpha = (int)(255 * ((introCounter - 1240) / 60.0)); // 1 sec fade in
            if (alpha > 255) alpha = 255;
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
            
            if (introCounter > 1300 && introCounter < 1600) {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60F));
                String text = "8 Years Later";
                int x = getXforCenteredText(text);
                int y = gp.getHeight() / 2;
                g2.drawString(text, x, y);
            }
            
            if (introCounter > 1650 && introCounter < 1830) { // Show for 3 seconds (180 frames)
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
                String text = "Mother tasked you with gathering some rabbits and berries for dinner.";
                int x = getXforCenteredText(text);
                int y = gp.getHeight() / 2;
                g2.drawString(text, x, y);
            }
        }
    }

    private float getEyelidClosure() {
        // 0 to 360: Closed (1.0) - Wait for title to fade
        if (introCounter < 360) return 1.0f;
        
        // 360 to 420: Blink 1 (Open slightly then close)
        if (introCounter < 420) {
            double progress = (introCounter - 360) / 60.0;
            return (float) (1.0 - Math.sin(progress * Math.PI) * 0.8);
        }
        
        // 420 to 480: Closed (Pause)
        if (introCounter < 480) return 1.0f;

        // 480 to 580: Blink 2 (Open slightly more then close) - Slower (100 frames)
        if (introCounter < 580) {
            double progress = (introCounter - 480) / 100.0;
            return (float) (1.0 - Math.sin(progress * Math.PI) * 0.9);
        }

        // 580 to 640: Closed (Pause)
        if (introCounter < 640) return 1.0f;
        
        // 640+: Open slowly
        double progress = Math.min(1.0, (introCounter - 640) / 180.0);
        return (float) (1.0 - progress);
    }

    public void drawSaveNamingScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        
        String text = "Enter Save Name:";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 50;
        g2.drawString(text, x, y);
        
        // Input Box
        g2.setColor(Color.WHITE);
        g2.drawRect(gp.getWidth()/2 - 200, gp.getHeight()/2, 400, 50);
        g2.drawString(tempSaveName, gp.getWidth()/2 - 190, gp.getHeight()/2 + 40);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        text = "Press ENTER to Save, ESC to Cancel";
        x = getXforCenteredText(text);
        y += 150;
        g2.drawString(text, x, y);
    }

    public void updateSaveFileList() {
        saveFiles.clear();
        File dir = new File(System.getProperty("user.home"));
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat") && name.startsWith("born_with_stars_"));
        if (files != null) {
            for (File f : files) {
                saveFiles.add(f.getName());
            }
        }
    }

    public void drawSaveSelectScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        String text = "Select Slot to Save";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 150;
        g2.drawString(text, x, y);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30F));
        int startY = gp.getHeight() / 2 - 100;
        
        // Option 0: Create New
        text = "Create New Save";
        x = getXforCenteredText(text);
        g2.drawString(text, x, startY);
        if (commandNum == 0) g2.drawString(">", x - 30, startY);

        // Existing Files
        for (int i = 0; i < saveFiles.size(); i++) {
            String name = saveFiles.get(i).replace("born_with_stars_", "").replace(".dat", "");
            int drawY = startY + ((i + 1) * 40);
            x = getXforCenteredText(name);
            g2.drawString(name, x, drawY);
            if (commandNum == i + 1) {
                g2.drawString(">", x - 30, drawY);
            }
        }
    }

    public void drawLoadScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        String text = "Select Save File";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 150;
        g2.drawString(text, x, y);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30F));
        int startY = gp.getHeight() / 2 - 100;
        
        for (int i = 0; i < saveFiles.size(); i++) {
            String name = saveFiles.get(i).replace("born_with_stars_", "").replace(".dat", "");
            int drawY = startY + (i * 40);
            x = getXforCenteredText(name);
            g2.drawString(name, x, drawY);
            if (commandNum == i) {
                g2.drawString(">", x - 30, drawY);
            }
        }
    }

    public void drawQuestLog() {
        // Background
        drawSubWindow(gp.getWidth() / 2 - 300, gp.getHeight() / 2 - 250, 600, 500);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        String text = "Quest Log";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 200;
        g2.drawString(text, x, y);
        
        // List Quests
        int startX = gp.getWidth() / 2 - 270;
        int startY = gp.getHeight() / 2 - 150;
        
        for(int i = 0; i < gp.questM.questList.size(); i++) {
            Quest q = gp.questM.questList.get(i);
            
            // Title
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
            g2.setColor(q.completed ? Color.GREEN : Color.YELLOW);
            g2.drawString(q.name + (q.completed ? " (Completed)" : ""), startX, startY + (i * 80));
            
            // Description
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16F));
            g2.setColor(Color.WHITE);
            g2.drawString(q.description, startX, startY + (i * 80) + 25);
            
            // Progress
            if (!q.completed) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawString("Progress: " + q.currentAmount + " / " + q.targetAmount, startX, startY + (i * 80) + 45);
            }
        }
    }

    public void drawDialogueScreen() {
        int x = gp.tileSize * 2;
        int y = gp.getHeight() - (gp.tileSize * 4);
        int width = gp.getWidth() - (gp.tileSize * 4);
        int height = gp.tileSize * 3;
        
        drawSubWindow(x, y, width, height);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        x += gp.tileSize;
        y += gp.tileSize;
        g2.drawString(currentDialogue, x, y);
    }

    public void drawTradeScreen() {
        drawSubWindow(gp.getWidth() / 2 - 250, gp.getHeight() / 2 - 200, 500, 400);
        
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        g2.setColor(Color.WHITE);
        String text = "Trading Post";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 150;
        g2.drawString(text, x, y);
        
        // Subtitle
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        g2.drawString("Your Spirit: " + gp.player.spirit, gp.getWidth() / 2 - 220, y);

        // Items List
        int startX = gp.getWidth() / 2 - 220;
        int startY = gp.getHeight() / 2 - 100;
        
        if (npc instanceof OBJ_NPC) {
            ArrayList<Item> inventory = ((OBJ_NPC)npc).inventory;
            
            for (int i = 0; i < inventory.size(); i++) {
                Item item = inventory.get(i);
                int drawY = startY + (i * 40);
                
                // Draw Cursor
                if (commandNum == i) {
                    g2.drawString(">", startX - 20, drawY);
                }
                
                // Draw Item Name and Price
                g2.drawString(item.name, startX, drawY);
                
                String costText = item.cost + " Spirit";
                int costX = gp.getWidth() / 2 + 150 - g2.getFontMetrics().stringWidth(costText);
                g2.drawString(costText, costX, drawY);
            }
        }
    }

    public void startDream(int type) {
        if (type == 1) {
            currentDreamTitle = "Spirit of the Bear";
            currentDreamDesc = "You dream of a mother bear protecting her cub.\nYou feel a deep strength growing within you.";
        } else if (type == 2) {
            currentDreamTitle = "Spirit of the Eagle";
            currentDreamDesc = "You dream of soaring high above the islands.\nThe world feels smaller, clearer.";
        } else if (type == 3) {
            currentDreamTitle = "Spirit of the Wolf";
            currentDreamDesc = "You dream of running with the pack under the moonlight.\nYour senses sharpen.";
        } else if (type == 4) {
            currentDreamTitle = "Nightmare";
            currentDreamDesc = "Dark shadows consume the island.\nYou run, but your legs are heavy.\nYou wake up in a cold sweat.";
        }
    }

    public void drawDreamScreen() {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        String text = currentDreamTitle;
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 3;
        g2.drawString(text, x, y);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        text = currentDreamDesc;
        for (String line : text.split("\n")) {
            x = getXforCenteredText(line);
            y += 40;
            g2.drawString(line, x, y);
        }
        
        y = gp.getHeight() - 100;
        text = "Press ENTER to Wake Up";
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        x = getXforCenteredText(text);
        g2.drawString(text, x, y);
    }

    public void drawSkillScreen() {
        drawSubWindow(gp.getWidth() / 2 - 300, gp.getHeight() / 2 - 200, 600, 400);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        String text = "SKILL TREE";
        int x = getXforCenteredText(text);
        int y = gp.getHeight() / 2 - 150;
        g2.drawString(text, x, y);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        g2.drawString("Available Spirit: " + gp.player.spirit, x, y + 30);

        // Skill 1: Magnet
        int startX = gp.getWidth() / 2 - 250;
        int startY = gp.getHeight() / 2 - 50;
        
        g2.drawRect(startX, startY, 500, 60);
        if (commandNum == 0) {
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRect(startX, startY, 500, 60);
            g2.setColor(Color.WHITE);
            g2.drawString(">", startX - 20, startY + 35);
        }
        
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        g2.drawString("Magnet", startX + 10, startY + 35);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        String status = gp.player.skill.magnetUnlocked ? "UNLOCKED" : "Cost: 50 Spirit";
        g2.drawString(status, startX + 350, startY + 35);
    }

    public void drawTooltip() {
        int x = gp.mouseX + 15;
        int y = gp.mouseY + 15;
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16F));
        int width = g2.getFontMetrics().stringWidth(gp.tooltipText) + 20;
        int height = 30;
        
        // Keep on screen
        if (x + width > gp.getWidth()) x = gp.getWidth() - width;
        if (y + height > gp.getHeight()) y = gp.getHeight() - height;

        // Draw Box
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.drawRoundRect(x, y, width, height, 10, 10);
        
        // Draw Text
        g2.drawString(gp.tooltipText, x + 10, y + 22);
    }

    public void drawSaveIcon(Graphics2D g2) {
        int x = gp.getWidth() - 60;
        int y = gp.getHeight() - 60;
        
        // Blink effect (every 15 frames)
        if (saveIconCounter % 30 < 15) {
            g2.setColor(new Color(255, 255, 255, 200));
            
            // Floppy Disk Body
            g2.fillRect(x, y, 40, 40);
            
            g2.setColor(Color.BLACK);
            // Label area (top)
            g2.fillRect(x + 5, y + 5, 30, 12);
            // Shutter area (bottom)
            g2.fillRect(x + 8, y + 22, 24, 18);
            
            // Slider metal
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(x + 12, y + 22, 10, 18);
            
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10F));
            g2.drawString("SAVING", x, y + 52);
        }
    }

    // --- TEST HELPERS ---
    public Rectangle getEquippedItemBounds() {
        // Slot is at y=160, size=64. Text is above it.
        // Returns approximate visual bounds: y=145 to y=224
        return new Rectangle(20, 145, gp.tileSize, gp.tileSize + 20);
    }

    public Rectangle getActiveQuestBounds() {
        // Text starts at y=260 (baseline), so top is approx 244.
        return new Rectangle(20, 244, 300, 50);
    }
}