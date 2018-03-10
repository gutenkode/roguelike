package de.gutenko.roguelike.scenes;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.motes.render.mesh.TexMesh;
import de.gutenko.motes.scenegraph.Scene;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.entities.Enemy;
import de.gutenko.roguelike.entities.Entity;

/**
 * Created by Peter on 2/1/18.
 */

public class DungeonScene implements Scene {

    public static TexMesh quadMesh;
    static {
        float[] vertices = {0,0, 1,0, 1,1, 0,1};
        quadMesh = new TexMesh(2, vertices, GLES20.GL_TRIANGLE_FAN);
        quadMesh.setTexCoords(new float[] {0,0, 1,0, 1,1, 0,1});
    }

    private int[][] map;
    private MVPMatrix matrix;
    private List<Entity> entities;

    public DungeonScene() {
        matrix = new MVPMatrix();
        map = new int[10][10];
        for (int[] i : map)
            for (int i2 = 0; i2 < i.length; i2++) {
                i[i2] = (int)(Math.random()*3);
            }

        entities = new ArrayList<>();
        entities.add(new Enemy());
    }

    @Override
    public void onDrawFrame() {
        Shader.use(Const.SHADER_SPRITE);
        Texture.bindUnfiltered(Const.TEX_TILESET);
        resetMatrix();

        for (int[] i : map) {
            for (int i2 = 0; i2 < i.length; i2++) {
                Shader.setMatrix(matrix);
                Shader.setUniformFloat("spriteInfo",8,8, i[i2]);
                quadMesh.render();
                Matrix.translateM(matrix.viewMatrix, 0, 1, 0, 0);
            }
            Matrix.translateM(matrix.viewMatrix,0, -i.length,1,0);
        }

        for (Entity e : entities) {
            resetMatrix();
            e.render(matrix);
        }
    }
    private void resetMatrix() {
        Matrix.setIdentityM(matrix.viewMatrix,0);
        Matrix.translateM(matrix.viewMatrix,0, -1,-1,0);
        Matrix.scaleM(matrix.viewMatrix,0, .2f,.2f,1);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        float ar = (float) width / height;

        Matrix.orthoM(matrix.projectionMatrix, 0, -ar, ar, -1, 1, -1, 1);
        //Matrix.setIdentityM(matrix.viewMatrix, 0);
    }
}
