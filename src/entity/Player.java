package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import main.GamePanel;
import main.KeyHandler;
import main.Node;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    public ArrayList<InventorySlot> inventorySlots = new ArrayList<>();
    public int maxInventorySize = 30; // Start with 30 slots (3 rows)
    public Item currentWeapon;

    // JUMPING PHYSICS
    public boolean jumping = false;
    public double z = 0; // Height off the ground
    public double verticalVelocity = 0;
    public int currentMapHeight = 0;
    
    // AUTO-WALK
    public boolean onPath = false;
    
    // SETTINGS
    public boolean autoCollect = false;
    public boolean wet = false;
    public int wetCounter = 0;
    
    // ENERGY
    public int maxEnergy;
    public int energy;
    public int energyCounter = 0;
    
    // SPAWN POINT
    public int spawnX;
    public int spawnY;
    
    // GROWTH
    public double ageScale = 1.0; // 1.0 = Adult, 0.4 = Child

    // STATS
    public int shotsFired = 0;
    public int shotsHit = 0;
    public int rabbitsKilled = 0;
    public int spirit = 0;
    
    // BUFFS
    public int speedBuffTimer = 0;
    public int strengthBuffTimer = 0;
    public int pendingDreamType = 0; // 0:None, 1:Bear, 2:Eagle, 3:Wolf, 4:Nightmare
    public Entity spiritCompanion;
    public SkillManager skill;
    
    // ATTACK ANIMATION
    public boolean attacking = false;
    public int attackTimer = 0;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        // Center of the screen
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        
        skill = new SkillManager(gp);

        // HITBOX: Crucial for CollisionChecker to work
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        // Use spawn points if set, otherwise default to 25,25
        if (spawnX != 0 || spawnY != 0) {
            worldX = spawnX;
            worldY = spawnY;
        } else {
            worldX = gp.tileSize * 25;
            worldY = gp.tileSize * 25;
        }
        
        // Safety Clamp: Ensure we never spawn off-map
        if (worldX < 0) worldX = gp.tileSize * 25;
        if (worldY < 0) worldY = gp.tileSize * 25;
        if (worldX >= gp.maxWorldCol * gp.tileSize) worldX = (gp.maxWorldCol - 1) * gp.tileSize;
        if (worldY >= gp.maxWorldRow * gp.tileSize) worldY = (gp.maxWorldRow - 1) * gp.tileSize;

        speed = 4;
        direction = "down";

        ageScale = 0.4; // Start as child
        // PLAYER STATUS
        maxLife = 6; // 6 units (e.g., 3 hearts or just a bar of 6)
        life = maxLife;
        maxEnergy = 20;
        energy = maxEnergy;
        currentMapHeight = 0;
        spirit = 100; // Start with strong spirit

        // TEST INVENTORY ITEMS
        inventorySlots.clear();
        pickUpItem(createItem("Sword"));
        pickUpItem(createItem("Potion"));
        pickUpItem(createItem("Tomahawk"));
        pickUpItem(createItem("Key"));
        pickUpItem(createItem("Bow"));
        Item arrows = createItem("Arrow");
        arrows.amount = 5;
        pickUpItem(arrows);
    }

    public void getPlayerImage() {
        int w = gp.tileSize;
        int h = gp.tileSize;
        
        up1 = generateSprite("up", 1, w, h);
        up2 = generateSprite("up", 2, w, h);
        down1 = generateSprite("down", 1, w, h);
        down2 = generateSprite("down", 2, w, h);
        left1 = generateSprite("left", 1, w, h);
        left2 = generateSprite("left", 2, w, h);
        right1 = generateSprite("right", 1, w, h);
        right2 = generateSprite("right", 2, w, h);
    }
    
    public BufferedImage generateSprite(String dir, int frame, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        
        // Anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int cx = w / 2;
        int cy = h / 2;
        
        // Colors
        Color skin = new Color(160, 82, 45); // Reddish-Brown
        Color shirt = new Color(139, 90, 43); // Dark Tan Vest
        Color pants = new Color(50, 50, 70); // Dark Blue/Grey
        Color shoes = new Color(30, 30, 30);
        
        // LEGS (Animation)
        int legW = 10;
        int legH = 18;
        int legY = cy + 10;
        int leftLegOffset = (frame == 1) ? -4 : 4;
        int rightLegOffset = (frame == 1) ? 4 : -4;
        
        if (dir.equals("left") || dir.equals("right")) {
            // Profile legs
             g2.setColor(pants);
             g2.fillRoundRect(cx - 4 + leftLegOffset, legY, legW, legH, 5, 5);
             g2.setColor(shoes);
             g2.fillRoundRect(cx - 4 + leftLegOffset, legY + 14, legW, 6, 5, 5);
        } else {
            // Front/Back legs
            g2.setColor(pants);
            g2.fillRoundRect(cx - 12, legY + leftLegOffset, legW, legH, 5, 5); // Left
            g2.fillRoundRect(cx + 2, legY + rightLegOffset, legW, legH, 5, 5); // Right
            
            g2.setColor(shoes);
            g2.fillRoundRect(cx - 12, legY + leftLegOffset + 14, legW, 6, 5, 5);
            g2.fillRoundRect(cx + 2, legY + rightLegOffset + 14, legW, 6, 5, 5);
        }

        // BODY
        g2.setColor(shirt);
        g2.fillRoundRect(cx - 14, cy - 10, 28, 26, 10, 10);
        
        // HEAD
        g2.setColor(skin);
        g2.fillOval(cx - 14, cy - 34, 28, 28);
        
        // FACE & ARMS
        if (dir.equals("down")) {
            g2.setColor(Color.BLACK);
            g2.fillOval(cx - 8, cy - 24, 4, 4); // Eyes
            g2.fillOval(cx + 4, cy - 24, 4, 4);
            g2.drawArc(cx - 4, cy - 20, 8, 6, 0, -180); // Smile
        } else if (dir.equals("left")) {
            g2.setColor(Color.BLACK);
            g2.fillOval(cx - 12, cy - 24, 4, 4);
        } else if (dir.equals("right")) {
            g2.setColor(Color.BLACK);
            g2.fillOval(cx + 8, cy - 24, 4, 4);
        }
        
        // ARMS
        g2.setColor(skin);
        if (dir.equals("down") || dir.equals("up")) {
            g2.fillRoundRect(cx - 20, cy - 8 + rightLegOffset, 8, 20, 4, 4);
            g2.fillRoundRect(cx + 12, cy - 8 + leftLegOffset, 8, 20, 4, 4);
        } else {
            g2.fillRoundRect(cx - 4, cy - 6, 8, 20, 4, 4);
        }

        g2.dispose();
        return img;
    }

    // Simple Factory to create items by name
    public Item createItem(String itemName) {
        Item item = new Item();
        item.name = itemName;
        
        // Assign types based on name (Placeholder logic)
        if (itemName.equals("Potion")) {
            item.type = Item.TYPE_CONSUMABLE;
            item.category = Item.CATEGORY_MEDICINE;
            item.value = 2; // Heals 2 units
            item.cost = 10;
        } else if (itemName.equals("Bread")) {
            item.type = Item.TYPE_CONSUMABLE;
            item.category = Item.CATEGORY_FOOD;
            item.value = 2; // Heals 2 units
            item.cost = 5;
        } else if (itemName.equals("Sword")) {
            item.type = Item.TYPE_SWORD;
            item.category = Item.CATEGORY_SWORD;
            item.value = 4; // Attack power
            item.durability = 50;
            item.maxDurability = 50;
            item.cost = 30;
        } else if (itemName.equals("Shield")) {
            item.type = Item.TYPE_SWORD; // Keeping type logic for now
            item.category = Item.CATEGORY_WEAPON;
            item.value = 4; // Attack power
            item.durability = 50;
            item.maxDurability = 50;
        } else if (itemName.equals("Tomahawk")) {
            item.type = Item.TYPE_AXE;
            item.category = Item.CATEGORY_TOOL;
            item.value = 2; // Attack power
            item.durability = 40;
            item.maxDurability = 40;
            item.cost = 20;
        } else if (itemName.equals("Wood")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_MISC;
            item.value = 1;
            item.stackable = true;
        } else if (itemName.equals("Branch")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_MISC;
            item.value = 1;
            item.stackable = true;
        } else if (itemName.equals("Berry")) {
            item.type = Item.TYPE_FOOD;
            item.category = Item.CATEGORY_FRUIT;
            item.value = 5; // Restores 5 Energy
            item.stackable = true;
        } else if (itemName.equals("Bow")) {
            item.type = Item.TYPE_BOW;
            item.category = Item.CATEGORY_RANGED;
            item.value = 3; // Bow damage
            item.cost = 25;
        } else if (itemName.equals("Arrow")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_RANGED;
            item.stackable = true;
            item.cost = 2;
        } else if (itemName.equals("Stone")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_RESOURCE;
            item.stackable = true;
        } else if (itemName.equals("Flint")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_RESOURCE;
            item.stackable = true;
        } else if (itemName.equals("Pickaxe")) {
            item.type = Item.TYPE_PICKAXE;
            item.category = Item.CATEGORY_TOOL;
            item.value = 2; // Low attack damage
            item.durability = 50;
            item.maxDurability = 50;
        } else if (itemName.contains("(Raw)")) {
            item.type = Item.TYPE_MISC; // Raw meat is not edible directly
            item.category = Item.CATEGORY_FOOD; // Group with food
            item.stackable = true;
        } else if (itemName.contains("(Cooked)")) {
            item.type = Item.TYPE_FOOD;
            item.category = Item.CATEGORY_FOOD;
            item.value = 6; // Restores energy
            item.stackable = true;
        } else if (itemName.equals("Fire Pit")) {
            item.type = Item.TYPE_PLACABLE;
            item.category = Item.CATEGORY_TOOL;
            item.stackable = true;
        } else if (itemName.contains("Fur")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_RESOURCE;
            item.stackable = true;
        } else if (itemName.equals("Bone")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_RESOURCE;
            item.stackable = true;
        } else if (itemName.equals("Key")) {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_SPECIAL;
            item.stackable = true;
            item.cost = 50;
        } else {
            item.type = Item.TYPE_MISC;
            item.category = Item.CATEGORY_MISC;
        }
        return item;
    }

    public void selectItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventorySlots.size()) {
            InventorySlot slot = inventorySlots.get(slotIndex);
            Item item = slot.getSelectedItem();
            
            if (item == null) return;

            item.use(this);
            
            // Example: Remove consumable after use
            if (item.type == Item.TYPE_CONSUMABLE || item.type == Item.TYPE_FOOD) {
                if (item.amount > 1) {
                    item.amount--;
                } else {
                    slot.remove(item);
                    if (slot.items.isEmpty()) inventorySlots.remove(slot);
                }
            } else if (item.type == Item.TYPE_PLACABLE) {
                if (item.name.equals("Fire Pit")) {
                    // Calculate placement position (1 tile in front)
                    int placeX = worldX;
                    int placeY = worldY;
                    switch(direction) {
                        case "up": placeY -= gp.tileSize; break;
                        case "down": placeY += gp.tileSize; break;
                        case "left": placeX -= gp.tileSize; break;
                        case "right": placeX += gp.tileSize; break;
                    }
                    
                    // Align to grid
                    int col = (placeX + gp.tileSize/2) / gp.tileSize;
                    int row = (placeY + gp.tileSize/2) / gp.tileSize;
                    
                    // Place it (Basic check, overwrites for now or stacks)
                    gp.obj.add(new OBJ_FirePit(gp, col, row));
                    slot.remove(item);
                    if (slot.items.isEmpty()) inventorySlots.remove(slot);
                    System.out.println("Placed Fire Pit");
                }
            } else if (item.type == Item.TYPE_SWORD || item.type == Item.TYPE_AXE || item.type == Item.TYPE_BOW || item.type == Item.TYPE_PICKAXE) {
                currentWeapon = item;
                System.out.println("Equipped: " + item.name);
            }
        }
    }

    public void attack() {
        // 1. Calculate the area being attacked (in front of player)
        int currentWorldX = worldX;
        int currentWorldY = worldY;
        int solidAreaWidth = solidArea.width;
        int solidAreaHeight = solidArea.height;

        // Temporarily shift player hitbox to check collision in front
        switch(direction) {
            case "up": worldY -= solidArea.height; break;
            case "down": worldY += solidArea.height; break;
            case "left": worldX -= solidArea.width; break;
            case "right": worldX += solidArea.width; break;
        }
        
        solidArea.width = 32;
        solidArea.height = 32;

        int objIndex = gp.cChecker.checkObject(this, true);

        // Restore player position
        worldX = currentWorldX;
        worldY = currentWorldY;
        solidArea.width = solidAreaWidth;
        solidArea.height = solidAreaHeight;

        // 2. CHECK INTERACTIONS (No weapon needed)
        if (objIndex != 999) {
            Entity target = gp.obj.get(objIndex);
            
            // Crafting Table
            if (target.name.equals("Crafting Table")) {
                gp.gameState = gp.craftingState;
                gp.ui.commandNum = 0;
                return;
            }

            // Tipi Interaction (Enter)
            if (target.name.equals("Tipi")) {
                gp.enterTipi(target.worldX + gp.tileSize/2, target.worldY + gp.tileSize);
                return;
            }
            
            // NPC Interaction
            if (target.type == TYPE_NPC && target.talkable) {
                target.speak();
                return;
            }

            // Fire Pit (Cooking)
            if (target.name.equals("Fire Pit")) {
                // Cooking Mechanic
                Item rawMeat = null;
                rawMeat = findItemWithSubstring("(Raw)");

                if (rawMeat != null) {
                    String cookedName = rawMeat.name.replace("(Raw)", "(Cooked)");
                    // Remove raw
                    for(InventorySlot slot : inventorySlots) {
                        if(slot.items.contains(rawMeat)) {
                            slot.remove(rawMeat);
                            if(slot.items.isEmpty()) inventorySlots.remove(slot);
                            break;
                        }
                    }
                    pickUpItem(createItem(cookedName));
                    gp.ui.showMessage("Cooked " + cookedName + "!");
                } else {
                    gp.ui.showMessage("No raw meat to cook!");
                }
                return; 
            }
        }

        // 3. COMBAT CHECKS
        if (currentWeapon == null) {
            System.out.println("You need a weapon to attack!");
            return;
        }
        
        attacking = true;
        attackTimer = 0;

        // Energy Cost for swinging
        energyCounter += 50; // Equivalent to ~0.8 seconds of passive drain

        // BOW ATTACK
        if (currentWeapon.type == Item.TYPE_BOW) {
            // Check for Arrows
            Item arrow = findItemInInventory("Arrow");
            
            if (arrow != null) {
                gp.projectiles.add(new Projectile(gp, this, currentWeapon.value));
                shotsFired++;
                arrow.amount--;
                if (arrow.amount <= 0) {
                    // Remove arrow from its slot
                    for(InventorySlot slot : inventorySlots) {
                        if(slot.items.contains(arrow)) {
                            slot.remove(arrow);
                            if(slot.items.isEmpty()) inventorySlots.remove(slot);
                            break;
                        }
                    }
                }
                System.out.println("Fired arrow!");
                gp.playSE(3); // Use Thwack sound for bow release for now, or add a specific bow sound
            } else {
                System.out.println("Out of arrows!");
            }
            return; // Don't do melee attack
        }

        // 4. MELEE ATTACK
        if (objIndex != 999) {
            Entity target = gp.obj.get(objIndex);
            
            // ELEVATION CHECK
            int targetCol = (target.worldX + gp.tileSize/2) / gp.tileSize;
            int targetRow = (target.worldY + gp.tileSize/2) / gp.tileSize;
            if (targetCol >= 0 && targetCol < gp.maxWorldCol && targetRow >= 0 && targetRow < gp.maxWorldRow) {
                int targetHeight = gp.tileM.tileHeightMap[gp.currentMap][targetCol][targetRow];
                if (targetHeight > currentMapHeight) {
                    gp.ui.showMessage("Too high to reach!");
                    return;
                }
            }

            if (target.type == TYPE_OBSTACLE) {
                if (target.name.equals("Tree")) {
                    int damage = 0;
                    if (currentWeapon.type == Item.TYPE_AXE) damage = 4; // Axes chop trees well (3 hits)
                    if (currentWeapon.type == Item.TYPE_PICKAXE) damage = 1; // Pickaxe bad at trees
                    if (currentWeapon.type == Item.TYPE_SWORD) damage = 1; // Swords are bad at chopping (10 hits)

                    target.life -= damage;
                    currentWeapon.durability--;
                    System.out.println("Hit Tree with " + currentWeapon.name + "! Tree HP: " + target.life);
                    gp.playSE(2); // Hit sound

                    if (target.life <= 0) {
                        gp.scheduleRespawn(target, 60000); // Respawn tree in 60s
                        gp.obj.remove(objIndex);
                        gp.questM.updateKill(target.name); // Quest Trigger
                        System.out.println("Tree chopped down!");
                        gp.playSE(6); // Break sound
                        handleDrop(target);
                    }
                } else if (target.name.equals("Berry Bush")) {
                    target.life = 0; // Bushes are fragile
                    gp.scheduleRespawn(target, 120000); // Respawn bush in 2 mins
                    gp.obj.remove(objIndex);
                    gp.questM.updateKill(target.name); // Quest Trigger (Harvesting counts as kill for obstacles)
                    System.out.println("Harvested Berry Bush!");
                    gp.playSE(6); // Break sound
                    currentWeapon.durability--;
                                       handleDrop(target);
                } else if (target.name.equals("Rock") || target.name.equals("Flint Vein")) {
                    int damage = 0;
                    int durabilityCost = 1;

                    if (currentWeapon.type == Item.TYPE_PICKAXE) {
                        damage = 4; // Pickaxe breaks rocks efficiently
                        durabilityCost = 1;
                    } else if (currentWeapon.type == Item.TYPE_AXE) {
                        damage = 1; // Tomahawk chips at it poorly
                        durabilityCost = 4; // And gets damaged heavily
                    } else {
                        damage = 0;
                    }

                    if (damage > 0) {
                        target.life -= damage;
                        System.out.println("Hit " + target.name + " with " + currentWeapon.name + " for " + damage + " damage.");
                        if (Math.random() < 0.5) gp.playSE(7);
                        else gp.playSE(8);
                        currentWeapon.durability -= durabilityCost;
                        if (target.life <= 0) {
                            gp.scheduleRespawn(target, 180000); // Respawn rock in 3 mins
                            gp.obj.remove(objIndex);
                            gp.questM.updateKill(target.name); // Quest Trigger
                            gp.playSE(6); // Break sound
                            handleDrop(target);
                        }
                    } else {
                        gp.playSE(4); // Tink sound (ineffective)
                    }
                }
            }
        } else {
            System.out.println("Swung " + currentWeapon.name + " at nothing.");
            gp.playSE(1); // Swing sound
        }
    }

    // Mouse-based Attack
    public void attack(Entity target) {
        if (currentWeapon == null) {
            return;
        }
        
        attacking = true;
        attackTimer = 0;

        // Energy Cost for swinging
        energyCounter += 50;

        // Check Distance (Reach)
        double dist = Math.sqrt(Math.pow(worldX - target.worldX, 2) + Math.pow(worldY - target.worldY, 2));
        if (dist > gp.tileSize * 1.5) { // Reach of ~1.5 tiles (adjacent)
            return;
        }

        if (!target.invincible) {
            // INTERACT WITH NPC
            if (target.type == TYPE_NPC && target.talkable) {
                target.speak();
                return;
            }
            
            if (target.name.equals("Crafting Table")) {
                gp.gameState = gp.craftingState;
                gp.ui.commandNum = 0;
                return;
            }
            
            // Tipi Interaction (Enter)
            if (target.name.equals("Tipi")) {
                gp.enterTipi(target.worldX + gp.tileSize/2, target.worldY + gp.tileSize);
                return;
            }

            // ELEVATION CHECK
            int targetCol = (target.worldX + gp.tileSize/2) / gp.tileSize;
            int targetRow = (target.worldY + gp.tileSize/2) / gp.tileSize;
            if (targetCol >= 0 && targetCol < gp.maxWorldCol && targetRow >= 0 && targetRow < gp.maxWorldRow) {
                int targetHeight = gp.tileM.tileHeightMap[gp.currentMap][targetCol][targetRow];
                boolean isRanged = currentWeapon != null && currentWeapon.type == Item.TYPE_BOW;
                if (targetHeight > currentMapHeight && !isRanged) {
                    gp.ui.showMessage("Too high to reach!");
                    return;
                }
            }

            int damage = currentWeapon.value;
            if (strengthBuffTimer > 0) damage += 2; // Strength Buff
            int durabilityCost = 1;
            
            // Type effectiveness
            if (target.type == TYPE_OBSTACLE) {
                if (target.name.equals("Fire Pit")) {
                    // Cooking Mechanic
                    Item rawMeat = findItemWithSubstring("(Raw)");
                    
                    if (rawMeat != null) {
                        String cookedName = rawMeat.name.replace("(Raw)", "(Cooked)");
                        // Remove raw
                        for(InventorySlot slot : inventorySlots) {
                            if(slot.items.contains(rawMeat)) {
                                slot.remove(rawMeat);
                                if(slot.items.isEmpty()) inventorySlots.remove(slot);
                                break;
                            }
                        }
                        pickUpItem(createItem(cookedName));
                        gp.ui.showMessage("Cooked " + cookedName + "!");
                    } else {
                        gp.ui.showMessage("No raw meat to cook!");
                    }
                    return;
                }
                if (target.name.equals("Tree")) {
                    if (currentWeapon.type == Item.TYPE_AXE) damage = 4; // Axe chops fast (3 hits)
                    else if (currentWeapon.type == Item.TYPE_PICKAXE) damage = 1;
                } else if (target.name.equals("Rock") || target.name.equals("Flint Vein")) {
                    if (currentWeapon.type == Item.TYPE_PICKAXE) {
                        damage = 4;
                        durabilityCost = 1;
                    } else if (currentWeapon.type == Item.TYPE_AXE) {
                        damage = 1;
                        durabilityCost = 4;
                    } else {
                        damage = 0;
                    }
                }
            }

            if (damage > 0) {
                target.life -= damage;
                target.invincible = true; // Trigger shake
                currentWeapon.durability -= durabilityCost;
                System.out.println("Hit " + target.name + " with " + currentWeapon.name + " for " + damage + " damage.");
                
                if (target.name.equals("Rock") || target.name.equals("Flint Vein")) {
                    if (Math.random() < 0.5) gp.playSE(7);
                    else gp.playSE(8);
                } else {
                    gp.playSE(2); // Hit sound
                }
                
                if (target.life <= 0) {
                    // Stats
                    if (target.name.equals("Rabbit")) rabbitsKilled++;
                    
                    // Regrowth
                    if (target.type == TYPE_OBSTACLE) {
                        gp.scheduleRespawn(target, 60000);
                    }
                    gp.obj.remove(target);
                    gp.questM.updateKill(target.name); // Quest Trigger
                    System.out.println(target.name + " destroyed!");
                    gp.playSE(6); // Break sound
                    handleDrop(target);
                }
            }
        }
    }
    
    public void handleDrop(Entity target) {
        target.getDrops(gp);
    }

    public void dropItem(Item item, int x, int y) {
        // Add small random offset so items don't stack perfectly
        int offsetX = (int)(Math.random() * 20) - 10;
        int offsetY = (int)(Math.random() * 20) - 10;

        if (autoCollect && pickUpItem(item)) {
            System.out.println("Auto-collected " + item.name + "!");
            gp.playSE(5); // Pickup sound
        } else {
            gp.obj.add(new OBJ_DroppedItem(gp, item, x + offsetX, y + offsetY));
        }
    }

    public boolean pickUpItem(Item item) {
        // Check for existing stack
        if (item.stackable) {
            for (InventorySlot slot : inventorySlots) {
                for (Item i : slot.items) {
                    if (i.name.equals(item.name)) {
                        i.amount += item.amount;
                        gp.questM.updateCollection(item.name); // Quest Trigger
                        return true;
                    }
                }
            }
        }
        
        // Check for existing slot with same category
        for (InventorySlot slot : inventorySlots) {
            if (slot.category == item.category) {
                slot.addItem(item);
                gp.questM.updateCollection(item.name); // Quest Trigger
                return true;
            }
        }

        // Create new slot
        if (inventorySlots.size() < maxInventorySize) {
            InventorySlot newSlot = new InventorySlot(item.category);
            newSlot.addItem(item);
            inventorySlots.add(newSlot);
            gp.questM.updateCollection(item.name); // Quest Trigger
            return true;
        }
        return false;
    }

    public void sleep() {
        boolean nearFire = false;
        for (Entity e : gp.obj) {
            if (e != null && e.name.equals("Fire Pit")) {
                double dist = Math.sqrt(Math.pow(worldX - e.worldX, 2) + Math.pow(worldY - e.worldY, 2));
                if (dist < gp.tileSize * 2) {
                    nearFire = true;
                    break;
                }
            }
        }

        if (nearFire) {
            if (gp.environmentM.dayCounter > 20000) {
                // Chance for Dream
                double dreamChance = 0.30;
                if (spirit < 30) dreamChance = 0.50; // Restless sleep if low spirit

                if (Math.random() < dreamChance) {
                    gp.gameState = gp.dreamState;
                    
                    // Determine Dream Type
                    if (spirit < 30 && Math.random() < 0.7) {
                        pendingDreamType = 4; // Nightmare
                    } else {
                        pendingDreamType = (int)(Math.random() * 3) + 1; // 1, 2, or 3
                    }
                    gp.ui.startDream(pendingDreamType);
                } else {
                    wakeUp();
                }
            } else {
                gp.ui.showMessage("Not tired yet.");
            }
        } else {
            gp.ui.showMessage("Need a fire to sleep.");
        }
    }

    public void wakeUp() {
        gp.environmentM.setMorning();
        life = maxLife;
        energy = maxEnergy;
        
        // Apply Dream Effects
        if (pendingDreamType == 1) { // Bear
            strengthBuffTimer = 3600; // 1 minute
            gp.ui.showMessage("Spirit of the Bear: Strength Up!");
        } else if (pendingDreamType == 2 || pendingDreamType == 3) { // Eagle/Wolf
            speedBuffTimer = 3600;
            gp.ui.showMessage("Spirit of the Wild: Speed Up!");
        } else if (pendingDreamType == 4) { // Nightmare
            spirit -= 15;
            if (spirit < 0) spirit = 0;
            energy = maxEnergy / 2; // Wake up tired
            gp.ui.showMessage("Nightmare: Spirit drained...");
        } else {
            gp.ui.showMessage("Rested until morning.");
        }
        pendingDreamType = 0;
    }

    public void toggleSpiritCompanion() {
        if (spiritCompanion == null) {
            // Summon
            if (spirit >= 20) { // Cost to summon
                spiritCompanion = new OBJ_SpiritAnimal(gp);
                gp.obj.add(spiritCompanion);
                spirit -= 20;
                gp.ui.showMessage("Spirit Companion Summoned");
            } else {
                gp.ui.showMessage("Not enough Spirit (Need 20)");
            }
        } else {
            // Dismiss
            if (gp.obj.contains(spiritCompanion)) {
                gp.obj.remove(spiritCompanion);
            }
            spiritCompanion = null;
            gp.ui.showMessage("Spirit Companion Dismissed");
        }
    }

    // --- CRAFTING SYSTEM ---
    public boolean hasItem(String itemName, int amountNeeded) {
        int count = 0;
        for (InventorySlot slot : inventorySlots) {
            for (Item i : slot.items) {
                if (i.name.equals(itemName)) {
                    count += i.amount;
                }
            }
        }
        return count >= amountNeeded;
    }

    public void consumeItem(String itemName, int amountNeeded) {
        int remaining = amountNeeded;
        for (int s = 0; s < inventorySlots.size(); s++) {
            InventorySlot slot = inventorySlots.get(s);
            for (int i = 0; i < slot.items.size(); i++) {
                Item item = slot.items.get(i);
                if (item.name.equals(itemName)) {
                    if (item.amount > remaining) {
                        item.amount -= remaining;
                        remaining = 0;
                    } else {
                        remaining -= item.amount;
                        slot.remove(item);
                        i--;
                    }
                    if (remaining <= 0) break;
                }
            }
            if (slot.items.isEmpty()) {
                inventorySlots.remove(s);
                s--;
            }
            if (remaining <= 0) break;
        }
    }

    public void craftItem(String recipeName) {
        if (recipeName.equals("Arrow")) {
            // Recipe: 1 Branch + 1 Flint = 5 Arrows
            if (hasItem("Branch", 1) && hasItem("Flint", 1)) {
                consumeItem("Branch", 1);
                consumeItem("Flint", 1);
                Item arrows = createItem("Arrow");
                arrows.amount = 5;
                pickUpItem(arrows);
                System.out.println("Crafted 5 Arrows!");
            }
        } else if (recipeName.equals("Pickaxe")) {
            // Recipe: 3 Stone + 2 Branch
            if (hasItem("Stone", 3) && hasItem("Branch", 2)) {
                consumeItem("Stone", 3);
                consumeItem("Branch", 2);
                pickUpItem(createItem("Pickaxe"));
                System.out.println("Crafted Pickaxe!");
            }
        } else if (recipeName.equals("Fire Pit")) {
            // Recipe: 2 Wood + 3 Stone
            if (hasItem("Wood", 2) && hasItem("Stone", 3)) {
                consumeItem("Wood", 2);
                consumeItem("Stone", 3);
                pickUpItem(createItem("Fire Pit"));
                System.out.println("Crafted Fire Pit!");
                gp.ui.showMessage("Crafted Fire Pit!");
            } else {
                gp.ui.showMessage("Not enough resources!");
            }
        }
    }

    public void setPath(int x, int y) {
        int startCol = (int)(worldX + gp.tileSize/2) / gp.tileSize;
        int startRow = (int)(worldY + gp.tileSize/2) / gp.tileSize;
        int goalCol = x / gp.tileSize;
        int goalRow = y / gp.tileSize;

        // Clamp values to map boundaries to prevent crashes and allow re-entry
        startCol = Math.max(0, Math.min(gp.maxWorldCol - 1, startCol));
        startRow = Math.max(0, Math.min(gp.maxWorldRow - 1, startRow));
        goalCol = Math.max(0, Math.min(gp.maxWorldCol - 1, goalCol));
        goalRow = Math.max(0, Math.min(gp.maxWorldRow - 1, goalRow));

        gp.pFinder.setNodes(startCol, startRow, goalCol, goalRow);
        if(gp.pFinder.search()) {
            this.onPath = true;
        }
    }

    // Helper to find item
    public Item findItemInInventory(String name) {
        for (InventorySlot slot : inventorySlots) {
            for (Item i : slot.items) {
                if (i.name.equals(name)) return i;
            }
        }
        return null;
    }

    // Helper to find item by substring
    public Item findItemWithSubstring(String sub) {
        for (InventorySlot slot : inventorySlots) {
            for (Item i : slot.items) {
                if (i.name.contains(sub)) return i;
            }
        }
        return null;
    }

    public void update() {
        
        // CHECK GAME OVER
        if (life <= 0) {
            gp.gameState = gp.gameOverState;
            gp.ui.commandNum = 0; // Reset selection
            return;
        }

        // ENERGY SYSTEM
        energyCounter++;
        // Active Drain: Moving drains energy faster
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            energyCounter++; // Double the drain rate when moving
        }
        
        // Wet Drain (Cold/Heavy clothes)
        if (wet) {
            energyCounter++; 
        }

        if (energyCounter > 600) { // Threshold for losing 1 Energy point
            energy--;
            energyCounter = 0;
            if (energy < 0) energy = 0;
        }
        
        // BUFF TIMERS
        if (speedBuffTimer > 0) speedBuffTimer--;
        if (strengthBuffTimer > 0) strengthBuffTimer--;
        
        // ATTACK TIMER
        if (attacking) {
            attackTimer++;
            if (attackTimer > 10) {
                attacking = false;
                attackTimer = 0;
            }
        }
        
        // Exhaustion Penalty (Drain Health)
        if (energy == 0 && energyCounter % 120 == 0) { // Every 2 seconds take damage
             life--;
             System.out.println("Exhausted! Lost 1 HP.");
        }

        // 0. UPDATE FOG
        gp.tileM.updateFog(worldX, worldY);

        // EXIT INTERIOR CHECK
        // If on Map 1 (Interior) and walk near the door (25, 29)
        if (gp.currentMap == 1) {
            double dist = Math.sqrt(Math.pow(worldX - (25*gp.tileSize), 2) + Math.pow(worldY - (29*gp.tileSize), 2));
            if (dist < gp.tileSize) {
                gp.returnToWorld();
            }
        }

        // 1. TERRAIN SENSING
        int centerWorldX = (int)worldX + (gp.tileSize / 2);
        int centerWorldY = (int)worldY + (gp.tileSize / 2);

        int col = centerWorldX / gp.tileSize;
        int row = centerWorldY / gp.tileSize;

        boolean inWater = false;
        // Check if player is on a "Coastline" tile (ID 1120 to 1183)
        if (col >= 0 && col < gp.maxWorldCol && row >= 0 && row < gp.maxWorldRow) {
            
            // HEIGHT LOGIC: Snap z when changing elevation
            int tileHeight = gp.tileM.tileHeightMap[gp.currentMap][col][row];
            if (tileHeight != currentMapHeight) {
                z -= (tileHeight - currentMapHeight) * 40; // 40 is the height scale
                currentMapHeight = tileHeight;
            }

            int tileNum = gp.tileM.mapTileNum[gp.currentMap][col][row];

            if (tileNum >= 1120 && tileNum <= 1183) {
                speed = 3; // Slow speed for sand
            } else if (tileNum >= 800 && tileNum <= 1119) {
                speed = 1; // Very slow for water (Swimming)
                inWater = true;
            } else {
                speed = 4; // Normal speed
            }
            
            // Energy Penalty (Diminish Stats)
            if (energy == 0) {
                speed = 1; // Very slow when exhausted
            } else if (energy < 5) {
                speed = 2; // Slow when tired
            }
        }

        // WET STATUS UPDATE
        if (inWater) {
            wet = true;
            wetCounter = 600; // 10 Seconds duration (60fps * 10)
        } else if (wetCounter > 0) {
            // Check for Fire Pit (Drying)
            boolean nearFire = false;
            for (Entity e : gp.obj) {
                if (e != null && e.name.equals("Fire Pit")) {
                    double dist = Math.sqrt(Math.pow(worldX - e.worldX, 2) + Math.pow(worldY - e.worldY, 2));
                    if (dist < gp.tileSize * 2) {
                        nearFire = true;
                        break;
                    }
                }
            }
            
            if (nearFire) {
                wetCounter = 0;
                wet = false;
                gp.ui.showMessage("Dried by the fire!");
            } else {
                wetCounter--;
                wet = true;
            }
        } else {
            wet = false;
        }
        
        // 2. JUMPING LOGIC
        if (keyH.spacePressed && !jumping && z == 0) {
            jumping = true;
            verticalVelocity = 8; // Jump strength
        }
        
        if (jumping || z > 0) {
            z += verticalVelocity;
            verticalVelocity -= 0.5; // Gravity
            
            if (z <= 0) {
                z = 0;
                jumping = false;
                verticalVelocity = 0;
            }
        }

        // 3. MOVEMENT & DIRECTION
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            onPath = false; // Manual input cancels auto-walk
            
            if (keyH.upPressed && keyH.leftPressed) direction = "up-left";
            else if (keyH.upPressed && keyH.rightPressed) direction = "up-right";
            else if (keyH.downPressed && keyH.leftPressed) direction = "down-left";
            else if (keyH.downPressed && keyH.rightPressed) direction = "down-right";
            else if (keyH.upPressed) direction = "up";
            else if (keyH.downPressed) direction = "down";
            else if (keyH.leftPressed) direction = "left";
            else if (keyH.rightPressed) direction = "right";

            // Collision Check
            collisionOn = false;
            gp.cChecker.checkTile(this);
            int objIndex = gp.cChecker.checkObject(this, true);
            
            // PICKUP LOGIC
            if (objIndex != 999) {
                Entity obj = gp.obj.get(objIndex);
                if (obj.type == TYPE_PICKUP) {
                    if (obj instanceof OBJ_DroppedItem && pickUpItem(((OBJ_DroppedItem)obj).item)) {
                        System.out.println("Picked up " + obj.name);
                        gp.playSE(5); // Pickup sound
                        gp.obj.remove(objIndex);
                    }
                }
            }

            if (!collisionOn) {
                double moveSpeed = speed;
                if (speedBuffTimer > 0) moveSpeed += 2; // Speed Buff
                // Diagonal normalization
                if ((keyH.upPressed || keyH.downPressed) && (keyH.leftPressed || keyH.rightPressed)) {
                    moveSpeed = speed * 0.707;
                }

                if (keyH.upPressed) worldY -= moveSpeed;
                if (keyH.downPressed) worldY += moveSpeed;
                if (keyH.leftPressed) worldX -= moveSpeed;
                if (keyH.rightPressed) worldX += moveSpeed;
            }
            
            // SPRITE ANIMATION
            spriteCounter++;
            if(spriteCounter > 12) {
                if(spriteNum == 1) spriteNum = 2;
                else if(spriteNum == 2) spriteNum = 1;
                spriteCounter = 0;
            }
        } else if (onPath) {
            // AUTO-WALK LOGIC
            if(!gp.pFinder.pathList.isEmpty()) {
                int nextCol = gp.pFinder.pathList.get(0).col;
                int nextRow = gp.pFinder.pathList.get(0).row;
                int nextX = nextCol * gp.tileSize;
                int nextY = nextRow * gp.tileSize;

                // Calculate deltas
                int dx = nextX - worldX;
                int dy = nextY - worldY;

                // Move X (Snap if close)
                if (Math.abs(dx) < speed) worldX = nextX;
                else worldX += (dx > 0) ? speed : -speed;

                // Move Y (Snap if close)
                if (Math.abs(dy) < speed) worldY = nextY;
                else worldY += (dy > 0) ? speed : -speed;

                // Update Direction based on largest movement
                if (Math.abs(dx) > Math.abs(dy)) direction = (dx > 0) ? "right" : "left";
                else if (Math.abs(dy) > 0) direction = (dy > 0) ? "down" : "up";
                // Note: If dx and dy are both 0, we keep previous direction

                // Check if reached the node (Exact match now possible due to snap)
                if (worldX == nextX && worldY == nextY) {
                     gp.pFinder.pathList.remove(0);
                }
            } else {
                onPath = false;
            }
            
            // SPRITE ANIMATION (Auto-walk)
            spriteCounter++;
            if(spriteCounter > 12) {
                if(spriteNum == 1) spriteNum = 2;
                else if(spriteNum == 2) spriteNum = 1;
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = null;
        
        switch(direction) {
            case "up":
            case "up-left":
            case "up-right":
                if(spriteNum == 1) image = up1;
                if(spriteNum == 2) image = up2;
                break;
            case "down":
            case "down-left":
            case "down-right":
                if(spriteNum == 1) image = down1;
                if(spriteNum == 2) image = down2;
                break;
            case "left":
                if(spriteNum == 1) image = left1;
                if(spriteNum == 2) image = left2;
                break;
            case "right":
                if(spriteNum == 1) image = right1;
                if(spriteNum == 2) image = right2;
                break;
        }
        
        // DIMENSIONS & CENTERING
        int zoomedSize = (int)(gp.tileSize * gp.scale * ageScale);
        int x = gp.getWidth() / 2 - (zoomedSize / 2);
        int y = gp.getHeight() / 2 - (zoomedSize / 2);

        // SHADOW
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(x + 5, y + zoomedSize - 10, zoomedSize - 10, 10);
        
        // JUMP OFFSET
        y -= z;

        // DRAW SPRITE
        g2.drawImage(image, x, y, zoomedSize, zoomedSize, null);
        
        // DRAW WEAPON (Animation)
        if (attacking && currentWeapon != null) {
            Graphics2D g2d = (Graphics2D)g2.create();
            
            int centerX = x + zoomedSize / 2;
            int centerY = y + zoomedSize / 2;
            
            // Base rotation based on direction
            double rotation = 0;
            switch(direction) {
                case "up": rotation = Math.toRadians(-45); break;
                case "down": rotation = Math.toRadians(135); break;
                case "left": rotation = Math.toRadians(225); break;
                case "right": rotation = Math.toRadians(45); break;
                case "up-left": rotation = Math.toRadians(270); break;
                case "up-right": rotation = Math.toRadians(0); break;
                case "down-left": rotation = Math.toRadians(180); break;
                case "down-right": rotation = Math.toRadians(90); break;
            }
            
            // Swing logic (Chop motion)
            double progress = (double)attackTimer / 10.0;
            double swing = Math.toRadians(-45 + (90 * progress));
            
            g2d.translate(centerX, centerY);
            g2d.rotate(rotation + swing);
            currentWeapon.draw(g2d, -zoomedSize/2, -zoomedSize/2 - (zoomedSize/4), zoomedSize);
            g2d.dispose();
        }

        // DEBUG: Draw Path
        if (onPath && !gp.pFinder.pathList.isEmpty()) {
            g2.setColor(new Color(255, 0, 0, 70));
            for (int i = 0; i < gp.pFinder.pathList.size(); i++) {
                Node node = gp.pFinder.pathList.get(i);
                int worldX = node.col * gp.tileSize;
                int worldY = node.row * gp.tileSize;
                int screenX = (int)((worldX - gp.player.worldX) * gp.scale + gp.player.screenX);
                int screenY = (int)((worldY - gp.player.worldY) * gp.scale + gp.player.screenY);
                g2.fillRect(screenX, screenY, (int)(gp.tileSize * gp.scale), (int)(gp.tileSize * gp.scale));
            }
        }
    }
}