package com.transportroutes.map;

import java.util.Random;

/**
 * Represents the game map as a 2D grid of tiles.
 * 
 * <p>The map is initialized with a given width and height, and each tile
 * is randomly assigned a type (forest or empty).</p>
 */
public class GameMap {

    private Tile[][] tiles;
    private int width;
    private int height;

    private Random rand = new Random();

    /**
     * Creates a new game map with the specified dimensions.
     * 
     * <p>Each tile is randomly initialized with a 40% chance of being a forest,
     * otherwise it is set as empty.</p>
     *
     * @param width  the width of the map
     * @param height the height of the map
     */
    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;

        tiles = new Tile[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                // 40% chance of forest
                if (rand.nextDouble() < 0.4) {
                    tiles[x][y] = new Tile(TileType.FOREST);
                } else {
                    tiles[x][y] = new Tile(TileType.EMPTY);
                }
            }
        }
    }

    /**
     * Returns the tile at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the tile at the given position
     */
    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    /**
     * Returns the width of the map.
     *
     * @return map width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the map.
     *
     * @return map height
     */
    public int getHeight() {
        return height;
    }
}