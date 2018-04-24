package de.gutenko.roguelike.scenes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.motes.render.mesh.FontUtils;
import de.gutenko.motes.render.mesh.TexMesh;
import de.gutenko.motes.scenegraph.Scene;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.data.Input;
import de.gutenko.roguelike.data.map.MapBuilder;
import de.gutenko.roguelike.entities.Enemy;
import de.gutenko.roguelike.entities.Entity;
import de.gutenko.roguelike.entities.Player;

/**
 * Created by Peter on 2/1/18.
 */

public class DungeonScene implements Scene {

    public static TexMesh quadMesh;
    private static DungeonScene instance;
    static {
        float[] vertices = {0,0, 1,0, 1,1, 0,1};
        quadMesh = new TexMesh(2, vertices, GLES20.GL_TRIANGLE_FAN);
        quadMesh.setTexCoords(new float[] {0,0, 1,0, 1,1, 0,1});
    }
    public static DungeonScene getInstance() {
        if (instance == null)
            instance = new DungeonScene();
        return instance;
    }

    private float aspectRatio, mapScale, logOffset, logAutoClearDelay = 90;
    private boolean[][] solid;
    private int[][] map;
    private MVPMatrix matrix;
    private List<Enemy> enemies, queuedActionEnemies;
    private List<Pair<Entity,Entity.TurnAction>> queuedActions;
    private List<TexMesh> logList;
    private int maxLogLength = 4;
    private Player player;
    private boolean isPlayerTurn = true, isPlayingAnimations = false, isProcessingActions = true;

    private DungeonScene() {
        matrix = new MVPMatrix();
        Pair<boolean[][],int[][]> p = MapBuilder.createBorderRoom(8,12);
        solid = p.first;
        map = p.second;
        mapScale = map.length; // number of rows that fit on the screen

        logList = new ArrayList<>();
        enemies = new ArrayList<>();
        queuedActionEnemies = new ArrayList<>();
        queuedActions = new ArrayList<>();

        log("Welcome to Floor 1.");
        player = new Player();
        for (int i = 0; i < 3; i++)
            enemies.add(new Enemy());
    }

    @Override
    public void onDrawFrame() {
        Shader.use(Const.SHADER_SPRITE);
        Texture.bindUnfiltered(Const.TEX_TILESET);
        resetMatrix();

        // render ground tiles
        for (int[] i : map) {
            for (int i2 = 0; i2 < i.length; i2++) {
                Shader.setMatrix(matrix);
                Shader.setUniformFloat("spriteInfo",8,8, i[i2]);
                quadMesh.render();
                Matrix.translateM(matrix.viewMatrix, 0, 0, 1, 0);
            }
            Matrix.translateM(matrix.viewMatrix,0, 1,-i.length,0);
        }

        // render enemies and the player
        for (Enemy e : enemies) {
            resetMatrix();
            e.renderStep();
            e.render(matrix);
        }
        resetMatrix();
        player.renderStep();
        player.render(matrix);

        // render log text
        if (logOffset > 0)
            logOffset -= .1;
        else
            logOffset = 0;
        if (logAutoClearDelay <= 0)
            clearTopLogEntry();
        else
            logAutoClearDelay--;
        Shader.use(Const.SHADER_TEXTURE);
        Texture.bindUnfiltered(Const.TEX_FONT);
        resetMatrix();
        Matrix.translateM(matrix.viewMatrix,0, .33f,logOffset+.15f+map[0].length,0);
        for (TexMesh m : logList) {
            Shader.setMatrix(matrix);
            m.render();
            Matrix.translateM(matrix.viewMatrix,0, 0,.5f,0);
        }

        // game logic update
        update();
    }

    private void update() {
        isPlayingAnimations = player.isAnimating();
        for (Enemy e : enemies)
            isPlayingAnimations = isPlayingAnimations || e.isAnimating();

        if (!isPlayingAnimations)
        {
            // process actions
            if (isProcessingActions) {
                processActions();
                isProcessingActions = !queuedActions.isEmpty();
            } else {
                // only take the next turn once all animations and actions are done
                if (isPlayerTurn) {
                    Entity.TurnAction a = player.act(); // wait for player input
                    if (a != Entity.TurnAction.NONE) { // if the player has taken an action, let enemies act next
                        queuedActions.add(new Pair(player,a));
                        isPlayerTurn = false;
                        isProcessingActions = true;
                        queuedActionEnemies.clear();
                        queuedActionEnemies.addAll(enemies); // queue all enemies for actions
                    }
                } else {
                    Iterator<Enemy> i = queuedActionEnemies.iterator();
                    while (i.hasNext()) {
                        Enemy e = i.next();
                        Entity.TurnAction a = e.act();
                        if (a != Entity.TurnAction.NONE) {
                            queuedActions.add(new Pair(e,a));
                            i.remove(); // this enemy has acted, remove it from the queue
                        }
                    }
                    if (queuedActionEnemies.isEmpty()) {
                        isProcessingActions = true;
                        isPlayerTurn = true;
                    }
                }
            }
        }

        // remove dead enemies
        Iterator<Enemy> i = enemies.iterator();
        while (i.hasNext()) {
            Enemy e = i.next();
            if (e.getHealth() <= 0) {
                DungeonScene.getInstance().log(e.getDeathString());
                i.remove();
            }
        }
        if (player.getHealth() <= 0) {
            DungeonScene.getInstance().log(player.getDeathString());
        }
    }
    private void processActions() {
        // if there's an action that needs to be asynchronous (e.g. attacking), then find it and perform it first
        Iterator<Pair<Entity, Entity.TurnAction>> i = queuedActions.iterator();
        while (i.hasNext()) {
            Pair<Entity, Entity.TurnAction> p = i.next();
            if (p.first.getHealth() <= 0)
                i.remove();
            else if (p.second == Entity.TurnAction.ATTACK) {
                p.second.act(p.first);
                i.remove();
                return;
            }
        }
        // start all remaining actions, since they can be simultaneous
        for (Pair<Entity, Entity.TurnAction> p : queuedActions)
            p.second.act(p.first);
        queuedActions.clear();

        Input.wasTapEvent(); // eat any queued taps
    }

    private void resetMatrix() {
        Matrix.setIdentityM(matrix.viewMatrix,0);
        Matrix.scaleM(matrix.viewMatrix,0, 1,-1,0);
        Matrix.translateM(matrix.viewMatrix,0, 0,-1,0);
        float scale = 1f/mapScale;//1f/mapScale*2
        Matrix.scaleM(matrix.viewMatrix,0, scale,scale,1);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        aspectRatio = (float) width / height;

        Matrix.orthoM(matrix.projectionMatrix, 0, 0, aspectRatio*2, -1, 1, -1, 1);
        //Matrix.orthoM(matrix.projectionMatrix, 0, -ar, ar, -1, 1, -1, 1);
    }
    public float getAspectRatio() { return aspectRatio; }
    public float getMapScale() { return mapScale; }
    public boolean isTileWalkable(int x, int y) {
        if (x < 0 || y < 0 || x > map.length-1 || y > map[0].length-1) // in-bounds check
            return false;
       return !solid[x][y];
    }
    public boolean isTileUnoccupied(int x, int y) {
        for (Enemy e : enemies)
            if (e.tileX == x && e.tileY == y)
                return false;
        if (player.tileX == x && player.tileY == y)
            return false;
        return true;
    }
    public Player getPlayer() { return player; }
    public Enemy getEnemyAt(int x, int y) {
        for (Enemy e : enemies)
            if (e.tileX == x && e.tileY == y)
                return e;
        return null;
    }

    public void log(String s) {
        logAutoClearDelay = 90;
        logList.add(FontUtils.createString(s,0,0,.4f,.4f));
        while (logList.size() > maxLogLength) {
            logList.remove(0);
            logOffset += .5f;
        }
    }
    private void clearTopLogEntry() {
        logAutoClearDelay = 90;
        if (!logList.isEmpty()) {
            logList.remove(0);
            logOffset += .5f;
        }
    }
}
