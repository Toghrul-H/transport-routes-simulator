package com.transportroutes.transport;

import com.transportroutes.core.Game;
import com.transportroutes.map.GameMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GarageTest {

    @Test
    void testGarageCreation() {
        Garage garage = new Garage(5, 10);

        assertEquals(5, garage.getX());
        assertEquals(10, garage.getY());
        assertNotNull(garage.getVehicles());
        assertEquals(0, garage.getVehicles().size());
    }

    @Test
    void testAddVehicle() {
        Game game = new Game();
        GameMap map = game.getMap();

        Garage garage = new Garage(0, 0);

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);
        garage.addVehicle(v);

        assertEquals(1, garage.getVehicles().size());
        assertTrue(garage.getVehicles().contains(v));
    }

    @Test
    void testAddSameVehicleTwice() {
        Game game = new Game();
        GameMap map = game.getMap();

        Garage garage = new Garage(0, 0);

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        garage.addVehicle(v);
        garage.addVehicle(v); // try duplicate

        assertEquals(1, garage.getVehicles().size());
    }

    @Test
    void testMultipleVehicles() {
        Game game = new Game();
        GameMap map = game.getMap();

        Garage garage = new Garage(0, 0);

        Vehicle v1 = new Vehicle(0, 0, VehicleType.BUS, map, game);
        Vehicle v2 = new Vehicle(1, 1, VehicleType.BUS, map, game);

        garage.addVehicle(v1);
        garage.addVehicle(v2);

        assertEquals(2, garage.getVehicles().size());
    }
}