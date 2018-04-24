package de.gutenko.roguelike.entities;

import android.opengl.Matrix;

import java.util.List;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.data.pathfind.Pathfind;
import de.gutenko.roguelike.data.pathfind.State;
import de.gutenko.roguelike.scenes.DungeonScene;

/**
 * Created by Peter on 3/10/18.
 */

public abstract class Entity {

    public enum TurnAction {
        NONE,
        MOVE,
        ATTACK,
        WAIT;
        public void act(Entity e) {
            switch (this) {
                case MOVE:
                    e.move();
                    break;
                case ATTACK:
                    e.attack();
                    break;
                default:
                    break;
            }
        }
    }

    public int tileX, tileY;
    protected int spriteX, spriteY, spriteInd;
    protected float
            renderX, renderY,
            attackX, attackY, atkValX, atkValY, attackCoef,
            shake, shakeVel,
            maxHealth = 1, health = maxHealth;
    protected String name, deathStr, spriteName;

    protected Entity targetEntity;
    protected int[] moveValue;

    ////////////////

    public void renderStep() {}
    public void render(MVPMatrix matrix) {
        updateRenderPos();
        Matrix.translateM(matrix.viewMatrix,0,renderX+attackX+shake,renderY+attackY,0);
        Shader.setMatrix(matrix);
        Texture.bindUnfiltered(spriteName);
        Shader.setUniformFloat("spriteInfo",spriteX,spriteY,spriteInd);
        DungeonScene.quadMesh.setColor(1,health/maxHealth,health/maxHealth,1);
        DungeonScene.quadMesh.render();
        DungeonScene.quadMesh.setColor(1,1,1,1);
    }

    public abstract TurnAction act();

    ////////////////

    protected void updateRenderPos() {
        float speed = .25f;

        if (renderX > tileX+speed)
            renderX -= speed;
        else if (renderX < tileX-speed)
            renderX += speed;
        else
            renderX = tileX;

        if (renderY > tileY + speed)
            renderY -= speed;
        else if (renderY < tileY - speed)
            renderY += speed;
        else
            renderY = tileY;

        if (attackCoef < 3.141f)
            attackCoef += 3.141f/9;
        else
            attackCoef = 3.141f;
        attackX = atkValX*(float)Math.sin(attackCoef);
        attackY = atkValY*(float)Math.sin(attackCoef);

        shake *= .5;
        shake += shakeVel;
        shakeVel -= shake*.8;
    }
    public boolean isAnimating() {
        return (renderX != tileX || renderY != tileY ||
                attackCoef != 3.141f ||
                Math.abs(shake) > .01);
    }
    protected int[] pathfindTo(int targetX, int targetY) {
        List<State> path = Pathfind.pathfind(tileX,tileY,targetX,targetY);
        /*
        Log.d("PathGoal",targetX+","+targetY);
        int i = 0;
        for (State s : path) {
            Log.d("Path"+i,s.toString());
            i++;
        }
        Log.d("PathStart",tileX+","+tileY);
        */
        //if (path.size() < 2)
        //    return fallbackPathfind(targetX,targetY);
        State next = path.get(path.size()-2);
        if (DungeonScene.getInstance().isTileUnoccupied(next.tileX,next.tileY)) {
            return new int[] {next.tileX-tileX,next.tileY-tileY};
        } else
            return new int[] {0,0};
    }

    ////////////////

    public void move() {
        if (DungeonScene.getInstance().isTileUnoccupied(tileX+moveValue[0],tileY+moveValue[1])) {
            tileX += moveValue[0];
            tileY += moveValue[1];
        }
    }

    public void attack() {
        DungeonScene.getInstance().log(getAttackString(targetEntity));
        targetEntity.damage(.4f);
        attackCoef = 0;
        atkValX = -(tileX-targetEntity.tileX)/2f; // quick and dirty attack animation
        atkValY = -(tileY-targetEntity.tileY)/2f;
    }

    protected void damage(float dmg) {
        health -= dmg;
        shakeVel = .2f;
        health = Math.max(0,health);
    }

    ////////////////

    public float getHealth() { return health; }
    public String getDeathString() { return deathStr; }
    public abstract String getAttackString(Entity target);
}
