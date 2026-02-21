package entity;

public class Quest {
    public String name;
    public String description;
    public boolean completed = false;
    
    // Quest Types
    public static final int TYPE_COLLECT = 0;
    public static final int TYPE_KILL = 1;
    public int type;
    
    public String targetName; // e.g., "Wood" or "Rabbit"
    public int targetAmount;
    public int currentAmount;
    
    public Item rewardItem;

    public Quest(String name, String desc, int type, String target, int amount) {
        this.name = name;
        this.description = desc;
        this.type = type;
        this.targetName = target;
        this.targetAmount = amount;
        this.currentAmount = 0;
    }
    
    public boolean check() {
        return currentAmount >= targetAmount;
    }
}