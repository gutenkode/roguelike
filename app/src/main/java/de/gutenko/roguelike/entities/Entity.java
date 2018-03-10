package de.gutenko.roguelike.entities;

import de.gutenko.motes.render.MVPMatrix;

/**
 * Created by Peter on 3/10/18.
 */

public abstract class Entity {
    public int tileX, tileY;
    public abstract void render(MVPMatrix matrix);
    public abstract void act();
}
