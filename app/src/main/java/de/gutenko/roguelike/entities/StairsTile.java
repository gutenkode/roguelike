package de.gutenko.roguelike.entities;

import android.opengl.Matrix;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.scenes.DungeonScene;

public class StairsTile extends TileSprite {

    public StairsTile(int x, int y) {
        super(x,y);
    }

    @Override
    public void onPlayerEnter() {
        DungeonScene.getInstance().loadNextFloor();
    }

    @Override
    public void render(MVPMatrix matrix) {
        Matrix.translateM(matrix.viewMatrix,0,X,Y,0);
        Shader.setMatrix(matrix);
        Texture.bindUnfiltered(Const.TEX_TILESET);
        Shader.setUniformFloat("spriteInfo",8,8,51);
        DungeonScene.quadMesh.render();
    }
}
