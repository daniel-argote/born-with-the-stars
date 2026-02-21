package entity;

import java.util.ArrayList;

import main.GamePanel;

public class QuestManager {
    GamePanel gp;
    public ArrayList<Quest> questList = new ArrayList<>();
    
    public QuestManager(GamePanel gp) {
        this.gp = gp;
    }
    
    public void addQuest(Quest q) {
        questList.add(q);
        gp.ui.showMessage("New Quest: " + q.name);
    }
    
    public void updateKill(String target) {
        for(Quest q : questList) {
            if(!q.completed && q.type == Quest.TYPE_KILL && q.targetName.equals(target)) {
                q.currentAmount++;
                if(q.check()) {
                    completeQuest(q);
                }
            }
        }
    }
    
    public void updateCollection(String itemName) {
        for(Quest q : questList) {
            if(!q.completed && q.type == Quest.TYPE_COLLECT && q.targetName.equals(itemName)) {
                q.currentAmount++;
                if(q.check()) {
                    completeQuest(q);
                }
            }
        }
    }
    
    public void completeQuest(Quest q) {
        q.completed = true;
        gp.ui.showMessage("Quest Completed: " + q.name + "!");
        if(q.rewardItem != null) {
            gp.player.pickUpItem(q.rewardItem);
            gp.ui.showMessage("Reward: " + q.rewardItem.name);
        }
    }
}