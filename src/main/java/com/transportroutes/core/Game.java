package com.transportroutes.core;

import com.transportroutes.map.GameMap;
import com.transportroutes.map.Tile;
import com.transportroutes.map.TileType;
import com.transportroutes.persistence.GameState;
import com.transportroutes.simulation.GameLoop;
import com.transportroutes.transport.Garage;
import com.transportroutes.transport.GoodsType;
import com.transportroutes.transport.Route;
import com.transportroutes.transport.Stop;
import com.transportroutes.transport.Vehicle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Core game class that manages the entire game state and logic.
 * 
 * <p>This class handles the map, vehicles, garages, stops, economy,
 * and the main simulation updates. It also provides functionality
 * for saving and loading the game state.</p>
 */
public class Game {

    private final GameMap map;
    private final List<Garage> garages = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Stop> stops = new ArrayList<>();
    private final GameLoop gameLoop;
    private final Random random = new Random();

    private double money = 10_000;
    private double gameSpeed = 1.0;
    private int totalPassengersTransported = 0;
    private int totalGoodsTransported = 0;
    private double totalRevenue = 0;

    /**
     * Creates a new game instance with a default map and initializes the game loop.
     */
    public Game() {
        this.map = new GameMap(50, 50);
        generateStartingMap();
        this.gameLoop = new GameLoop(this);
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        gameLoop.start();
    }

    /**
     * Generates initial map content.
     */
    private void generateStartingMap() {
        seedStarterRoad();
        seedForests();
    }

    private void seedStarterRoad() {
        int centerX = map.getWidth() / 2;
        int centerY = map.getHeight() / 2;

        for (int x = centerX - 3; x <= centerX + 3; x++) {
            if (isValid(x, centerY)) {
                map.getTile(x, centerY).setType(TileType.ROAD);
            }
        }

        for (int y = centerY - 2; y <= centerY + 2; y++) {
            if (isValid(centerX, y)) {
                map.getTile(centerX, y).setType(TileType.ROAD);
            }
        }
    }

    /**
     * Randomly places forests on the map and initializes them with goods.
     */
    private void seedForests() {
        for (int i = 0; i < 8; i++) {
            int x = random.nextInt(map.getWidth());
            int y = random.nextInt(map.getHeight());

            if (map.getTile(x, y).getType() == TileType.EMPTY) {
                map.getTile(x, y).setType(TileType.FOREST);
                map.getTile(x, y).addGoods(GoodsType.WOOD, 20);
            }
        }
    }

    /**
     * Checks if coordinates are within map bounds.
     */
    private boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight();
    }

    /**
     * Updates the game state.
     * 
     * <p>This includes updating vehicles, producing goods,
     * and spawning passengers.</p>
     */
    public void update() {
        for (Vehicle vehicle : vehicles) {
            vehicle.update(1.0);
        }

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                tile.produceGoods();

                if (tile.getType() == TileType.STOP && random.nextDouble() < 0.25) {
                    tile.addPassengers(1);
                }
            }
        }
    }

    public void updateVehicleVisuals(double elapsedSeconds) {
        for (Vehicle vehicle : vehicles) {
            vehicle.updateVisual(elapsedSeconds, gameSpeed);
        }
    }

    /**
     * Creates a snapshot of the current game state for saving.
     *
     * @return a GameState object containing all game data
     */
    public GameState createGameState() {
        GameState.TileState[][] tileStates = new GameState.TileState[map.getWidth()][map.getHeight()];

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                tileStates[x][y] = new GameState.TileState(
                        tile.getType(),
                        tile.getGoodsType(),
                        tile.getGoodsAmount(),
                        tile.getPassengers());
            }
        }

        List<GameState.GarageState> garageStates = new ArrayList<>();
        for (Garage garage : garages) {
            garageStates.add(new GameState.GarageState(garage.getX(), garage.getY()));
        }

        List<GameState.StopState> stopStates = new ArrayList<>();
        for (Stop stop : stops) {
            stopStates.add(new GameState.StopState(stop.getX(), stop.getY()));
        }

        List<GameState.VehicleState> vehicleStates = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            vehicleStates.add(new GameState.VehicleState(
                    vehicle.getTileX(),
                    vehicle.getTileY(),
                    vehicle.getType(),
                    vehicle.getHomeGarageX(),
                    vehicle.getHomeGarageY(),
                    vehicle.getRouteIndex(),
                    vehicle.getCurrentLoad(),
                    vehicle.getAngle(),
                    vehicle.getMoveCooldown(),
                    vehicle.getCarriedGoodsType(),
                    copyRoutePoints(vehicle.getRoute()),
                    copyRoutePoints(vehicle.getMaintenanceRoute()),
                    vehicle.getMaintenanceRouteIndex(),
                    vehicle.getMaintenanceWaitRemaining(),
                    copyRoutePoints(vehicle.getResumeRouteAfterMaintenance()),
                    vehicle.getResumeRouteIndexAfterMaintenance()));
        }

        return new GameState(
                money,
                gameSpeed,
                totalPassengersTransported,
                totalGoodsTransported,
                totalRevenue,
                map.getWidth(),
                map.getHeight(),
                tileStates,
                garageStates,
                stopStates,
                vehicleStates);
    }

    /**
     * Loads a previously saved game state.
     *
     * @param state the saved game state
     */
    public void loadGameState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("Game state cannot be null.");
        }

        if (state.mapWidth != map.getWidth() || state.mapHeight != map.getHeight()) {
            throw new IllegalArgumentException("Save file map size does not match.");
        }

        money = state.money;
        gameSpeed = state.gameSpeed;
        totalPassengersTransported = state.totalPassengersTransported;
        totalGoodsTransported = state.totalGoodsTransported;
        totalRevenue = state.totalRevenue;

        // Restore tiles
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                GameState.TileState saved = state.tiles[x][y];

                tile.clearGoods();
                tile.clearPassengers();
                tile.setType(saved.type);

                if (saved.goodsType != null && saved.goodsAmount > 0) {
                    tile.addGoods(saved.goodsType, saved.goodsAmount);
                }

                if (saved.passengers > 0) {
                    tile.addPassengers(saved.passengers);
                }
            }
        }

        // Restore garages
        garages.clear();
        for (GameState.GarageState g : state.garages) {
            addGarage(new Garage(g.x, g.y));
        }

        // Restore stops
        stops.clear();
        for (GameState.StopState s : state.stops) {
            addStop(new Stop(s.x, s.y));
        }

        // Restore vehicles
        vehicles.clear();
        for (GameState.VehicleState v : state.vehicles) {
            Vehicle vehicle = new Vehicle(
                    v.x, v.y, v.type, map, this,
                    v.homeGarageX, v.homeGarageY
            );

            vehicle.restoreState(
                    restoreRoute(v.routePoints),
                    v.routeIndex,
                    v.currentLoad,
                    v.angle,
                    v.moveCooldown,
                    v.carriedGoodsType,
                    restoreRoute(v.maintenanceRoutePoints),
                    v.maintenanceRouteIndex,
                    v.maintenanceWaitRemaining,
                    restoreRoute(v.resumeRouteAfterMaintenancePoints),
                    v.resumeRouteIndexAfterMaintenance
            );

            vehicles.add(vehicle);
        }
    }

    /** @return current player money */
    public double getMoney() { return money; }

    /**
     * Attempts to spend money.
     * 
     * @param amount amount to spend
     * @return true if successful, false otherwise
     */
    public boolean spendMoney(double amount) {
        if (money < amount) return false;
        money -= amount;
        return true;
    }

    /** @return true when the player has no money left */
    public boolean isGameOver() {
        return money <= 0;
    }

    /**
     * Adds money to the player and updates total revenue.
     */
    public void addMoney(double amount) {
        money += amount;
        totalRevenue += amount;
    }

    /** Sets game speed multiplier */
    public void setGameSpeed(double speed) { this.gameSpeed = speed; }

    /** @return current game speed */
    public double getGameSpeed() { return gameSpeed; }

    /** @return game map */
    public GameMap getMap() { return map; }

    /** @return list of vehicles */
    public List<Vehicle> getVehicles() { return vehicles; }

    /** @return list of garages */
    public List<Garage> getGarages() { return garages; }

    /** @return list of stops */
    public List<Stop> getStops() { return stops; }

    /** Adds a vehicle to the game */
    public void addVehicle(Vehicle vehicle) { vehicles.add(vehicle); }

    /** Adds or replaces a garage */
    public void addGarage(Garage garage) {
        garages.removeIf(g -> g.getX() == garage.getX() && g.getY() == garage.getY());
        garages.add(garage);
    }

    /** Removes garage at given position */
    public void removeGarageAt(int x, int y) {
        garages.removeIf(g -> g.getX() == x && g.getY() == y);
    }

    /** Adds or replaces a stop */
    public void addStop(Stop stop) {
        stops.removeIf(s -> s.getX() == stop.getX() && s.getY() == stop.getY());
        stops.add(stop);
    }

    /** Removes stop at given position */
    public void removeStopAt(int x, int y) {
        stops.removeIf(s -> s.getX() == x && s.getY() == y);
    }

    /** Increments passenger count */
    public void addPassengerTransported() { totalPassengersTransported++; }

    /** Adds transported goods */
    public void addGoodsTransported(int amount) { totalGoodsTransported += amount; }

    /** Rebuilds a route from stored points */
    private Route restoreRoute(List<int[]> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) return null;

        Route route = new Route();
        for (int[] p : routePoints) {
            route.addPoint(p[0], p[1]);
        }
        return route;
    }

    /** Converts a route to a list of coordinates for saving */
    private List<int[]> copyRoutePoints(Route route) {
        List<int[]> list = new ArrayList<>();
        if (route == null) return list;

        for (int[] p : route.getPoints()) {
            list.add(new int[]{p[0], p[1]});
        }
        return list;
    }

    /** Finds a garage at given position */
    private Garage getGarageAt(int x, int y) {
        for (Garage g : garages) {
            if (g.getX() == x && g.getY() == y) return g;
        }
        return null;
    }
}
