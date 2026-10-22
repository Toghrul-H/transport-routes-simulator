package com.transportroutes.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transport stop on the map.
 * 
 * <p>A stop is a location where passengers accumulate over time
 * and can be picked up by vehicles.</p>
 */
public class Stop {

    private int x, y;

    private int passengers = 0;
    private int maxPassengers = 50;

    private List<int[]> points = new ArrayList<>();

    /**
     * Creates a stop at a specific position.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Stop(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds a point (used if the stop stores route-related points).
     */
    public void addPoint(int x, int y) {
        points.add(new int[]{x, y});
    }

    /** @return number of stored points */
    public int size() {
        return points.size();
    }

    /** @return point at given index */
    public int[] get(int index) {
        return points.get(index);
    }

    /** @return all stored points */
    public List<int[]> getPoints() {
        return points;
    }

    /**
     * Updates passenger growth over time.
     *
     * @param speed game speed multiplier
     */
    public void update(double speed) {
        passengers += Math.max(1, (int) (0.3 * speed));

        if (passengers > maxPassengers) {
            passengers = maxPassengers;
        }
    }

    /**
     * Adds passengers manually.
     *
     * @param amount number of passengers
     */
    public void addPassengers(int amount) {
        passengers += amount;

        if (passengers > maxPassengers) {
            passengers = maxPassengers;
        }
    }

    /**
     * Removes passengers when picked up by a vehicle.
     *
     * @param capacity maximum passengers vehicle can take
     * @return number of passengers taken
     */
    public int pickPassengers(int capacity) {
        int taken = Math.min(passengers, capacity);
        passengers -= taken;
        return taken;
    }

    /** @return current number of passengers */
    public int getPassengers() {
        return passengers;
    }

    /** @return x-coordinate */
    public int getX() { return x; }

    /** @return y-coordinate */
    public int getY() { return y; }
}