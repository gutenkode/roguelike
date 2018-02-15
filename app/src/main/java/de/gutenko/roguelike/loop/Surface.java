package de.gutenko.roguelike.loop;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class Surface extends GLSurfaceView {

    private final GameLoop renderer;

    public Surface(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        // void android.opengl.GLSurfaceView.setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize)
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        renderer = GameLoop.getInstance();

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // crashes
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }

    public GameLoop getGameLoop() { return renderer; }
}
