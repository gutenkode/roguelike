package de.gutenko.roguelike.loop;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.gutenko.motes.render.MVPMatrix;
import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.motes.scenegraph.Scene;
import de.gutenko.roguelike.scenes.DungeonScene;

import static android.opengl.GLES10.*;

public class GameLoop implements GLSurfaceView.Renderer {

    private static GameLoop instance;
    public static GameLoop getInstance() {
        if (instance == null)
            instance = new GameLoop();
        return instance;
    }

    private float aspectRatio = 16/9f; // a general default, should never actually be used, overwritten in onSurfaceChanged
    private int width, height;
    private MVPMatrix screenMatrix;

    private Scene currentScene;

    // init non-rendering data
    private GameLoop() {
        screenMatrix = new MVPMatrix();
        currentScene = new DungeonScene();
    }

    // init rendering data
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        Shader.loadAll();
        Texture.createTextures();
    }

    // set display/aspect ratio stuff
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        currentScene.onSurfaceChanged(width, height);
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);

        float ar = (float) width / height;
        aspectRatio = ar;

        Matrix.orthoM(screenMatrix.projectionMatrix, 0, -ar, ar, -1, 1, -1, 1);
        Matrix.setIdentityM(screenMatrix.viewMatrix, 0);
    }

    // draw a frame
    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Shader.setMatrix(screenMatrix);
        currentScene.onDrawFrame();
    }

    private void makeFramebufferCurrent() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, width, height);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getAspectRatio() { return aspectRatio; }
}
