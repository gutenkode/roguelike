package de.gutenko.roguelike.data.map;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.scenes.DungeonScene;

public class Minimap {

    private static int startX, startY, currentX, currentY;
    private static List<int[]> rooms;

    public static void reset(int x, int y) {
        rooms = new ArrayList<>();
        startX = x;
        startY = y;
        currentX = 0;
        currentY = 0;
    }
    public static void addRoom(int x, int y) {
        rooms.add(new int[] {x-startX,y-startY});
    }
    public static void setCurrentRoom(int x, int y) {
        currentX = x-startX;
        currentY = y-startY;
    }

    public static void render(MVPMatrix matrix) {
        Texture.bindUnfiltered(Const.TEX_MAP);
        Matrix.scaleM(matrix.viewMatrix,0,.3f,.3f,0);
        float[] base = matrix.viewMatrix;

        // draw background grid
        for (int x = -3; x <= 3; x++)
            for (int y = -3; y <= 3; y++) {
                matrix.viewMatrix = base.clone();
                Matrix.translateM(matrix.viewMatrix,0, x,y,0);
                Shader.setMatrix(matrix);
                Shader.setUniformFloat("spriteInfo",4,1, 0f);
                DungeonScene.quadMesh.render();
        }

        // draw visited rooms
        for (int[] pos : rooms) {
            matrix.viewMatrix = base.clone();
            Matrix.translateM(matrix.viewMatrix,0, pos[0],pos[1],0);
            Shader.setMatrix(matrix);
            Shader.setUniformFloat("spriteInfo",4,1, 1f);
            DungeonScene.quadMesh.render();
        }

        // draw current room
        matrix.viewMatrix = base.clone();
        Matrix.translateM(matrix.viewMatrix,0, currentX,currentY,0);
        Shader.setMatrix(matrix);
        Shader.setUniformFloat("spriteInfo",4,1, 2f);
        DungeonScene.quadMesh.render();
    }
}
