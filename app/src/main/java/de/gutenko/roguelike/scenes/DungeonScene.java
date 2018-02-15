package de.gutenko.roguelike.scenes;

import de.gutenko.motes.scenegraph.Scene;

/**
 * Created by Peter on 2/1/18.
 */

public class DungeonScene implements Scene {

    private int[][] map;

    public DungeonScene() {
        map = new int[10][10];
        for (int[] i : map)
            for (int i2 = 0; i2 < i.length; i2++) {
                i[i2] = (int)(Math.random()*3);
            }
    }

    @Override
    public void render() {

    }
}
