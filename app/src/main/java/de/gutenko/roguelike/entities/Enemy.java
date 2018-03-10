package de.gutenko.roguelike.entities;

import android.opengl.Matrix;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.scenes.DungeonScene;

/**
 * Created by Peter on 3/10/18.
 */

public class Enemy extends Entity {

    public Enemy() {
        tileX = 3;
        tileY = 4;
    }
    @Override
    public void render(MVPMatrix matrix) {
        Matrix.translateM(matrix.viewMatrix,0,tileX,tileY,0);
        Shader.setMatrix(matrix);
        Texture.bindUnfiltered(Const.TEX_SLIME);
        Shader.setUniformFloat("spriteInfo",1,1,0);
        DungeonScene.quadMesh.render();
    }

    @Override
    public void act() {

    }
}
