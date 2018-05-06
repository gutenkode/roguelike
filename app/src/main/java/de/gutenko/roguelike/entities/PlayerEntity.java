package de.gutenko.roguelike.entities;

import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.data.Input;
import de.gutenko.roguelike.habittracker.data.player.Player;
import de.gutenko.roguelike.loop.GameLoop;
import de.gutenko.roguelike.scenes.DungeonScene;

import static android.os.SystemClock.uptimeMillis;
import static de.gutenko.roguelike.entities.Entity.TurnAction.ATTACK;
import static de.gutenko.roguelike.entities.Entity.TurnAction.MOVE;
import static de.gutenko.roguelike.entities.Entity.TurnAction.NONE;

/**
 * Created by Peter on 3/10/18.
 */

public class PlayerEntity extends Entity {

    private static PlayerEntity instance;
    public static PlayerEntity moveInstanceTo(int x, int y) {
        if (instance == null)
            instance = new PlayerEntity();
        instance.moveTo(x,y);
        return instance;
    }
    public static PlayerEntity getInstance() {
        if (instance == null) {
            instance = new PlayerEntity();
            instance.moveTo(1, 1);
        }
        return instance;
    }

    private int targetX, targetY;
    private PlayerEntity() {
        name = "Player";
        deathStr = "You have died.";
        spriteName = Const.TEX_PLAYER;

        spriteX = 2;
        spriteY = 1;
        spriteInd = 0;

        maxHealth = 100;
        health = maxHealth;
        spd = mag = 10;
        atk = 15;
        def = 15;
    }
    private void moveTo(int x, int y) {
        renderX = targetX = tileX = x;
        renderY = targetY = tileY = y;
    }
    @Override
    public void teleportTo(int x, int y) {
        super.teleportTo(x,y);
        targetX = x;
        targetY = y;
    }

    @Override
    public TurnAction act() {
        if (health <= 0)
            return NONE;
        if (Input.wasTapEvent()) { // will "eat" tap events
            double normX = Input.touchX / GameLoop.getInstance().getWidth();
            double normY = Input.touchY / GameLoop.getInstance().getHeight() / DungeonScene.getInstance().getAspectRatio();
            int newX = (int) (normX * DungeonScene.getInstance().getMapScale());
            int newY = (int) (normY * DungeonScene.getInstance().getMapScale());
            if (DungeonScene.getInstance().isTileWalkable(newX, newY)) {
                targetX = newX;
                targetY = newY;
            }

            // attack adjacent enemies when tapped
            int distX = Math.abs(targetX-tileX);
            int distY = Math.abs(targetY-tileY);
            int tapDistance = Math.abs(targetX-tileX) + Math.abs(targetY-tileY); // manhattan distance from tapped tile to current tile
            // TODO expand to cover all interactions with tiles
            if (tapDistance == 1 || (distX==1&&distY==1)) {
                Enemy e = DungeonScene.getInstance().getEnemyAt(targetX,targetY);
                if (e != null) {
                    targetX = tileX; // don't move while attacking
                    targetY = tileY;
                    targetEntity = e;
                    return ATTACK;
                }
            }
        }

        // move to tapped square
        int distance = Math.abs(targetX-tileX) + Math.abs(targetY-tileY); // manhattan distance from target tile to current tile
        if (distance > 0) {
            moveValue = pathfindTo(targetX, targetY); // will be something like {1,0} or {0,0}
            return MOVE;
        } else
            return NONE;
    }

    @Override
    public void renderStep() {
        long time = uptimeMillis();
        double seconds = time/1000d;
        int ind = (int)(seconds*1.5)%2;
        spriteInd = ind;
    }

    @Override
    protected void damage(double dmg) {
        super.damage(dmg);
        // cancel any movement when attacked
        targetX = tileX;
        targetY = tileY;
    }

    @Override
    public String getAttackString(Entity target) {
        return "You attack the "+target.name+"!";
    }
}
