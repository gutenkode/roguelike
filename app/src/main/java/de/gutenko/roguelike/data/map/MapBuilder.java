package de.gutenko.roguelike.data.map;

import android.util.Pair;

public class MapBuilder {

    public static Pair<boolean[][],int[][]> createBorderRoom(int w, int h) {
        boolean[][] solid = new boolean[w][h];
        int[][] map = new int[w][h];

        for (boolean[] i : solid)
            for (int i2 = 0; i2 < i.length; i2++)
                i[i2] = false;

        for (int i = 0; i < solid.length; i++) {
            solid[i][0] = true;
            solid[i][solid[i].length-1] = true;
        }
        for (int i = 0; i < solid[0].length; i++) {
            solid[0][i] = true;
            solid[solid.length-1][i] = true;
        }
        solid[2][2] = true;
        solid[3][2] = true;
        solid[5][6] = true;
        solid[5][7] = true;
        solid[4][7] = true;

        // create the tilemap
        for (int x = 0; x < map.length; x++)
            for (int y = 0; y < map[0].length; y++) {
                boolean c = test(x,y,solid);
                boolean u = test(x,y-1,solid);
                boolean d = test(x,y+1,solid);
                boolean l = test(x-1,y,solid);
                boolean r = test(x+1,y,solid);
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
                    else if (d &&  l)
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

                    if (l && u && !d && r)
                        map[x][y] = 12;
                    else if (l && !u && !d && !r)
                        map[x][y] = 18;
                    else if (!l && !u && !d && r)
                        map[x][y] = 16;

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
        return new Pair(solid,map);
    }
    private static boolean test(int x, int y, boolean[][] solid) {
        if (x >= solid.length || x < 0 || y >= solid[0].length || y < 0)
            return false;
        return solid[x][y];
    }
}
