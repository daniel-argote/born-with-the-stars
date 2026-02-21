package entity;

import main.GamePanel;

public class SkillManager {
    
    GamePanel gp;
    
    // SKILL FLAGS
    public boolean magnetUnlocked = false;
    public boolean nightVisionUnlocked = false; // Placeholder for future
    public boolean speedDemonUnlocked = false;  // Placeholder for future

    public SkillManager(GamePanel gp) {
        this.gp = gp;
    }

    public boolean unlock(String skillName) {
        int cost = 50; // Flat cost for now
        
        if (gp.player.spirit >= cost) {
            if (skillName.equals("Magnet") && !magnetUnlocked) {
                magnetUnlocked = true;
                gp.player.spirit -= cost;
                gp.ui.showMessage("Unlocked: Magnet!");
                return true;
            }
            // Add other skills here
        }
        return false;
    }
}
