package com.transportroutes.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of vehicles in the game.
 * 
 * <p>This class acts as a container for vehicles, allowing them
 * to be added, removed, and accessed.</p>
 */
public class Transport {

    private List<Vehicle> vehicles = new ArrayList<>();

    /**
     * Adds a vehicle to the transport system.
     *
     * @param v the vehicle to add
     */
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    /**
     * Removes a vehicle from the transport system.
     *
     * @param v the vehicle to remove
     */
    public void removeVehicle(Vehicle v) {
        vehicles.remove(v);
    }

    /**
     * Returns all vehicles.
     *
     * @return list of vehicles
     */
    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}