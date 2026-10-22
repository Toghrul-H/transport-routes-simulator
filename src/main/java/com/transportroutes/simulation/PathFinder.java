package com.transportroutes.simulation;

import com.transportroutes.map.GameMap;
import com.transportroutes.map.TileType;
import com.transportroutes.transport.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Provides pathfinding functionality for vehicles.
 * 
 * <p>This class uses a Breadth-First Search (BFS) algorithm to find
 * the shortest path between two points on the map.</p>
 */
public class PathFinder {

    private static final int[][] DIRS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    /**
     * Finds the shortest path between two points using BFS.
     *
     * @param map the game map
     * @param startX starting x coordinate
     * @param startY starting y coordinate
     * @param endX destination x coordinate
     * @param endY destination y coordinate
     * @return list of points representing the path, or empty list if no path exists
     */
    public static List<int[]> findPath(GameMap map, int startX, int startY, int endX, int endY) {

        if (startX == endX && startY == endY) {
            return new ArrayList<>();
        }

        if (!isValidMove(map, startX, startY) || !isValidMove(map, endX, endY)) {
            return new ArrayList<>();
        }

        boolean[][] visited = new boolean[map.getWidth()][map.getHeight()];
        int[][] parent = new int[map.getWidth()][map.getHeight()];

        for (int i = 0; i < map.getWidth(); i++) {
            Arrays.fill(parent[i], -1);
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            // Destination reached
            if (x == endX && y == endY) {
                return reconstructPath(parent, startX, startY, endX, endY);
            }

            // Explore neighbors
            for (int i = 0; i < DIRS.length; i++) {
                int nx = x + DIRS[i][0];
                int ny = y + DIRS[i][1];

                if (isValidMove(map, nx, ny) && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    parent[nx][ny] = i;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * Creates a loop route (go and return).
     *
     * <p>The vehicle travels to the destination and then returns
     * along the same path.</p>
     *
     * @return a Route object or null if no path exists
     */
    public static Route findLoopRoute(GameMap map, int startX, int startY, int endX, int endY) {
        List<int[]> outbound = findPath(map, startX, startY, endX, endY);
        if (outbound.isEmpty()) {
            return null;
        }

        Route route = new Route();

        // Forward path
        for (int[] step : outbound) {
            route.addPoint(step[0], step[1]);
        }

        // Reverse path (excluding last point to avoid duplication)
        for (int i = outbound.size() - 2; i >= 0; i--) {
            int[] step = outbound.get(i);
            route.addPoint(step[0], step[1]);
        }

        // Return to start
        route.addPoint(startX, startY);
        return route;
    }

    /**
     * Checks if a tile can be used for movement.
     *
     * @return true if tile is drivable
     */
    private static boolean isValidMove(GameMap map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) {
            return false;
        }

        TileType type = map.getTile(x, y).getType();
        return type == TileType.ROAD
                || type == TileType.STOP
                || type == TileType.ROAD_STOP
                || type == TileType.GARAGE
                || type == TileType.TRAFFIC_LIGHT;
    }

    /**
     * Reconstructs the path from BFS parent data.
     *
     * @return ordered list of coordinates from start to end
     */
    private static List<int[]> reconstructPath(int[][] parent,
                                               int startX, int startY,
                                               int endX, int endY) {

        List<int[]> path = new ArrayList<>();
        int x = endX;
        int y = endY;

        while (x != startX || y != startY) {
            path.add(0, new int[]{x, y});

            int direction = parent[x][y];
            if (direction == -1) {
                break;
            }

            x -= DIRS[direction][0];
            y -= DIRS[direction][1];
        }

        return path;
    }
}
