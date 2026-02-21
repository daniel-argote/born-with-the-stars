package main;

import java.io.File;

public class SaveLoadTest {

    public static void main(String[] args) {
        System.out.println("[TEST] Starting Save/Load Unit Test...");

        // 1. Initialize GamePanel (headless-ish)
        GamePanel gp = new GamePanel();
        
        // 2. Define a temporary test file
        File testFile = new File("test_save_data.dat");
        
        // 3. Set specific state to verify
        System.out.println("[TEST] Setting up player state...");
        gp.player.worldX = 1234;
        gp.player.worldY = 5678;
        gp.player.life = 1;
        gp.player.maxLife = 5;
        gp.player.direction = "left";
        gp.player.inventorySlots.clear();
        gp.player.pickUpItem(gp.player.createItem("Sword"));
        gp.player.pickUpItem(gp.player.createItem("Potion"));

        // 4. Save to the test file
        System.out.println("[TEST] Saving game...");
        gp.saveGame(testFile);

        // 5. Reset/Corrupt the current state
        System.out.println("[TEST] Resetting player state...");
        gp.player.worldX = 0;
        gp.player.worldY = 0;
        gp.player.life = 6;
        gp.player.direction = "down";
        gp.player.inventorySlots.clear();

        // 6. Load from the test file
        System.out.println("[TEST] Loading game...");
        gp.loadGame(testFile);

        // 7. Assertions
        boolean passed = true;
        
        if (gp.player.worldX != 1234) {
            System.err.println("FAIL: worldX mismatch. Expected 1234, got " + gp.player.worldX);
            passed = false;
        }
        if (gp.player.worldY != 5678) {
            System.err.println("FAIL: worldY mismatch. Expected 5678, got " + gp.player.worldY);
            passed = false;
        }
        if (gp.player.life != 1) {
            System.err.println("FAIL: life mismatch. Expected 1, got " + gp.player.life);
            passed = false;
        }
        if (!gp.player.direction.equals("left")) {
            System.err.println("FAIL: direction mismatch. Expected 'left', got '" + gp.player.direction + "'");
            passed = false;
        }
        if (gp.player.inventorySlots.size() != 2) {
            System.err.println("FAIL: Inventory size mismatch. Expected 2, got " + gp.player.inventorySlots.size());
            passed = false;
        } else {
            if (!gp.player.inventorySlots.get(0).items.get(0).name.equals("Sword")) {
                System.err.println("FAIL: Item 1 mismatch.");
                passed = false;
            }
        }

        // 8. Cleanup
        if (testFile.exists()) {
            testFile.delete();
        }

        if (passed) {
            System.out.println("[TEST] SUCCESS: Save/Load logic is working correctly.");
        } else {
            System.out.println("[TEST] FAILED: See errors above.");
        }
        
        System.exit(0);
    }
}