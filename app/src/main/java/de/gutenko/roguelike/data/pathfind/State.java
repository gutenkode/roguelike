package de.gutenko.roguelike.data.pathfind;

import java.util.*;

import de.gutenko.roguelike.scenes.DungeonScene;

public class State {

    public final int tileX, tileY;
    private Set<State> childStates;

    public State(int x, int y) {
        tileX = x;
        tileY = y;
    }

    public Set<State> getChildStates() {
        if (childStates == null) {
            childStates = new HashSet<>();
            addChild(1,0,childStates);
            addChild(-1,0,childStates);
            addChild(0,1,childStates);
            addChild(0,-1,childStates);

            addChild(1,1,childStates);
            addChild(-1,1,childStates);
            addChild(1,-1,childStates);
            addChild(-1,-1,childStates);
        }
        return childStates;
    }

    private void addChild(int x, int y, Set<State> c) {
        if (DungeonScene.getInstance().isTileWalkable(tileX+x, tileY+y))
            c.add(new State(tileX+x, tileY+y));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof State) {
            State s = (State)o;
            return tileX==s.tileX && tileY==s.tileY;
        }
        return false;
    }

    @Override
    public String toString() {
        return tileX+","+tileY;
    }
}
