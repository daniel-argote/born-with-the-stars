package entity;

import java.util.ArrayList;

public class InventorySlot {
    public ArrayList<Item> items = new ArrayList<>();
    public int selectedIndex = 0;
    public int category;

    public InventorySlot(int category) {
        this.category = category;
    }

    public Item getSelectedItem() {
        if (items.isEmpty()) return null;
        if (selectedIndex >= items.size()) selectedIndex = 0;
        return items.get(selectedIndex);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void remove(Item item) {
        items.remove(item);
        if (selectedIndex >= items.size()) selectedIndex = 0;
    }
}