package de.gutenko.motes.scenegraph;

/**
 * Created by Peter on 12/31/17.
 */

public interface Scene {
    void onDrawFrame();
    void onSurfaceChanged(int width, int height);
}
