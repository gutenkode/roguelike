package de.gutenko.roguelike.data.map;

import java.util.ArrayList;
import java.util.List;

import de.gutenko.roguelike.entities.Enemy;
import de.gutenko.roguelike.entities.PlayerEntity;
import de.gutenko.roguelike.entities.StairsTile;
import de.gutenko.roguelike.entities.TileSprite;

import static de.gutenko.roguelike.entities.Enemy.EnemyType.RAT;
import static de.gutenko.roguelike.entities.Enemy.EnemyType.SLIME;

public class MapBuilder {

    public static MapDefinition createMap(int w, int h, int floorNum) {
        boolean[][] solid = new boolean[w][h];
        int[][] map = new int[w][h];

        // default no solid walls
        for (boolean[] i : solid)
            for (int i2 = 0; i2 < i.length; i2++)
                i[i2] = false;

        // solid borders
        for (int i = 0; i < solid.length; i++) {
            solid[i][0] = true;
            solid[i][solid[i].length-1] = true;
        }
        for (int i = 0; i < solid[0].length; i++) {
            solid[0][i] = true;
            solid[solid.length-1][i] = true;
        }
        // doors
        solid[0][h/2] = false;
        solid[0][h/2-1] = false;
        solid[w-1][h/2] = false;
        solid[w-1][h/2-1] = false;
        solid[w/2][0] = false;
        solid[w/2-1][0] = false;
        solid[w/2][h-1] = false;
        solid[w/2-1][h-1] = false;

        // hardcoded wall placement
        /*
        if (floorNum == 1) {
            solid[2][2] = true;
            solid[3][2] = true;
            solid[5][6] = true;
            solid[5][7] = true;
            solid[4][7] = true;
        } else {
            solid[6][8] = true;
            solid[4][8] = true;
            solid[3][8] = true;
            solid[3][9] = true;
            solid[3][10] = true;

            solid[2][2] = true;
            solid[2][3] = true;
            solid[4][3] = true;

            solid[5][3] = true;
            solid[6][3] = true;
        }
        */
        // random wall placement
        int numWalls = 0;
        for (int x = 1; x < solid.length-1; x++) {
            for (int y = 1; y < solid[0].length-1; y++) {
                if (Math.random() > .6+.1*(numWalls/((double)floorNum))) {
                    solid[x][y] = true;
                    numWalls++;
                }
            }
        }

        createTilemap(solid, map);

        List<Enemy> enemies = new ArrayList<>();
        // hardcoded enemy placement
        if (floorNum == 1) {
            //PlayerEntity.moveInstanceTo(2,3);
            enemies.add(new Enemy(5, 5, SLIME));
            enemies.add(new Enemy(4, 6, SLIME));
            enemies.add(new Enemy(2, 8, RAT));
        } else {
            //PlayerEntity.moveInstanceTo(5,1);
            enemies.add(new Enemy(3, 4, SLIME));
            enemies.add(new Enemy(5, 5, SLIME));
            enemies.add(new Enemy(4, 6, RAT));
            enemies.add(new Enemy(2, 8, RAT));
            enemies.add(new Enemy(6, 9, SLIME));
            enemies.add(new Enemy(4, 10, RAT));
        }
        List<TileSprite> tiles = new ArrayList<>();
        tiles.add(new StairsTile(w-3,h-2));

        return new MapDefinition(solid,map,enemies,tiles);
    }
    private static boolean test(int x, int y, boolean[][] solid) {
        if (x >= solid.length || x < 0 || y >= solid[0].length || y < 0)
            return false;
        return solid[x][y];
    }
    private static void createTilemap(boolean[][] solid, int[][] map) {
        // create tilemap from wall data
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                boolean c = test(x, y, solid);
                boolean u = test(x, y - 1, solid);
                boolean d = test(x, y + 1, solid);
                boolean l = test(x - 1, y, solid);
                boolean r = test(x + 1, y, solid);
                if (!c) {
                    // floors

                    // thin straight floors
                    if (u && d && !l && !r)
                        map[x][y] = 37;
                    else if (!u && !d && l && r)
                        map[x][y] = 35;

                        // floor corners
                    else if (u && l)
                        map[x][y] = 24;
                    else if (u && r)
                        map[x][y] = 26;
                    else if (d && l)
                        map[x][y] = 40;
                    else if (d && r)
                        map[x][y] = 42;

                    else if (u)
                        map[x][y] = 25;
                    else if (d)
                        map[x][y] = 41;
                    else if (l)
                        map[x][y] = 32;
                    else if (r)
                        map[x][y] = 34;

                    else
                        map[x][y] = 33;
                } else {
                    // walls

                    // edges
                    if (l && u && !d && r)
                        map[x][y] = 12;
                    else if (l && !u && !d && !r)
                        map[x][y] = 18;
                    else if (!l && !u && !d && r)
                        map[x][y] = 16;
                    else if (!l && u && !d && !r)
                        map[x][y] = 9;
                    else if (!l && !u && d && !r)
                        map[x][y] = 8;

                        // t-walls
                    else if (l && u && d && !r)
                        map[x][y] = 13;
                    else if (!l && u && d && r)
                        map[x][y] = 11;
                    else if (l && !u && d && r)
                        map[x][y] = 4;
                    else if (l && u && !d && r)
                        map[x][y] = 1;

                        // corners
                    else if (l && u && !d && !r)
                        map[x][y] = 18;
                    else if (!l && u && !d && r)
                        map[x][y] = 16;
                    else if (l && !u && d && !r)
                        map[x][y] = 2;
                    else if (!l && !u && d && r)
                        map[x][y] = 0;
                        // up/down walls
                    else if ((l || r) && !u && !d)
                        map[x][y] = 1;
                    else if (!l && !r && (u || d))
                        map[x][y] = 8;
                    else if (!l && !r && !u && !d)
                        map[x][y] = 9;
                }
            }
        }
    }
}
