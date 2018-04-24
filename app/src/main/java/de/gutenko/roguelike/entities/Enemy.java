package de.gutenko.roguelike.entities;

import android.opengl.Matrix;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.scenes.DungeonScene;

import static android.os.SystemClock.uptimeMillis;
import static de.gutenko.roguelike.entities.Entity.TurnAction.ATTACK;
import static de.gutenko.roguelike.entities.Entity.TurnAction.MOVE;

/**
 * Created by Peter on 3/10/18.
 */

public class Enemy extends Entity {

    public Enemy() {
        renderX = tileX = 6;
        renderY = tileY = 5;

        name = "slime";
        deathStr = "The slime melts away.";
        spriteName = Const.TEX_SLIME;

        spriteX = 4;
        spriteY = 2;
        spriteInd = 1;
    }

    @Override
    public TurnAction act() {
        Player p = DungeonScene.getInstance().getPlayer();
        int distance = Math.abs(p.tileX-tileX) + Math.abs(p.tileY-tileY); // manhattan distance from player tile to current tile

        if (distance > 1) {
            // move closer to the player
            moveValue = pathfindTo(p.tileX, p.tileY);
            return MOVE;
        } else {
            // attack the player
            targetEntity = p;
            return ATTACK;
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
