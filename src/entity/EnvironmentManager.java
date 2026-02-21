package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import main.GamePanel;

public class EnvironmentManager {
    GamePanel gp;
    public BufferedImage darknessFilter;
    public int dayCounter;
    public float filterAlpha = 0f;
    
    // Day/Night Cycle
    public final int dayLength = 36000; // 10 minutes at 60FPS
    
    public EnvironmentManager(GamePanel gp) {
        this.gp = gp;
    }
    
    public void setup() {
        darknessFilter = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_ARGB);
        dayCounter = 3600; // Start at morning
    }
    
    public void update() {
        dayCounter++;
        if(dayCounter > dayLength) {
            dayCounter = 0;
        }
        
        // Cycle: Dawn -> Day -> Dusk -> Night
        float maxDarkness = 0.80f;
        
        if(dayCounter < 3600) { // Dawn (0 - 3600)
            float progress = (float)dayCounter / 3600f;
            filterAlpha = maxDarkness - (maxDarkness * progress);
        }
        else if(dayCounter < 20000) { // Day (3600 - 20000)
            filterAlpha = 0f;
        }
        else if(dayCounter < 24000) { // Dusk (20000 - 24000)
            float progress = (float)(dayCounter - 20000) / 4000f;
            filterAlpha = maxDarkness * progress;
        }
        else { // Night (24000 - 36000)
            filterAlpha = maxDarkness;
        }
    }
    
    public void draw(Graphics2D g2) {
        if(filterAlpha <= 0.01f) return; // Don't draw if it's bright day
        if(gp.currentMap != 0) return; // No darkness indoors
        
        int currentWidth = gp.getWidth();
        int currentHeight = gp.getHeight();

        // Resize filter if window size changed
        if (darknessFilter.getWidth() != currentWidth || darknessFilter.getHeight() != currentHeight) {
            darknessFilter = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
        }

        // 1. Create a black rectangle
        Graphics2D g2d = (Graphics2D)darknessFilter.getGraphics();
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.CLEAR, 0.0f));
        g2d.fillRect(0, 0, currentWidth, currentHeight);
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER));
        
        g2d.setColor(new Color(0, 0, 0, (int)(255 * filterAlpha)));
        g2d.fillRect(0, 0, currentWidth, currentHeight);
        
        // 2. Punch holes for light sources
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.DST_OUT));
        
        for(Entity e : gp.obj) {
            if(e != null && e.lightRadius > 0 && e.map == gp.currentMap) {
                double screenOffsetX = gp.getWidth() / 2 - (gp.tileSize * gp.scale) / 2;
                double screenOffsetY = gp.getHeight() / 2 - (gp.tileSize * gp.scale) / 2;
                int screenX = (int)((e.worldX - gp.player.worldX) * gp.scale + screenOffsetX);
                int screenY = (int)((e.worldY - gp.player.worldY) * gp.scale + screenOffsetY);
                
                // Adjust for center of entity
                screenX += (gp.tileSize * gp.scale) / 2;
                screenY += (gp.tileSize * gp.scale) / 2;
                
                drawLightCircle(g2d, screenX, screenY, e.lightRadius);
            }
        }
        
        g2d.dispose();
        
        // 3. Draw the filter
        g2.drawImage(darknessFilter, 0, 0, null);
    }
    
    public void drawLightCircle(Graphics2D g2, int x, int y, int radius) {
        int r = (int)(radius * gp.scale);
        if (r <= 0) return;
        
        // Center alpha 1.0 (removes darkness), Edge alpha 0.0 (keeps darkness)
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 255), 
            new Color(0, 0, 0, 200),
            new Color(0, 0, 0, 0)
        };
        
        RadialGradientPaint grad = new RadialGradientPaint(x, y, r, dist, colors);
        g2.setPaint(grad);
        g2.fillOval(x - r, y - r, r * 2, r * 2);
    }

    public void toggleDayNight() {
        if (dayCounter < 20000) {
            dayCounter = 28000; // Switch to Night
        } else {
            dayCounter = 10000; // Switch to Day
        }
    }

    public void setMorning() {
        dayCounter = 3600;
    }
}