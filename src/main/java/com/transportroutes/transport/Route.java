package com.transportroutes.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route that a vehicle follows.
 * 
 * <p>A route is a sequence of points (x, y coordinates)
 * that define the path a vehicle will travel.</p>
 */
public class Route {

    private List<int[]> points = new ArrayList<>();

    /**
     * Adds a new point to the route.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void addPoint(int x, int y) {
        points.add(new int[]{x, y});
    }

    /**
     * Returns the number of points in the route.
     *
     * @return route size
     */
    public int size() {
        return points.size();
    }

    /**
     * Returns a specific point in the route.
     *
     * @param index position in the route
     * @return coordinate as int array [x, y]
     */
    public int[] get(int index) {
        return points.get(index);
    }

    /**
     * Returns all route points.
     *
     * @return list of coordinates
     */
    public List<int[]> getPoints() {
        return points;
    }
}