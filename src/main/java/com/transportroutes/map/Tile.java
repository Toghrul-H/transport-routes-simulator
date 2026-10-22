package com.transportroutes.map;

import static com.transportroutes.map.TileType.FOREST;
import static com.transportroutes.map.TileType.INDUSTRY;
import static com.transportroutes.map.TileType.ROAD;
import static com.transportroutes.map.TileType.STOP;
import static com.transportroutes.map.TileType.TRAFFIC_LIGHT;
import com.transportroutes.transport.GoodsType;

/**
 * Represents a single tile on the game map.
 * 
 * <p>A tile can store goods, passengers, and has a specific type
 * such as road, forest, stop, or industry. It also contains logic
 * for production, storage limits, and traffic light behavior.</p>
 */
public class Tile {

    private static final int MAX_GOODS = 50;
    private static final int MAX_PASSENGERS = 50;

    private TileType type;
    private GoodsType goodsType;
    private int goodsAmount;
    private int passengers;

    /**
     * Creates a tile with the specified type.
     *
     * @param type the initial type of the tile
     */
    public Tile(TileType type) {
        this.type = type;
    }

    /**
     * Adds goods to the tile if allowed.
     * 
     * <p>Only one type of goods can be stored at a time,
     * and the amount is capped at a maximum limit.</p>
     *
     * @param type   type of goods
     * @param amount amount to add
     */
    public void addGoods(GoodsType type, int amount) {
        if (amount <= 0 || !canStoreGoods()) return;

        if (goodsType == null) {
            goodsType = type;
        }

        if (goodsType != type) return;

        goodsAmount = Math.min(MAX_GOODS, goodsAmount + amount);
    }

    /** @return type of stored goods */
    public GoodsType getGoodsType() { return goodsType; }

    /** @return amount of stored goods */
    public int getGoodsAmount() { return goodsAmount; }

    /** @return true if tile has goods */
    public boolean hasGoods() {
        return goodsType != null && goodsAmount > 0;
    }

    /**
     * Checks if this tile type can store goods.
     *
     * @return true if goods storage is allowed
     */
    public boolean canStoreGoods() {
        return switch (type) {
            case ROAD, STOP, FOREST, INDUSTRY, TRAFFIC_LIGHT -> true;
            default -> false;
        };
    }

    /**
     * Removes goods from the tile up to a given capacity.
     *
     * @param capacity maximum amount to take
     * @return amount of goods taken
     */
    public int takeGoods(int capacity) {
        if (capacity <= 0) return 0;

        int taken = Math.min(goodsAmount, capacity);
        goodsAmount -= taken;

        if (goodsAmount == 0) {
            goodsType = null;
        }

        return taken;
    }

    /** Clears all stored goods */
    public void clearGoods() {
        goodsType = null;
        goodsAmount = 0;
    }

    /**
     * Produces goods over time.
     * 
     * <p>Only forest and industry tiles generate goods automatically.</p>
     */
    public void produceGoods() {
        if ((type == FOREST || type == INDUSTRY)
                && goodsType != null
                && goodsAmount < MAX_GOODS) {
            goodsAmount++;
        }
    }

    /** @return tile type */
    public TileType getType() { return type; }

    /**
     * Sets the tile type.
     * 
     * <p>If the new type cannot store goods, existing goods are cleared.</p>
     */
    public void setType(TileType type) {
        this.type = type;
        if (!canStoreGoods()) {
            clearGoods();
        }
    }

    /**
     * Adds passengers to the tile.
     *
     * @param amount number of passengers
     */
    public void addPassengers(int amount) {
        if (amount <= 0) return;

        passengers = Math.min(MAX_PASSENGERS, passengers + amount);
    }

    /** @return number of passengers */
    public int getPassengers() { return passengers; }

    /**
     * Removes passengers from the tile.
     *
     * @param capacity maximum passengers to take
     * @return number of passengers taken
     */
    public int takePassengers(int capacity) {
        if (capacity <= 0) return 0;

        int taken = Math.min(passengers, capacity);
        passengers -= taken;
        return taken;
    }

    /** @return true if tile has passengers */
    public boolean hasPassengers() {
        return passengers > 0;
    }

    /**
     * Determines if a traffic light is currently red.
     * 
     * <p>Uses a time-based cycle: 3 seconds red, 3 seconds green.</p>
     *
     * @return true if red, false otherwise
     */
    public boolean isTrafficLightRed() {
        if (type != TileType.TRAFFIC_LIGHT) return false;

        long cycle = 6000;
        long time = System.currentTimeMillis() % cycle;
        return time < 3000;
    }

    /** Clears all passengers from the tile */
    public void clearPassengers() {
        passengers = 0;
    }
}