package de.gutenko.roguelike.scenes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.motes.render.mesh.FontUtils;
import de.gutenko.motes.render.mesh.TexMesh;
import de.gutenko.motes.scenegraph.Scene;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.data.Input;
import de.gutenko.roguelike.data.map.MapBuilder;
import de.gutenko.roguelike.data.map.MapDefinition;
import de.gutenko.roguelike.data.map.Minimap;
import de.gutenko.roguelike.entities.Enemy;
import de.gutenko.roguelike.entities.Entity;
import de.gutenko.roguelike.entities.PlayerEntity;
import de.gutenko.roguelike.entities.TileSprite;
import de.gutenko.roguelike.habittracker.data.player.Player;

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


    private int currentFloor, mapX, mapY, scrollX, scrollY;
    private double scrollDelay;
    private float aspectRatio, mapScale, logOffset, logAutoClearDelay = 90;
    private boolean[][] solid;
    private int[][] tiles;
    private MVPMatrix matrix;
    private Map<Integer, MapDefinition> dungeonRooms;
    private List<TileSprite> tileSprites;
    private List<Enemy> enemies, queuedActionEnemies;
    private List<Pair<Entity,Entity.TurnAction>> queuedActions;
    private List<TexMesh> logList;
    private int maxLogLength = 4;
    private PlayerEntity player;
    private boolean isPlayerTurn = true, isPlayingAnimations = false, isProcessingActions = true, roomTransitioned = false;

    private DungeonScene() {
        matrix = new MVPMatrix();
        dungeonRooms = new HashMap<>();
        logList = new ArrayList<>();
        enemies = new ArrayList<>();
        queuedActionEnemies = new ArrayList<>();
        queuedActions = new ArrayList<>();

        currentFloor = 0;
        loadNextFloor();
    }
    public void loadNextFloor() {
        currentFloor++;
        dungeonRooms.clear();

        Minimap.reset(64,64);
        loadMap(64,64);
        player = PlayerEntity.moveInstanceTo(1,1);
        //player = PlayerEntity.getInstance();

        log("Welcome to Floor "+currentFloor+".");
    }
    private void loadMap(int x, int y) {
        mapX = x;
        mapY = y;
        int key = mapKey(x,y);
        //int key = ((x << 16) & 0xFF00)+(y & 0x00FF);
        MapDefinition map;
        if (dungeonRooms.containsKey(key)) {
            map = dungeonRooms.get(key);
        } else {
            map = MapBuilder.createMap(8,12, currentFloor);
            dungeonRooms.put(key,map);
            Minimap.addRoom(mapX,mapY);
        }
        Minimap.setCurrentRoom(mapX,mapY);

        //enemies.clear();
        queuedActionEnemies.clear();
        queuedActions.clear();

        solid = map.solid;
        tiles = map.tiles;
        enemies = map.enemies;
        tileSprites = map.tileSprites;

        mapScale = tiles.length; // number of rows that fit on the screen
    }

    @Override
    public void onDrawFrame() {
        Shader.use(Const.SHADER_SPRITE);

        // render ground tiles
        Texture.bindUnfiltered(Const.TEX_TILESET);
        renderTiles(tiles, 0,0);
        if (scrollX != 0)
            renderTiles(dungeonRooms.get(mapKey(mapX-(int)Math.signum(scrollX),mapY)).tiles, -tiles.length*(int)Math.signum(scrollX),0);
        else if (scrollY != 0)
            renderTiles(dungeonRooms.get(mapKey(mapX,mapY-(int)Math.signum(scrollY))).tiles, 0, -tiles[0].length*(int)Math.signum(scrollY));

        // render tile sprites
        for (TileSprite t : tileSprites) {
            resetMatrix();
            t.render(matrix);
        }

        // render enemies and the player
        for (Enemy e : enemies) {
            resetMatrix();
            e.renderStep();
            e.render(matrix);
        }
        resetMatrix();
        player.renderStep();
        Matrix.translateM(matrix.viewMatrix,0,-scrollX/2f/tiles.length,-scrollY/2f/tiles[0].length,0);
        player.render(matrix);

        for (Enemy e : enemies) {
            resetMatrix();
            e.renderHealthBar(matrix);
        }
        resetMatrix();
        Matrix.translateM(matrix.viewMatrix,0,-scrollX/2f/tiles.length,-scrollY/2f/tiles[0].length,0);
        player.renderHealthBar(matrix);

        ///////////////////////////////
        // UI STUFF HERE
        ///////////////////////////////

        // background texture for status bar
        Shader.use(Const.SHADER_TEXTURE);
        Texture.bindUnfiltered(Const.TEX_STATUSBAR);
        resetMatrixNoScroll();
        Matrix.translateM(matrix.viewMatrix,0,0,tiles[0].length,0);
        float totalHeight = (1/aspectRatio)*mapScale;
        float statusbarHeight = totalHeight-tiles[0].length;
        Matrix.scaleM(matrix.viewMatrix,0,mapScale,statusbarHeight,1);
        Shader.setMatrix(matrix);
        quadMesh.render();

        // render log text
        if (logOffset > 0)
            logOffset -= .1;
        else
            logOffset = 0;
        if (logAutoClearDelay <= 0)
            clearTopLogEntry();
        else
            logAutoClearDelay--;
        Texture.bindUnfiltered(Const.TEX_FONT);
        resetMatrixNoScroll();
        Matrix.translateM(matrix.viewMatrix,0, .33f,logOffset+.15f+ tiles[0].length,0);
        for (TexMesh m : logList) {
            Shader.setMatrix(matrix);
            m.render();
            Matrix.translateM(matrix.viewMatrix,0, 0,.5f,0);
        }

        // render minimap
        Shader.use(Const.SHADER_SPRITE);
        resetMatrixNoScroll();
        Matrix.translateM(matrix.viewMatrix,0, tiles.length-1.5f,tiles[0].length+1.25f,0);
        Minimap.render(matrix);

        // game logic update
        update();
    }
    private void renderTiles(int[][] tiles, int xOffset, int yOffset) {
        resetMatrix();
        Matrix.translateM(matrix.viewMatrix,0,xOffset,yOffset,0);
        for (int[] i : tiles) {
            for (int i2 = 0; i2 < i.length; i2++) {
                Shader.setMatrix(matrix);
                Shader.setUniformFloat("spriteInfo",8,8, i[i2]);
                quadMesh.render();
                Matrix.translateM(matrix.viewMatrix, 0, 0, 1, 0);
            }
            Matrix.translateM(matrix.viewMatrix,0, 1,-i.length,0);
        }
    }

    private void update() {
        isPlayingAnimations = player.isAnimating();
        for (Enemy e : enemies)
            isPlayingAnimations = isPlayingAnimations || e.isAnimating();
        if (scrollX != 0 || scrollY != 0) {
            isPlayingAnimations = true;
            if (scrollDelay <= 0) {
                if (scrollX != 0)
                    scrollX -= Math.signum(scrollX);
                if (scrollY != 0)
                    scrollY -= Math.signum(scrollY);
                scrollDelay = .01;
            } else
                scrollDelay -= 1/60.0; // hardcoded delta time of 60fps
        }

        if (!isPlayingAnimations)
        {
            // process actions
            if (isProcessingActions) {
                processActions();
                isProcessingActions = !queuedActions.isEmpty();
            } else
            {
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

                // process tile sprite events
                for (TileSprite t : tileSprites) {
                    if (t.X == player.tileX && t.Y == player.tileY)
                        t.onPlayerEnter();
                }
                // room transition
                if (player.tileX == 0 || player.tileY == 0 || player.tileX == tiles.length-1 || player.tileY == tiles[0].length-1) {
                    if (!roomTransitioned) {
                        roomTransitioned = true;
                        if (player.tileX == 0 || player.tileX == tiles.length-1) {
                            if (player.tileX == 0) {
                                loadMap(mapX-1, mapY);
                                scrollX = 2*-tiles.length;
                            } else {
                                loadMap(mapX + 1, mapY);
                                scrollX = 2*tiles.length;
                            }
                            player.teleportTo(tiles.length - 1 - player.tileX, player.tileY);
                        } else {
                            if (player.tileY == 0) {
                                loadMap(mapX, mapY-1);
                                scrollY = 2*-tiles[0].length;
                            } else {
                                loadMap(mapX, mapY+1);
                                scrollY = 2*tiles[0].length;
                            }
                            player.teleportTo(player.tileX, tiles[0].length - 1 - player.tileY);
                        }
                    }
                } else
                    roomTransitioned = false;
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
        resetMatrixNoScroll();
        Matrix.translateM(matrix.viewMatrix,0,scrollX/2f,scrollY/2f,0);
    }
    private void resetMatrixNoScroll() {
        Matrix.setIdentityM(matrix.viewMatrix,0);
        Matrix.scaleM(matrix.viewMatrix,0, 1,-1,1);
        Matrix.translateM(matrix.viewMatrix,0, 0,-1,0);
        float scale = 1f/mapScale;//1f/mapScale*2
        Matrix.scaleM(matrix.viewMatrix,0, scale,scale,1);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        aspectRatio = (float) width / height;
        float bottomScale = ((float)height/width)-1;

        Matrix.orthoM(matrix.projectionMatrix, 0, 0, 1, -bottomScale, 1, -1, 1);
        //Matrix.orthoM(matrix.projectionMatrix, 0, -ar, ar, -1, 1, -1, 1);
    }
    public float getAspectRatio() { return aspectRatio; }
    public float getMapScale() { return mapScale; }
    public boolean isTileWalkable(int x, int y) {
        if (x < 0 || y < 0 || x > tiles.length-1 || y > tiles[0].length-1) // in-bounds check
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
    public PlayerEntity getPlayer() { return player; }
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

    private int mapKey(int x, int y) {
        return (x << 16)+(y & 0xFF);
    }
}
