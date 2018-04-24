package de.gutenko.roguelike.data.pathfind;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.gutenko.roguelike.scenes.DungeonScene;

/**
 * Created by Peter on 3/10/18.
 */

public class Pathfind {

    public static List<State> pathfind(int startX, int startY, int endX, int endY)
    {
        State start = new State(startX, startY);
        State goal = new State(endX, endY);
        List<State> path = aStar(start, goal);
        return path;
    }

    private static List<State> aStar(State start, State goal) {
        int limit = 100;

        Set<State> closed = new HashSet<>();

        Set<State> open = new HashSet<>();
        open.add(start);

        Map<State, State> cameFrom = new HashMap<>();

        Map<State, Double> gScore = new HashMap<>();
        gScore.put(start, 0d);

        Map<State, Double> fScore = new HashMap<>();
        fScore.put(start, getHeuristicScore(start, goal));

        while (!open.isEmpty() && limit > 0) {
            limit--;
            State parent = getLowestScoreState(open, fScore);
            if (parent.equals(goal))
                return getPath(cameFrom, parent, start);

            open.remove(parent);
            closed.add(parent);

            for (State child : parent.getChildStates())
            {
                if (closed.contains(child))
                    continue;

                double childGScore = gScore.get(parent) + getMoveCost(parent, child, goal);
                Double prevChildGScore = gScore.get(child);
                if (prevChildGScore != null && childGScore >= prevChildGScore)
                        continue;

                if (!open.contains(child))
                    open.add(child);

                cameFrom.put(child, parent);
                gScore.put(child, childGScore);
                fScore.put(child, childGScore + getHeuristicScore(child, goal));
            }
        }
        State bestGuess = getLowestScoreState(open, fScore);
        return getPath(cameFrom, bestGuess, start);
    }

    private static State getLowestScoreState(Set<State> open, Map<State, Double> fScore) {
        State minState = null;
        double minScore = Double.MAX_VALUE;
        for (State s : open) {
            double score = fScore.get(s);
            if (score < minScore) {
                minScore = score;
                minState = s;
            }
        }
        return minState;
    }

    private static double getMoveCost(State parent, State child, State goal) {
        if (child.equals(goal))
            return .1; // the goal is often an entity, so make sure it's cheap to move to
        double dist = getHeuristicScore(parent, child);
        if (!DungeonScene.getInstance().isTileUnoccupied(child.tileX,child.tileY))
            dist += 15; // high, but not infinity
        return dist;
    }

    private static double getHeuristicScore(State current, State goal) {
        // must be admissable, e.g. must never overestimate
        return Math.sqrt(Math.pow(current.tileX-goal.tileX,2)+Math.pow(current.tileY-goal.tileY,2));
    }

    private static List<State> getPath(Map<State, State> cameFrom, State current, State start) {
        List<State> path = new ArrayList<>();
        path.add(current);

        while (!current.equals(start)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        return path;
    }
}