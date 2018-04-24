package de.gutenko.roguelike.entities;

import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.scenes.DungeonScene;

import static android.os.SystemClock.uptimeMillis;
import static de.gutenko.roguelike.entities.Entity.TurnAction.ATTACK;
import static de.gutenko.roguelike.entities.Entity.TurnAction.MOVE;

/**
 * Created by Peter on 3/10/18.
 */

public class Enemy extends Entity {

    public enum EnemyType {
        SLIME,RAT;
    }

    public Enemy(int x, int y, EnemyType type) {
        renderX = tileX = x;
        renderY = tileY = y;

        switch (type) {
            case SLIME:
                name = "slime";
                deathStr = "The slime melts away.";
                spriteName = Const.TEX_SLIME;
                maxHealth = 23;
                health = maxHealth;
                atk = 9;
                def = spd = mag = 9;
                break;
            case RAT:
                name = "rat";
                deathStr = "The rat is slain.";
                spriteName = Const.TEX_RAT;
                maxHealth = 18;
                health = maxHealth;
                atk = spd = 14;
                def = mag = 4;
                break;
        }

        spriteX = 4;
        spriteY = 2;
        spriteInd = 1;

    }

    @Override
    public TurnAction act() {
        PlayerEntity p = DungeonScene.getInstance().getPlayer();
        int distX = Math.abs(p.tileX-tileX);
        int distY = Math.abs(p.tileY-tileY);

        if (distX+distY <= 1 || (distX==1&&distY==1)) {
            // attack the player
            targetEntity = p;
            return ATTACK;
        } else {
            // move closer to the player
            moveValue = pathfindTo(p.tileX, p.tileY);
            return MOVE;
        }
    }

    @Override
    public void renderStep() {
        long time = uptimeMillis();
        double seconds = time/1000d;
        int ind = (int)(seconds*1.25)%2;
        spriteInd = 1+2*ind;
    }

    @Override
    public String getAttackString(Entity target) {
        return "The "+name+" attacks!";
    }
}
