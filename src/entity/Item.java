package entity;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;

public class Item {

    public String name;
    public BufferedImage image;
    public String description = "";
    public int value = 0; // Heal amount, damage, etc.
    public int cost = 0; // Intangible spirit cost
    public int durability = 100;
    public int maxDurability = 100;
    public int amount = 1;
    public boolean stackable = false;
    
    // ITEM TYPES
    public int type;
    public static final int TYPE_CONSUMABLE = 0;
    public static final int TYPE_EQUIPABLE = 1;
    public static final int TYPE_MISC = 2;
    public static final int TYPE_AXE = 3;
    public static final int TYPE_SWORD = 4;
    public static final int TYPE_FOOD = 5;
    public static final int TYPE_BOW = 6;
    public static final int TYPE_PICKAXE = 7;
    public static final int TYPE_PLACABLE = 8;

    // INVENTORY CATEGORIES
    public int category;
    public static final int CATEGORY_WEAPON = 0;
    public static final int CATEGORY_TOOL = 1;
    public static final int CATEGORY_FOOD = 2;
    public static final int CATEGORY_RESOURCE = 3;
    public static final int CATEGORY_MISC = 4;
    public static final int CATEGORY_SWORD = 5;
    public static final int CATEGORY_RANGED = 6;
    public static final int CATEGORY_MEDICINE = 7;
    public static final int CATEGORY_FRUIT = 8;
    public static final int CATEGORY_SPECIAL = 9;

    public Item() {
    }

    public void use(Player player) {
        if (type == TYPE_CONSUMABLE) {
            player.life += value;
            if (player.life > player.maxLife) {
                player.life = player.maxLife;
            }
            System.out.println("Consumed " + name + ". Recovered " + value + " health.");
        }
        if (type == TYPE_FOOD) {
            player.energy += value;
            if (player.energy > player.maxEnergy) {
                player.energy = player.maxEnergy;
            }
            System.out.println("Ate " + name + ". Recovered " + value + " energy.");
        }
        System.out.println("Used item: " + name + " (Life: " + player.life + "/" + player.maxLife + ")");
    }

    public void draw(Graphics2D g2, int x, int y, int size) {
        // Save original transform
        java.awt.geom.AffineTransform originalTransform = g2.getTransform();
        g2.translate(x, y);

        switch (name) {
            case "Wood": drawWood(g2); break;
            case "Branch": drawBranch(g2); break;
            case "Berry": drawBerry(g2, size); break;
            case "Arrow": drawArrow(g2, size); break;
            case "Bow": drawBow(g2, size); break;
            case "Stone": drawStone(g2); break;
            case "Flint": drawFlint(g2); break;
            case "Pickaxe": drawPickaxe(g2, size); break;
            case "Bone": drawBone(g2); break;
            case "Tomahawk": drawTomahawk(g2, size); break;
            case "Fire Pit": drawFirePit(g2); break;
            case "Potion": drawPotion(g2); break;
            case "Sword": drawSword(g2); break;
            case "Key": drawKey(g2); break;
            default:
                if (name.contains("(Raw)")) drawRawMeat(g2);
                else if (name.contains("(Cooked)")) drawCookedMeat(g2);
                else if (name.contains("Fur")) drawFur(g2);
                else drawGeneric(g2);
                break;
        }
        
        // Restore transform
        g2.setTransform(originalTransform);
    }

    private void drawWood(Graphics2D g2) {
        g2.setColor(new Color(139, 69, 19)); // Saddle Brown
        g2.fillRect(16, 24, 32, 16); // Rectangular plank
        g2.setColor(new Color(100, 50, 0)); // Darker outline
        g2.drawRect(16, 24, 32, 16);
    }

    private void drawBranch(Graphics2D g2) {
        g2.setColor(new Color(101, 67, 33)); // Dark Brown
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(16, 48, 48, 16); // Main stick
        g2.drawLine(32, 32, 42, 22); // Small twig
    }

    private void drawBerry(Graphics2D g2, int size) {
        AffineTransform old = g2.getTransform();
        setupItemScale(g2, size, 1.1);
        Color berryColor = new Color(59, 47, 80); 
        drawSingleBerry(g2, -8, -4, berryColor);
        drawSingleBerry(g2, 8, -1, berryColor);
        drawSingleBerry(g2, 0, 6, berryColor);
        g2.setTransform(old);
    }

    private void drawArrow(Graphics2D g2, int size) {
        AffineTransform old = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(size / 2, size / 2);
        g2.rotate(Math.toRadians(-45));
        double scale = (double)size / 240.0;
        g2.scale(scale, scale);
        g2.translate(-100, 0);

        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(0, -3, 200, 6);

        g2.setColor(new Color(40, 40, 40));
        int[] fx = {0, -20, -35, -15};
        int[] fyTop = {-3, -20, -20, -3};
        int[] fyBottom = {3, 20, 20, 3};
        g2.fillPolygon(fx, fyTop, 4);
        g2.fillPolygon(fx, fyBottom, 4);

        int[] hx = {200, 220, 200};
        int[] hy = {-10, 0, 10};
        g2.setColor(new Color(105, 105, 105));
        g2.fillPolygon(hx, hy, 3);

        drawColoredWrap(g2, 40);
        drawColoredWrap(g2, 120);
        drawColoredWrap(g2, 185);
        g2.setTransform(old);
    }

    private void drawBow(Graphics2D g2, int size) {
        AffineTransform old = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(size / 2, size / 2);
        g2.rotate(Math.toRadians(45));
        double scale = (double)size / 220.0;
        g2.scale(scale, scale);
        
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, -100, 0, 100);

        g2.setColor(new Color(101, 67, 33));
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D upperLimb = new Path2D.Double();
        upperLimb.moveTo(0, 0);
        upperLimb.quadTo(-40, -50, 0, -100);
        g2.draw(upperLimb);
        Path2D lowerLimb = new Path2D.Double();
        lowerLimb.moveTo(0, 0);
        lowerLimb.quadTo(-40, 50, 0, 100);
        g2.draw(lowerLimb);

        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(-4, -15, 8, 30);
        g2.setTransform(old);
    }

    private void drawStone(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        g2.fillOval(20, 24, 24, 20);
    }

    private void drawFlint(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        int[] xPoints = {32, 20, 44};
        int[] yPoints = {16, 40, 40};
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawPickaxe(Graphics2D g2, int size) {
        AffineTransform old = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(size / 2, size / 2);
        g2.rotate(Math.toRadians(-45));
        double scale = (double)size / 250.0;
        g2.scale(scale, scale);
        g2.translate(0, -60);

        g2.setColor(new Color(160, 82, 45));
        g2.fillRect(-6, -40, 12, 200);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(-6, -40, 12, 200);

        Path2D head = new Path2D.Double();
        head.moveTo(-80, 5);
        head.quadTo(0, -20, 80, 5);
        head.lineTo(80, -5);
        head.quadTo(0, -40, -80, -5);
        head.closePath();

        g2.setColor(new Color(128, 128, 128));
        g2.fill(head);
        g2.setColor(new Color(200, 200, 200));
        g2.fillPolygon(new int[]{-80, -60, -80}, new int[]{5, -5, -5}, 3);
        g2.fillPolygon(new int[]{80, 60, 80}, new int[]{5, -5, -5}, 3);
        g2.setColor(Color.BLACK);
        g2.draw(head);
        g2.setTransform(old);
    }

    private void drawRawMeat(Graphics2D g2) {
        g2.setColor(new Color(178, 34, 34));
        g2.fillOval(20, 24, 24, 16);
        g2.setColor(new Color(250, 128, 114));
        g2.fillOval(24, 26, 10, 6);
    }

    private void drawCookedMeat(Graphics2D g2) {
        g2.setColor(new Color(139, 69, 19));
        g2.fillOval(20, 24, 24, 16);
        g2.setColor(new Color(160, 82, 45));
        g2.fillOval(24, 26, 10, 6);
    }

    private void drawFur(Graphics2D g2) {
        g2.setColor(new Color(210, 180, 140));
        g2.fillOval(18, 18, 28, 28);
    }

    private void drawBone(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(20, 20, 44, 44);
        g2.fillOval(16, 16, 8, 8);
        g2.fillOval(40, 40, 8, 8);
    }

    private void drawTomahawk(Graphics2D g2, int size) {
        AffineTransform old = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(size / 2, size / 2);
        g2.rotate(Math.toRadians(-45));
        double scale = (double)size / 300.0; 
        g2.scale(scale, scale);
        g2.translate(0, -60);
        g2.setStroke(new BasicStroke(4)); 

        Path2D stone = new Path2D.Double();
        stone.moveTo(-60, -20);
        stone.lineTo(40, -35);
        stone.lineTo(50, 10);
        stone.lineTo(-50, 25);
        stone.closePath();
        g2.setColor(new Color(142, 142, 142));
        g2.fill(stone);
        g2.setColor(Color.BLACK);
        g2.draw(stone);

        g2.setColor(new Color(160, 82, 45));
        g2.fillRect(-10, -50, 20, 250);
        g2.setColor(Color.BLACK);
        g2.drawRect(-10, -50, 20, 250);

        g2.setColor(new Color(222, 184, 135));
        g2.fillRect(-11, -10, 22, 40);
        g2.fillRect(-11, 170, 22, 30);
        
        drawFeather(g2, -15, 30);
        drawFeather(g2, -5, 35);
        g2.setTransform(old);
    }

    private void drawFirePit(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        g2.drawOval(16, 16, 32, 32);
        g2.setColor(Color.ORANGE);
        g2.fillOval(24, 24, 16, 16);
    }

    private void drawPotion(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillOval(20, 24, 24, 24);
        g2.setColor(Color.WHITE);
        g2.fillRect(28, 16, 8, 8);
    }

    private void drawSword(Graphics2D g2) {
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(48, 16, 16, 48);
        g2.setColor(new Color(139, 69, 19));
        g2.drawLine(20, 44, 12, 52);
        g2.drawLine(16, 40, 24, 48);
    }

    private void drawKey(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(20, 44, 44, 20);
        g2.drawOval(16, 40, 10, 10);
        g2.drawLine(40, 24, 44, 28);
        g2.drawLine(36, 28, 40, 32);
    }

    private void drawGeneric(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(20, 20, 24, 24);
    }

    private void drawSingleBerry(Graphics2D g2d, int x, int y, Color color) {
        int size = 18;
        
        // Main berry body
        g2d.setColor(color);
        g2d.fillOval(x - size/2, y - size/2, size, size);
        
        // Darker outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(x - size/2, y - size/2, size, size);
        
        // 3. The Shine (Glint)
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillOval(x - 4, y - 5, 4, 4);
    }

    private void drawFeather(Graphics2D g2d, int x, int y) {
        // Top half (White)
        int[] xPoints = {x, x - 10, x + 10};
        int[] yPoints = {y, y + 40, y + 40};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Bottom half (Black)
        int[] xPoints2 = {x - 10, x + 10, x};
        int[] yPoints2 = {y + 40, y + 40, y + 70};
        g2d.setColor(Color.BLACK);
        g2d.fillPolygon(xPoints2, yPoints2, 3);
    }

    private void drawColoredWrap(Graphics2D g2d, int xPos) {
        // Turquoise base
        g2d.setColor(new Color(64, 224, 208)); 
        g2d.fillRect(xPos, -4, 15, 8);
        
        // Tiny red and white stripes
        g2d.setColor(Color.RED);
        g2d.fillRect(xPos + 5, -4, 2, 8);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xPos + 8, -4, 2, 8);
    }

    private void setupItemScale(Graphics2D g2, int size, double scaleFactor) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(size / 2, size / 2);
        double s = (double)size / 64.0 * scaleFactor;
        g2.scale(s, s);
    }
}