package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import ui.UI;
import entity.InventorySlot;
import entity.OBJ_NPC;
import entity.Item;

public class KeyHandler implements KeyListener {

    public GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed;
    public boolean zPressed; // For Zoom
    public boolean tPressed, spacePressed; // T for Travel (Auto-walk), Space for Jump
    public long[] keyHoldTimes = new long[10];
    
    // Constructor
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (gp.gameState == gp.saveNamingState) {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_') {
                if (gp.ui.tempSaveName.length() < 15) {
                    gp.ui.tempSaveName += c;
                }
            }
            if (c == KeyEvent.VK_BACK_SPACE && gp.ui.tempSaveName.length() > 0) {
                gp.ui.tempSaveName = gp.ui.tempSaveName.substring(0, gp.ui.tempSaveName.length() - 1);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }

        // INTRO SKIP (Debug only)
        if (gp.gameState == gp.introState && gp.debug) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                gp.gameState = gp.playState;
            }
        }

        if (gp.gameState == gp.titleState) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 3;
                }
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 3) {
                    gp.ui.commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) {
                    System.out.println("NEW GAME");
                    gp.gameState = gp.introState;
                    gp.ui.resetIntro();
                    gp.player.spawnX = gp.tileSize * 25;
                    gp.player.spawnY = gp.tileSize * 25;
                    gp.player.setDefaultValues(); // Reset the player
                    gp.setupGame(); //Load Trees
                }
                if (gp.ui.commandNum == 1) {
                    System.out.println("Load");
                    gp.gameState = gp.loadState;
                    gp.ui.updateSaveFileList();
                }
                if (gp.ui.commandNum == 2) {
                    System.out.println("Tutorial");
                    gp.ui.showMessage("Tutorial coming soon!");
                }
                if (gp.ui.commandNum == 3) {
                    System.out.println("DevMode");
                    gp.gameState = gp.playState;
                    gp.player.spawnX = gp.tileSize * 50;
                    gp.player.spawnY = gp.tileSize * 50;
                    gp.player.setDefaultValues();
                    gp.player.ageScale = 1.0; // Adult size for Dev Mode
                     // Example of DEV MODE - Teleport and use a different map
                    gp.tileM.generateDevMap();
                    gp.setupGame();
                }

            }
        }

        // PAUSE TOGGLE
        if (code == KeyEvent.VK_ESCAPE) {
            if (gp.gameState == gp.playState) {
                System.out.println("Pause key pressed: Pausing game");
                gp.gameState = gp.pauseState;
            } else if (gp.gameState == gp.pauseState) {
                gp.gameState = gp.playState;
            }
        }

        // PAUSE STATE INPUTS
        if (gp.gameState == gp.pauseState) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = 4;
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 4) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) { // Resume
                    gp.gameState = gp.playState;
                }
                else if (gp.ui.commandNum == 1) { // Save
                    gp.gameState = gp.saveState;
                    gp.ui.updateSaveFileList();
                    gp.ui.commandNum = 0;
                }
                else if (gp.ui.commandNum == 2) { // Load
                    gp.gameState = gp.loadState;
                    gp.ui.updateSaveFileList();
                }
                else if (gp.ui.commandNum == 3) { // Settings
                    gp.gameState = gp.optionsState;
                    gp.ui.commandNum = 0;
                }
                else if (gp.ui.commandNum == 4) { // Quit
                    gp.gameState = gp.titleState;
                    gp.ui.commandNum = 0;
                }
            }
        }

        // OPTIONS STATE INPUTS
        if (gp.gameState == gp.optionsState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.pauseState;
                gp.ui.commandNum = 3; // Return to "Settings" selection
            }
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = 4; // Now 0-4
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 4) gp.ui.commandNum = 0; // Now 0-4
            }
            if (code == KeyEvent.VK_A) {
                if (gp.ui.commandNum == 0 && gp.music.volumeScale > 0) {
                    gp.music.volumeScale--;
                    gp.music.checkVolume();
                }
                if (gp.ui.commandNum == 1 && gp.se.volumeScale > 0) {
                    gp.se.volumeScale--;
                }
            }
            if (code == KeyEvent.VK_D) {
                if (gp.ui.commandNum == 0 && gp.music.volumeScale < 5) {
                    gp.music.volumeScale++;
                    gp.music.checkVolume();
                }
                if (gp.ui.commandNum == 1 && gp.se.volumeScale < 5) {
                    gp.se.volumeScale++;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 2) { // Auto Collect
                    gp.player.autoCollect = !gp.player.autoCollect;
                }
                else if (gp.ui.commandNum == 3) { // Show Timers
                    gp.ui.showStatusTimer = !gp.ui.showStatusTimer;
                }
                else if (gp.ui.commandNum == 4) { // Back
                    gp.gameState = gp.pauseState;
                    gp.ui.commandNum = 3;
                }
            }
        }

        // SAVE STATE INPUTS
        if (gp.gameState == gp.saveState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.pauseState;
            }
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = gp.ui.saveFiles.size();
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > gp.ui.saveFiles.size()) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) { // Create New
                    gp.gameState = gp.saveNamingState;
                    gp.ui.tempSaveName = "";
                } else { // Overwrite existing
                    gp.saveGame(gp.ui.saveFiles.get(gp.ui.commandNum - 1));
                    gp.gameState = gp.playState;
                }
            }
        }

        // SAVE NAMING STATE INPUTS
        if (gp.gameState == gp.saveNamingState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.saveState;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.tempSaveName.length() > 0) {
                    gp.saveGame("born_with_stars_" + gp.ui.tempSaveName + ".dat");
                    gp.gameState = gp.playState;
                }
            }
        }

        // LOAD STATE INPUTS
        if (gp.gameState == gp.loadState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.titleState;
            }
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = Math.max(0, gp.ui.saveFiles.size() - 1);
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum >= gp.ui.saveFiles.size()) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (!gp.ui.saveFiles.isEmpty()) {
                    if (gp.loadGame(gp.ui.saveFiles.get(gp.ui.commandNum))) {
                        gp.gameState = gp.playState;
                    }
                }
            }
        }

        // CRAFTING STATE INPUTS
        if (gp.gameState == gp.craftingState) {
            if (code == KeyEvent.VK_C || code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = 2; // 3 recipes
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 2) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                gp.ui.craftSelected();
            }
        }

        // GAME OVER STATE INPUTS
        if (gp.gameState == gp.gameOverState) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = 2;
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 2) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) { // Load
                    if (gp.loadGame(gp.currentSaveFileName)) { // Try loading last used save
                        gp.gameState = gp.playState;
                    } else {
                        gp.gameState = gp.playState;
                        gp.player.setDefaultValues();
                        gp.setupGame();
                    }
                }
                else if (gp.ui.commandNum == 1) { // Sacrifice
                    if (!gp.player.inventorySlots.isEmpty()) {
                        int slotIndex = (int)(Math.random() * gp.player.inventorySlots.size());
                        InventorySlot slot = gp.player.inventorySlots.get(slotIndex);
                        int itemIndex = (int)(Math.random() * slot.items.size());
                        Item item = slot.items.get(itemIndex);
                        String itemName = item.name;
                        
                        slot.remove(item);
                        if (slot.items.isEmpty()) {
                            gp.player.inventorySlots.remove(slotIndex);
                        }
                        
                        gp.player.life = gp.player.maxLife / 2;
                        gp.player.energy = gp.player.maxEnergy / 2;
                        gp.gameState = gp.playState;
                        gp.ui.showMessage("Sacrificed " + itemName + "!");
                    } else {
                        gp.ui.showMessage("Nothing to sacrifice!");
                    }
                }
                else if (gp.ui.commandNum == 2) { // Quit
                    gp.gameState = gp.titleState;
                    gp.ui.commandNum = 0;
                }
            }
        }
        
        // QUEST STATE
        if (gp.gameState == gp.questState) {
            if (code == KeyEvent.VK_Q || code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }
        }
        
        // DIALOGUE STATE
        if (gp.gameState == gp.dialogueState) {
            if (code == KeyEvent.VK_ENTER) {
                if (gp.currentSpeaker != null) {
                    gp.currentSpeaker.speak();
                } else {
                    gp.gameState = gp.playState;
                }
            }
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }
        }

        // TRADE STATE
        if (gp.gameState == gp.tradeState) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) gp.ui.commandNum = 4; // Assuming 5 items max for now
            }
            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 4) gp.ui.commandNum = 0;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.npc instanceof OBJ_NPC) {
                    OBJ_NPC chief = (OBJ_NPC)gp.ui.npc;
                    if (gp.ui.commandNum < chief.inventory.size()) {
                        Item itemToBuy = chief.inventory.get(gp.ui.commandNum);
                        if (gp.player.spirit >= itemToBuy.cost) {
                            if (gp.player.pickUpItem(gp.player.createItem(itemToBuy.name))) {
                                gp.player.spirit -= itemToBuy.cost;
                                gp.ui.showMessage("Traded for " + itemToBuy.name);
                            } else {
                                gp.ui.showMessage("Inventory Full!");
                            }
                        } else {
                            gp.ui.showMessage("Not enough Spirit!");
                        }
                    }
                }
            }
        }
        
        // SKILL STATE
        if (gp.gameState == gp.skillState) {
            if (code == KeyEvent.VK_U || code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) {
                    gp.player.skill.unlock("Magnet");
                }
                // Add more indices for more skills
            }
        }

        // PLAY STATE INPUTS
        if (gp.gameState == gp.playState) {
            // MOVEMENT
            if (code == KeyEvent.VK_W) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_A) {
                leftPressed = true;
            }
            if (code == KeyEvent.VK_D) {
                rightPressed = true;
            }

            // ZOOM MODIFIER
            if (code == KeyEvent.VK_Z) {
                zPressed = true;
            }
            
            // AUTO-WALK MODIFIER
            if (code == KeyEvent.VK_T) {
                tPressed = true;
            }
            // JUMP
            if (code == KeyEvent.VK_SPACE) {
                spacePressed = true;
            }
            
            // INTERACT / ATTACK
            if (code == KeyEvent.VK_ENTER) {
                gp.player.attack();
            }

            // MINI-MAP TOGGLES
            if (code == KeyEvent.VK_M) {
                // Toggles whether the map is drawn at all
                gp.ui.toggleMiniMap();
            }
            if (code == KeyEvent.VK_N) {
                // Toggles between Local (centered) and Full (world) view
                gp.ui.toggleViewMode();
            }
            
            // QUEST LOG
            if (code == KeyEvent.VK_Q) {
                gp.gameState = gp.questState;
            }

            // DEBUG: DAY/NIGHT TOGGLE
            if (code == KeyEvent.VK_K) {
                gp.environmentM.toggleDayNight();
            }

            // SLEEP
            if (code == KeyEvent.VK_F) {
                gp.player.sleep();
            }
            
            // SPIRIT COMPANION
            if (code == KeyEvent.VK_X) {
                gp.player.toggleSpiritCompanion();
            }

            // DEV LEGEND
            if (code == KeyEvent.VK_H) {
                gp.ui.showDevLegend = !gp.ui.showDevLegend;
            }
            
            // SKILL TREE
            if (code == KeyEvent.VK_U) {
                gp.gameState = gp.skillState;
                gp.ui.commandNum = 0;
            }
        }

        if (gp.gameState == gp.playState) {
            // INVENTORY
            if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
                int index = code - KeyEvent.VK_0;
                if (index == 0) index = 9; // 0 key is index 9
                else index -= 1; // 1 key is index 0
                
                if (keyHoldTimes[index] == 0) {
                    keyHoldTimes[index] = System.currentTimeMillis();
                }
            }
            
            // TOGGLE AUTO-COLLECT
            if (code == KeyEvent.VK_V) {
                gp.player.autoCollect = !gp.player.autoCollect;
                gp.ui.showMessage("Auto-Collect: " + (gp.player.autoCollect ? "ON" : "OFF"));
            }
            
            // CRAFTING MENU
            if (code == KeyEvent.VK_C) {
                gp.gameState = gp.craftingState;
                gp.ui.commandNum = 0;
            }

            // EXPORT MAP DATA
            if (code == KeyEvent.VK_P) {
                System.out.println("Exporting map data to console...");
                gp.tileM.exportMapToConsole();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }

        // ZOOM MODIFIER RELEASE
        if (code == KeyEvent.VK_Z) {
            zPressed = false;
        }
        if (code == KeyEvent.VK_T) {
            tPressed = false;
        }
        if (code == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = false;
        }
        
        // INVENTORY RELEASE
        if (gp.gameState == gp.playState) {
            if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
                int index = code - KeyEvent.VK_0;
                if (index == 0) index = 9;
                else index -= 1;
                
                long duration = System.currentTimeMillis() - keyHoldTimes[index];
                keyHoldTimes[index] = 0;
                
                if (duration < 500) { // Short press
                    gp.player.selectItem(index);
                }
            }
        }
    }

    public void resetKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        zPressed = false;
        tPressed = false;
        spacePressed = false;
        enterPressed = false;
    }
}