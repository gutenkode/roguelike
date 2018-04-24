package de.gutenko.roguelike.entities;

import de.gutenko.motes.render.MVPMatrix;

/**
 * Non-entity objects in the world that can be interacted with, like stairs.
 */
public abstract class TileSprite {
    public final int X,Y;
    public TileSprite(int x, int y) {
        X = x;
        Y = y;
    }

    public abstract void onPlayerEnter();
    public abstract void render(MVPMatrix matrix);
}
