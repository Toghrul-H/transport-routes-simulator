package com.transportroutes.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a garage on the map.
 * 
 * <p>A garage is a location where vehicles are stored, created,
 * and managed. Each garage keeps track of the vehicles assigned to it.</p>
 */
public class Garage {

    private int x;
    private int y;

    private List<Vehicle> vehicles = new ArrayList<>();

    /**
     * Creates a garage at a specific position on the map.
     *
     * @param x x-coordinate of the garage
     * @param y y-coordinate of the garage
     */
    public Garage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds a vehicle to this garage.
     * 
     * <p>A vehicle is only added if it is not already present.</p>
     *
     * @param v the vehicle to add
     */
    public void addVehicle(Vehicle v) {
        if (!vehicles.contains(v)) {
            vehicles.add(v);
        }
    }

    /**
     * Returns all vehicles assigned to this garage.
     *
     * @return list of vehicles
     */
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Returns the x-coordinate of the garage.
     *
     * @return x position
     */
    public int getX() { return x; }

    /**
     * Returns the y-coordinate of the garage.
     *
     * @return y position
     */
    public int getY() { return y; }
}