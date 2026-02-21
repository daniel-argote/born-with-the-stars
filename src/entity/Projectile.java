package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import main.GamePanel;

public class Projectile extends Entity {

    GamePanel gp;
    Entity user;
    int damage;

    public Projectile(GamePanel gp, Entity user, int damage) {
        this.gp = gp;
        this.user = user;
        this.damage = damage;
        
        this.worldX = user.worldX;
        this.worldY = user.worldY;
        this.direction = user.direction;
        this.speed = 10; // Fast speed for arrow
        this.maxLife = 60; // Disappear after 60 frames (approx 1 sec)
        this.life = maxLife;
        
        // Small hitbox
        this.solidArea = new Rectangle(16, 16, 32, 32);
        this.solidAreaDefaultX = solidArea.x;
        this.solidAreaDefaultY = solidArea.y;
    }

    @Override
    public void update() {
        
        life--;
        if (life <= 0) {
            gp.projectiles.remove(this);
            return;
        }

        // Collision with Walls
        collisionOn = false;
        gp.cChecker.checkTile(this);
        if (collisionOn) {
            System.out.println("SE: Thwack!"); // Sound placeholder
            gp.playSE(3); // Thwack sound
            generateParticle(this, null);
            checkDrop();
            gp.projectiles.remove(this); // Destroy on wall hit
            return;
        }

        // Collision with Objects/Animals
        int objIndex = gp.cChecker.checkObject(this, true);
        if (objIndex != 999) {
            Entity target = gp.obj.get(objIndex);
            if (target.type == TYPE_NPC || target.type == TYPE_OBSTACLE) {
                // Prevent arrows from damaging rocks/veins
                if (target.name.equals("Rock") || target.name.equals("Flint Vein")) {
                     System.out.println("SE: Tink!"); // Sound placeholder for hitting rock
                     gp.playSE(4); // Tink sound
                     gp.projectiles.remove(this);
                     return;
                }

                if (!target.invincible) {
                    // Prevent arrows from damaging friendly (talkable) NPCs
                    if (target.type == TYPE_NPC && target.talkable) {
                        gp.projectiles.remove(this); // Arrow stops
                        return;
                    }

                    target.life -= damage;
                    target.invincible = true;
                    
                    if (user instanceof Player) {
                        ((Player)user).shotsHit++;
                    }
                    System.out.println("Arrow hit " + target.name + " for " + damage + " damage!");
                    System.out.println("SE: Hit!"); // Sound placeholder
                    gp.playSE(2); // Hit sound
                    generateParticle(this, target);
                    
                    if (target.life <= 0) {
                        gp.obj.remove(objIndex);
                        if (user instanceof Player) {
                            ((Player)user).handleDrop(target);
                        }
                    }
                }
                checkDrop();
                gp.projectiles.remove(this); // Destroy arrow on hit
                return;
            }
        }

        // Movement
        switch(direction) {
            case "up": worldY -= speed; break;
            case "down": worldY += speed; break;
            case "left": worldX -= speed; break;
            case "right": worldX += speed; break;
            case "up-left": worldY -= speed; worldX -= speed; break;
            case "up-right": worldY -= speed; worldX += speed; break;
            case "down-left": worldY += speed; worldX -= speed; break;
            case "down-right": worldY += speed; worldX += speed; break;
        }
    }

    public void generateParticle(Entity generator, Entity target) {
        Color color = new Color(139, 69, 19); // Brown (Wood)
        int size = 6;
        int speed = 1;
        int maxLife = 20;
        
        for(int i=0; i<4; i++) {
            int xd = (int)(Math.random()*3) - 1;
            int yd = (int)(Math.random()*3) - 1;
            gp.particleList.add(new Particle(gp, generator, color, size, speed, maxLife, xd, yd));
        }
    }

    private void checkDrop() {
        // 40% chance to recover arrow
        if (user instanceof Player && Math.random() < 0.4) {
            Item arrow = ((Player)user).createItem("Arrow");
            arrow.amount = 1;
            ((Player)user).dropItem(arrow, worldX, worldY);
        }
    }

    // Draw logic is handled by Entity.draw() using the default sprite or we can override for a simple shape
    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        double screenOffsetX = gp.getWidth() / 2 - (gp.tileSize * gp.scale) / 2;
        double screenOffsetY = gp.getHeight() / 2 - (gp.tileSize * gp.scale) / 2;
        int screenX = (int)((worldX - gp.player.worldX) * gp.scale + screenOffsetX);
        int screenY = (int)((worldY - gp.player.worldY) * gp.scale + screenOffsetY);

        g2.setColor(Color.WHITE);
        g2.fillOval(screenX + 24, screenY + 24, 16, 16); // Simple dot for now
    }
}