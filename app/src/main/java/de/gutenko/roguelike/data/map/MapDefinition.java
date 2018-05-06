package de.gutenko.roguelike.data.map;

import java.util.List;

import de.gutenko.roguelike.entities.Enemy;
import de.gutenko.roguelike.entities.PlayerEntity;
import de.gutenko.roguelike.entities.TileSprite;

public class MapDefinition {
    public boolean[][] solid;
    public int[][] tiles;
    public List<Enemy> enemies;
    public List<TileSprite> tileSprites;

    public MapDefinition(boolean[][] s, int[][] t, List<Enemy> e, List<TileSprite> ts) {
        solid = s;
        tiles = t;
        enemies = e;
        tileSprites = ts;
    }

}
