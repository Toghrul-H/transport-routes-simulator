package com.transportroutes.persistence;

import com.transportroutes.map.TileType;
import com.transportroutes.transport.GoodsType;
import com.transportroutes.transport.VehicleType;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a complete snapshot of the game state.
 * 
 * <p>This class is used for saving and loading the game. It stores
 * all necessary data such as map tiles, vehicles, garages, stops,
 * and player statistics in a serializable format.</p>
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    public final double money;
    public final double gameSpeed;
    public final int totalPassengersTransported;
    public final int totalGoodsTransported;
    public final double totalRevenue;
    public final int mapWidth;
    public final int mapHeight;
    public final TileState[][] tiles;
    public final List<GarageState> garages;
    public final List<StopState> stops;
    public final List<VehicleState> vehicles;

    /**
     * Creates a new GameState containing all game data.
     */
    public GameState(
            double money,
            double gameSpeed,
            int totalPassengersTransported,
            int totalGoodsTransported,
            double totalRevenue,
            int mapWidth,
            int mapHeight,
            TileState[][] tiles,
            List<GarageState> garages,
            List<StopState> stops,
            List<VehicleState> vehicles) {

        this.money = money;
        this.gameSpeed = gameSpeed;
        this.totalPassengersTransported = totalPassengersTransported;
        this.totalGoodsTransported = totalGoodsTransported;
        this.totalRevenue = totalRevenue;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tiles = tiles;
        this.garages = garages;
        this.stops = stops;
        this.vehicles = vehicles;
    }

    /**
     * Represents the saved state of a single tile.
     */
    public static final class TileState implements Serializable {

        private static final long serialVersionUID = 1L;

        public final TileType type;
        public final GoodsType goodsType;
        public final int goodsAmount;
        public final int passengers;

        /**
         * Creates a tile state snapshot.
         */
        public TileState(TileType type, GoodsType goodsType, int goodsAmount, int passengers) {
            this.type = type;
            this.goodsType = goodsType;
            this.goodsAmount = goodsAmount;
            this.passengers = passengers;
        }
    }

    /**
     * Represents the saved state of a garage.
     */
    public static final class GarageState implements Serializable {

        private static final long serialVersionUID = 1L;

        public final int x;
        public final int y;

        /**
         * Creates a garage state with its position.
         */
        public GarageState(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Represents the saved state of a stop.
     */
    public static final class StopState implements Serializable {

        private static final long serialVersionUID = 1L;

        public final int x;
        public final int y;

        /**
         * Creates a stop state with its position.
         */
        public StopState(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Represents the saved state of a vehicle.
     * 
     * <p>Includes position, route, cargo, movement data,
     * and maintenance-related information.</p>
     */
    public static final class VehicleState implements Serializable {

        private static final long serialVersionUID = 1L;

        public final int x;
        public final int y;
        public final VehicleType type;
        public final int homeGarageX;
        public final int homeGarageY;
        public final int routeIndex;
        public final int currentLoad;
        public final double angle;
        public final double moveCooldown;
        public final GoodsType carriedGoodsType;
        public final List<int[]> routePoints;
        public final List<int[]> maintenanceRoutePoints;
        public final int maintenanceRouteIndex;
        public final double maintenanceWaitRemaining;
        public final List<int[]> resumeRouteAfterMaintenancePoints;
        public final int resumeRouteIndexAfterMaintenance;

        /**
         * Creates a vehicle state snapshot.
         */
        public VehicleState(
                int x,
                int y,
                VehicleType type,
                int homeGarageX,
                int homeGarageY,
                int routeIndex,
                int currentLoad,
                double angle,
                double moveCooldown,
                GoodsType carriedGoodsType,
                List<int[]> routePoints,
                List<int[]> maintenanceRoutePoints,
                int maintenanceRouteIndex,
                double maintenanceWaitRemaining,
                List<int[]> resumeRouteAfterMaintenancePoints,
                int resumeRouteIndexAfterMaintenance) {

            this.x = x;
            this.y = y;
            this.type = type;
            this.homeGarageX = homeGarageX;
            this.homeGarageY = homeGarageY;
            this.routeIndex = routeIndex;
            this.currentLoad = currentLoad;
            this.angle = angle;
            this.moveCooldown = moveCooldown;
            this.carriedGoodsType = carriedGoodsType;
            this.routePoints = routePoints;
            this.maintenanceRoutePoints = maintenanceRoutePoints;
            this.maintenanceRouteIndex = maintenanceRouteIndex;
            this.maintenanceWaitRemaining = maintenanceWaitRemaining;
            this.resumeRouteAfterMaintenancePoints = resumeRouteAfterMaintenancePoints;
            this.resumeRouteIndexAfterMaintenance = resumeRouteIndexAfterMaintenance;
        }
    }
}