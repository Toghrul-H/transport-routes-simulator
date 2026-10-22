package com.transportroutes.transport;

import com.transportroutes.core.Game;
import com.transportroutes.map.GameMap;
import com.transportroutes.map.TileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void testVehicleCreation() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(
                5, 5,
                VehicleType.BUS,
                map,
                game
        );

        assertNotNull(v);
        assertEquals(5, v.getX());
        assertEquals(5, v.getY());
        assertEquals(VehicleType.BUS, v.getType());
    }

    @Test
    void testSetRoute() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        Route route = new Route();
        route.addPoint(1, 0);
        route.addPoint(2, 0);

        v.setRoute(route);

        assertTrue(v.hasRoute());
        assertEquals(0, v.getRouteIndex());
    }

    @Test
    void testHasRouteFalseInitially() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        assertFalse(v.hasRoute());
    }

    @Test
    void testRestoreStateBasic() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        Route route = new Route();
        route.addPoint(1, 1);
        route.addPoint(2, 2);

        v.restoreState(route, 1, 5, 0.5, 1.0, null);

        assertEquals(1, v.getRouteIndex());
        assertEquals(5, v.getCurrentLoad());
        assertEquals(1.0, v.getMoveCooldown());
    }

    @Test
    void testRestoreStateLimitsLoad() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        Route route = new Route();
        route.addPoint(1, 1);

        // put very large load (should be capped by capacity)
        v.restoreState(route, 0, 9999, 0, 0, null);

        assertTrue(v.getCurrentLoad() <= VehicleType.BUS.capacity);
    }

    @Test
    void testHomeGarageCoordinates() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(3, 4, VehicleType.BUS, map, game, 3, 4);

        assertEquals(3, v.getHomeGarageX());
        assertEquals(4, v.getHomeGarageY());
    }

    @Test
    void testSendToMaintenanceNoCrash() {
        Game game = new Game();
        GameMap map = game.getMap();

        Vehicle v = new Vehicle(0, 0, VehicleType.BUS, map, game);

        String result = v.sendToMaintenance();

        // either null or message is acceptable
        assertNotNull(result == null ? "" : result);
    }

    @Test
    void testCarPicksUpPassengersWhenStopIsAheadOnRoute() {
        Game game = new Game();
        GameMap map = game.getMap();
        map.getTile(0, 0).setType(TileType.ROAD);
        map.getTile(1, 0).setType(TileType.ROAD);
        map.getTile(2, 0).setType(TileType.STOP);
        map.getTile(1, 0).addPassengers(6);

        Vehicle v = new Vehicle(0, 0, VehicleType.CAR, map, game);
        Route route = new Route();
        route.addPoint(1, 0);
        route.addPoint(2, 0);
        v.setRoute(route);

        v.update(1.0);

        assertEquals(VehicleType.CAR.capacity, v.getCurrentLoad());
        assertEquals(2, map.getTile(1, 0).getPassengers());
    }

    @Test
    void testCarPicksUpPassengersFromStartingStopWhenStopIsAheadOnRoute() {
        Game game = new Game();
        GameMap map = game.getMap();
        map.getTile(0, 0).setType(TileType.STOP);
        map.getTile(1, 0).setType(TileType.ROAD);
        map.getTile(2, 0).setType(TileType.STOP);
        map.getTile(0, 0).addPassengers(6);

        Vehicle v = new Vehicle(0, 0, VehicleType.CAR, map, game);
        Route route = new Route();
        route.addPoint(1, 0);
        route.addPoint(2, 0);
        v.setRoute(route);

        assertEquals(VehicleType.CAR.capacity, v.getCurrentLoad());
        assertEquals(2, map.getTile(0, 0).getPassengers());
    }

    @Test
    void testPassengersLeaveVehicleAtStopWithoutRejoiningQueue() {
        Game game = new Game();
        GameMap map = game.getMap();
        map.getTile(0, 0).setType(TileType.ROAD);
        map.getTile(1, 0).setType(TileType.ROAD);
        map.getTile(2, 0).setType(TileType.STOP);
        map.getTile(1, 0).addPassengers(6);

        Vehicle v = new Vehicle(0, 0, VehicleType.CAR, map, game);
        Route route = new Route();
        route.addPoint(1, 0);
        route.addPoint(2, 0);
        v.setRoute(route);

        v.update(1.0);
        v.updateVisual(1.0, 1.0);
        v.update(1.0);

        assertEquals(0, v.getCurrentLoad());
        assertEquals(0, map.getTile(2, 0).getPassengers());
    }

    @Test
    void testCarDoesNotPickUpPassengersWithoutStopAheadOnRoute() {
        Game game = new Game();
        GameMap map = game.getMap();
        map.getTile(0, 0).setType(TileType.ROAD);
        map.getTile(1, 0).setType(TileType.ROAD);
        map.getTile(1, 0).addPassengers(6);

        Vehicle v = new Vehicle(0, 0, VehicleType.CAR, map, game);
        Route route = new Route();
        route.addPoint(1, 0);
        v.setRoute(route);

        v.update(1.0);

        assertEquals(0, v.getCurrentLoad());
        assertEquals(6, map.getTile(1, 0).getPassengers());
    }
}
