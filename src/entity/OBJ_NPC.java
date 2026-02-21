package entity;

import main.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class OBJ_NPC extends Entity {

    GamePanel gp;
    public String npcType;
    
    // Quest Giver specific
    String[] dialogues = new String[20];
    int dialogueIndex = 0;
    
    // Chief/Merchant specific
    public ArrayList<Item> inventory = new ArrayList<>();

    public OBJ_NPC(GamePanel gp, String npcType, int col, int row) {
        this.gp = gp;
        this.npcType = npcType;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = npcType;
        this.type = TYPE_NPC;
        this.speed = 0;
        this.direction = "down";
        this.collision = true;
        this.talkable = true;
        
        this.solidArea = new Rectangle(8, 16, 48, 48);
        this.solidAreaDefaultX = solidArea.x;
        this.solidAreaDefaultY = solidArea.y;
        this.life = 20; // Default life

        generateProperties();
    }

    private void generateProperties() {
        down1 = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = down1.createGraphics();

        if (npcType.equals("Old Man")) {
            // Visuals (Simple Cyan Rect)
            g2.setColor(Color.CYAN);
            g2.fillRect(8, 8, gp.tileSize-16, gp.tileSize-16);
            // Eyes
            g2.setColor(Color.BLACK);
            g2.fillRect(20, 20, 6, 6);
            g2.fillRect(38, 20, 6, 6);
            
            setDialogue();
            this.life = 20;
        } 
        else if (npcType.equals("Chief")) {
            this.life = 100;
            // Visuals (Red/Brown Rect with headdress hint)
            g2.setColor(new Color(160, 82, 45)); // Sienna skin
            g2.fillRect(16, 16, 32, 48);
            // Headdress
            g2.setColor(Color.WHITE);
            g2.fillRect(14, 10, 36, 10);
            g2.setColor(Color.RED);
            g2.fillRect(14, 10, 6, 10);
            g2.fillRect(44, 10, 6, 10);
            
            setItems();
        }
        g2.dispose();
    }

    public void setDialogue() {
        dialogues[0] = "Hello there, traveler.";
        dialogues[1] = "I have a task for you.";
        dialogues[2] = "Please bring me 3 Stones.";
        dialogues[3] = "The island is dangerous at night.";
    }
    
    public void setItems() {
        inventory.add(gp.player.createItem("Potion"));
        inventory.add(gp.player.createItem("Key"));
        inventory.add(gp.player.createItem("Sword"));
        inventory.add(gp.player.createItem("Bow"));
        inventory.add(gp.player.createItem("Arrow"));
    }

    @Override
    public void update() {
        super.update();
        if (npcType.equals("Old Man")) {
            this.life = 20; // God Mode
        }
    }

    @Override
    public void speak() {
        if (npcType.equals("Old Man")) {
            // Init conversation state if coming from playState
            if (gp.gameState == gp.playState) {
                gp.gameState = gp.dialogueState;
                gp.currentSpeaker = this;
            }

            if (dialogues[dialogueIndex] == null) {
                dialogueIndex = 0;
                gp.gameState = gp.playState;
                gp.currentSpeaker = null;
                return;
            }
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            dialogueIndex++;
            
            // Give Quest logic (Simple check)
            boolean hasQuest = false;
            for(Quest q : gp.questM.questList) {
                if(q.name.equals("Stone Collector")) {
                    hasQuest = true;
                    break;
                }
            }
            
            if(!hasQuest && dialogueIndex == 3) { // Give quest after "Please bring me 3 Stones"
                Quest q = new Quest("Stone Collector", "Collect 3 Stones for the Old Man.", Quest.TYPE_COLLECT, "Stone", 3);
                q.rewardItem = gp.player.createItem("Potion");
                gp.questM.addQuest(q);
            }
        } else if (npcType.equals("Chief")) {
            gp.gameState = gp.tradeState;
            gp.ui.npc = this;
        }
    }
}