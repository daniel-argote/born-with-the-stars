package ui;

import main.GamePanel;
import java.awt.Rectangle;

public class UITest {
    public static void main(String[] args) {
        System.out.println("[TEST] Checking UI Element Overlap...");
        
        GamePanel gp = new GamePanel();
        UI ui = new UI(gp);
        
        Rectangle equipped = ui.getEquippedItemBounds();
        Rectangle quest = ui.getActiveQuestBounds();
        
        if (equipped.intersects(quest)) {
            System.err.println("FAIL: Equipped Item area overlaps with Active Quest area!");
        } else {
            System.out.println("PASS: UI Elements do not overlap.");
        }
    }
}