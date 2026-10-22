package com.transportroutes.transport;

import com.transportroutes.core.Game;
import com.transportroutes.map.GameMap;
import com.transportroutes.map.Tile;
import com.transportroutes.map.TileType;
import com.transportroutes.simulation.PathFinder;
import java.util.List;
import java.util.function.Predicate;

public class Vehicle {

    private static final double MOVE_DELAY = 0.05;
    private static final double BASE_TILES_PER_SECOND = 3.4;
    private static final double TRAFFIC_LIGHT_WAIT = 3.0; // 3 seconds at red light
    private static final double MAINTENANCE_WAIT = 3.0;
    private static final double STOP_WAIT = 1.1;
    private static final int PASSENGER_FARE = 80;

    private final GameMap map;
    private final Game game;
    private final VehicleType type;
    private final double speed;
    private final int homeGarageX;
    private final int homeGarageY;

    private int x;
    private int y;
    private double renderX;
    private double renderY;
    private int routeIndex;
    private int currentLoad;
    private double angle;
    private double renderAngle;
    private double moveCooldown;
    private double stopWaitRemaining;
    private boolean snapTurnAtStop;
    private double stopTurnAngle;
    private int headingX;
    private int headingY;
    private Route route;
    private GoodsType carriedGoodsType;
    private Route maintenanceRoute;
    private int maintenanceRouteIndex;
    private double maintenanceWaitRemaining;
    private Route resumeRouteAfterMaintenance;
    private int resumeRouteIndexAfterMaintenance;

    public Vehicle(double x, double y, VehicleType type, GameMap map, Game game) {
        this((int) x, (int) y, type, map, game, (int) x, (int) y);
    }

    public Vehicle(double x, double y, VehicleType type, GameMap map, Game game, int homeGarageX, int homeGarageY) {
        this.x = (int) x;
        this.y = (int) y;
        this.renderX = this.x;
        this.renderY = this.y;
        this.type = type;
        this.speed = type.speed;
        this.map = map;
        this.game = game;
        this.homeGarageX = homeGarageX;
        this.homeGarageY = homeGarageY;
        this.renderAngle = angle;
    }

    public void update(double speedMultiplier) {
        if (!isValid(x, y) || speedMultiplier <= 0) {
            return;
        }

        if (maintenanceWaitRemaining > 0) {
            maintenanceWaitRemaining = Math.max(0, maintenanceWaitRemaining - (0.05 * speedMultiplier));
            if (maintenanceWaitRemaining == 0) {
                restoreRouteAfterMaintenance();
            }
            return;
        }

        if (!isAtRenderDestination()) {
            return;
        }

        if (snapTurnAtStop) {
            angle = stopTurnAngle;
            renderAngle = angle;
            snapTurnAtStop = false;
        }

        if (stopWaitRemaining > 0) {
            stopWaitRemaining = Math.max(0, stopWaitRemaining - (0.05 * speedMultiplier));
            return;
        }

        if (moveCooldown > 0) {
            moveCooldown = Math.max(0, moveCooldown - (0.05 * speedMultiplier));
            return;
        }

        if (isInMaintenance()) {
            followRoute(maintenanceRoute, false, false, speedMultiplier);
            return;
        }

        if (!hasRoute()) {
            return;
        }

        followRoute(route, true, true, speedMultiplier);
    }

    public void updateVisual(double elapsedSeconds, double speedMultiplier) {
        if (elapsedSeconds <= 0 || speedMultiplier <= 0) {
            return;
        }
        updateRenderPosition(elapsedSeconds, speedMultiplier);
        renderAngle = angle;
    }

    private void handleTileInteraction(Tile tile) {
        if (type.isCargo()) {
            handleCargo(tile);
            return;
        }

        handlePassengers(tile);
    }

    private void handlePassengers(Tile tile) {
        if (currentLoad > 0 && isPassengerStop(tile)) {
            currentLoad = 0;
        }

        if (currentLoad >= type.capacity || !tile.hasPassengers() || !hasPassengerStopAhead()) {
            return;
        }

        int picked = tile.takePassengers(type.capacity - currentLoad);
        if (picked > 0) {
            currentLoad += picked;
            game.addMoney(picked * PASSENGER_FARE);
            for (int i = 0; i < picked; i++) {
                game.addPassengerTransported();
            }
        }
    }

    private void handleCargo(Tile tile) {
        if (currentLoad > 0 && tile.getType() == TileType.STOP) {
            tile.addGoods(carriedGoodsType, currentLoad);
            currentLoad = 0;
            carriedGoodsType = null;
        }

        if (currentLoad >= type.capacity || !tile.hasGoods() || !canPickUpGoods(tile)) {
            return;
        }

        GoodsType tileGoodsType = tile.getGoodsType();
        int picked = tile.takeGoods(type.capacity - currentLoad);

        if (picked > 0) {
            currentLoad += picked;
            carriedGoodsType = tileGoodsType;
            game.addMoney(picked * tileGoodsType.price);
            game.addGoodsTransported(picked);
        }
    }

    private boolean canPickUpGoods(Tile tile) {
        TileType tileType = tile.getType();
        return tileType == TileType.ROAD
                || tileType == TileType.TRAFFIC_LIGHT
                || tileType == TileType.FOREST;
    }

    private boolean canDriveOn(Tile tile) {
        TileType tileType = tile.getType();
        return tileType == TileType.ROAD
                || tileType == TileType.STOP
                || tileType == TileType.ROAD_STOP
                || tileType == TileType.GARAGE
                || tileType == TileType.TRAFFIC_LIGHT;
    }

    private void findAutomaticRoute() {
        int[] destination;

        if (type.isCargo()) {
            if (currentLoad > 0) {
                destination = findNearestReachableTile(tile -> tile.getType() == TileType.STOP);
            } else {
                destination = findNearestReachableTile(tile -> tile.hasGoods() && canPickUpGoods(tile));
                if (destination == null) {
                    destination = findNearestReachableTile(tile -> tile.getType() == TileType.STOP);
                }
            }
        } else {
            destination = findNearestReachableTile(tile -> isPassengerStop(tile) && tile.hasPassengers());
            if (destination == null) {
                destination = findNearestReachableTile(this::isPassengerStop);
            }
        }

        if (destination == null) {
            return;
        }

        Route automaticRoute = PathFinder.findLoopRoute(map, x, y, destination[0], destination[1]);
        if (automaticRoute != null) {
            setRoute(automaticRoute);
        }
    }

    private int[] findNearestReachableTile(Predicate<Tile> matcher) {
        int[] best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int tx = 0; tx < map.getWidth(); tx++) {
            for (int ty = 0; ty < map.getHeight(); ty++) {
                if (tx == x && ty == y) {
                    continue;
                }

                Tile tile = map.getTile(tx, ty);
                if (!matcher.test(tile)) {
                    continue;
                }

                List<int[]> path = PathFinder.findPath(map, x, y, tx, ty);
                if (path.isEmpty() || path.size() >= bestDistance) {
                    continue;
                }

                bestDistance = path.size();
                best = new int[]{tx, ty};
            }
        }

        return best;
    }

    private void advanceRoute() {
        routeIndex++;
        if (routeIndex >= route.size()) {
            routeIndex = 0;
        }
    }

    private boolean isVehicleOnTile(int tx, int ty) {
        for (Vehicle vehicle : game.getVehicles()) {
            if (vehicle != this && vehicle.x == tx && vehicle.y == ty) {
                return true;
            }
        }
        return false;
    }

    private boolean isVehicleBlocking(int tx, int ty, int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return false;
        }

        for (Vehicle vehicle : game.getVehicles()) {
            if (vehicle == this || vehicle.x != tx || vehicle.y != ty) {
                continue;
            }

            if (vehicle.headingX == dx && vehicle.headingY == dy) {
                return true;
            }
        }

        return false;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight();
    }

    public boolean hasRoute() {
        return route != null && route.size() > 0;
    }

    public void setRoute(Route route) {
        this.route = route;
        this.routeIndex = 0;
        this.moveCooldown = 0;
        this.stopWaitRemaining = 0;
        this.snapTurnAtStop = false;
        if (hasRoute() && isAtRenderDestination()) {
            handleTileInteraction(map.getTile(x, y));
        }
    }

    public void restoreState(Route route, int routeIndex, int currentLoad,
                             double angle, double moveCooldown, GoodsType carriedGoodsType) {
        restoreState(route, routeIndex, currentLoad, angle, moveCooldown, carriedGoodsType,
                null, 0, 0, null, 0);
    }

    public void restoreState(Route route, int routeIndex, int currentLoad,
                             double angle, double moveCooldown, GoodsType carriedGoodsType,
                             Route maintenanceRoute, int maintenanceRouteIndex,
                             double maintenanceWaitRemaining, Route resumeRouteAfterMaintenance,
                             int resumeRouteIndexAfterMaintenance) {
        this.route = route != null && route.size() > 0 ? route : null;
        this.routeIndex = this.route == null ? 0 : Math.max(0, Math.min(routeIndex, this.route.size() - 1));
        this.currentLoad = Math.max(0, Math.min(currentLoad, type.capacity));
        this.angle = angle;
        this.renderAngle = angle;
        this.moveCooldown = Math.max(0, moveCooldown);
        this.stopWaitRemaining = 0;
        this.snapTurnAtStop = false;
        this.carriedGoodsType = carriedGoodsType;
        this.maintenanceRoute = maintenanceRoute != null && maintenanceRoute.size() > 0 ? maintenanceRoute : null;
        this.maintenanceRouteIndex = this.maintenanceRoute == null
                ? 0
                : Math.max(0, Math.min(maintenanceRouteIndex, this.maintenanceRoute.size() - 1));
        this.maintenanceWaitRemaining = Math.max(0, maintenanceWaitRemaining);
        this.resumeRouteAfterMaintenance =
                resumeRouteAfterMaintenance != null && resumeRouteAfterMaintenance.size() > 0
                        ? resumeRouteAfterMaintenance
                        : null;
        this.resumeRouteIndexAfterMaintenance = this.resumeRouteAfterMaintenance == null
                ? 0
                : Math.max(0, Math.min(resumeRouteIndexAfterMaintenance, this.resumeRouteAfterMaintenance.size() - 1));
        this.renderX = x;
        this.renderY = y;
    }

    public String sendToMaintenance() {
        if (isInMaintenance() || maintenanceWaitRemaining > 0) {
            return "This vehicle is already returning for maintenance.";
        }

        List<int[]> pathToGarage = PathFinder.findPath(map, x, y, homeGarageX, homeGarageY);
        if ((x != homeGarageX || y != homeGarageY) && pathToGarage.isEmpty()) {
            return "No road path to this vehicle's garage.";
        }

        resumeRouteAfterMaintenance = route;
        resumeRouteIndexAfterMaintenance = route == null ? 0 : Math.min(routeIndex, route.size() - 1);

        if (pathToGarage.isEmpty()) {
            maintenanceRoute = null;
            maintenanceRouteIndex = 0;
            maintenanceWaitRemaining = MAINTENANCE_WAIT;
            moveCooldown = 0;
            return null;
        }

        maintenanceRoute = createRouteFromPath(pathToGarage);
        maintenanceRouteIndex = 0;
        maintenanceWaitRemaining = 0;
        moveCooldown = 0;
        return null;
    }

    public boolean isInMaintenance() {
        return maintenanceRoute != null && maintenanceRoute.size() > 0;
    }

    public double getAngle() {
        return renderAngle;
    }

    public double getX() {
        return renderX;
    }

    public double getY() {
        return renderY;
    }

    public int getTileX() {
        return x;
    }

    public int getTileY() {
        return y;
    }

    public VehicleType getType() {
        return type;
    }

    public Route getRoute() {
        return route;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public double getMoveCooldown() {
        return moveCooldown;
    }

    public double getLaneOffset() {
        if (headingX == 0 && headingY == 0) {
            return 0;
        }
        return 0.18;
    }

    public GoodsType getCarriedGoodsType() {
        return carriedGoodsType;
    }

    public int getHomeGarageX() {
        return homeGarageX;
    }

    public int getHomeGarageY() {
        return homeGarageY;
    }

    public Route getMaintenanceRoute() {
        return maintenanceRoute;
    }

    public int getMaintenanceRouteIndex() {
        return maintenanceRouteIndex;
    }

    public double getMaintenanceWaitRemaining() {
        return maintenanceWaitRemaining;
    }

    public Route getResumeRouteAfterMaintenance() {
        return resumeRouteAfterMaintenance;
    }

    public int getResumeRouteIndexAfterMaintenance() {
        return resumeRouteIndexAfterMaintenance;
    }

    private void followRoute(Route activeRoute, boolean loopAtEnd, boolean allowTileInteraction, double speedMultiplier) {
        if (activeRoute == null || activeRoute.size() == 0) {
            return;
        }

        int currentIndex = loopAtEnd ? routeIndex : maintenanceRouteIndex;
        int[] target = activeRoute.get(currentIndex);
        if (x == target[0] && y == target[1]) {
            boolean hasNext = advanceRoute(activeRoute, loopAtEnd);
            if (!loopAtEnd && !hasNext) {
                beginMaintenanceWait();
                return;
            }
            currentIndex = loopAtEnd ? routeIndex : maintenanceRouteIndex;
            target = activeRoute.get(currentIndex);
        }

        int dx = Integer.compare(target[0], x);
        int dy = Integer.compare(target[1], y);

        if (isVehicleBlocking(target[0], target[1], dx, dy)) {
            moveCooldown = MOVE_DELAY;
            return;
        }

        Tile targetTile = map.getTile(target[0], target[1]);
        if (!canDriveOn(targetTile)) {
            return;
        }

        if (targetTile.getType() == TileType.TRAFFIC_LIGHT && targetTile.isTrafficLightRed()) {
            moveCooldown = TRAFFIC_LIGHT_WAIT;
            return;
        }

        if (dx != 0 && dy != 0) {
            return;
        }

        if (dx != 0 || dy != 0) {
            angle = Math.atan2(dy, dx);
            renderAngle = angle;
            headingX = dx;
            headingY = dy;
        }

        x += dx;
        y += dy;
        moveCooldown = 0;

        if (allowTileInteraction) {
            handleTileInteraction(map.getTile(x, y));
        }

        if (x == target[0] && y == target[1]) {
            boolean hasNext = advanceRoute(activeRoute, loopAtEnd);
            if (!loopAtEnd && !hasNext) {
                beginMaintenanceWait();
            } else if (allowTileInteraction && isTurnaroundTile(targetTile)) {
                faceNextRoutePoint(activeRoute, loopAtEnd);
                if (isStopTile(targetTile)) {
                    stopWaitRemaining = STOP_WAIT;
                }
            }
        }
    }

    private boolean isTurnaroundTile(Tile tile) {
        TileType tileType = tile.getType();
        return tileType == TileType.STOP || tileType == TileType.ROAD_STOP || tileType == TileType.GARAGE;
    }

    private boolean isStopTile(Tile tile) {
        TileType tileType = tile.getType();
        return tileType == TileType.STOP || tileType == TileType.ROAD_STOP;
    }

    private boolean isPassengerStop(Tile tile) {
        return isStopTile(tile);
    }

    private boolean hasPassengerStopAhead() {
        if (!hasRoute()) {
            return false;
        }

        for (int stepsAhead = 1; stepsAhead <= route.size(); stepsAhead++) {
            int index = (routeIndex + stepsAhead) % route.size();
            int[] point = route.get(index);

            if (point[0] == x && point[1] == y) {
                continue;
            }

            if (isPassengerStop(map.getTile(point[0], point[1]))) {
                return true;
            }
        }

        return false;
    }

    private void faceNextRoutePoint(Route activeRoute, boolean loopAtEnd) {
        int currentIndex = loopAtEnd ? routeIndex : maintenanceRouteIndex;
        if (activeRoute == null || activeRoute.size() == 0 || currentIndex >= activeRoute.size()) {
            return;
        }

        int[] next = activeRoute.get(currentIndex);
        int dx = Integer.compare(next[0], x);
        int dy = Integer.compare(next[1], y);
        if (dx == 0 && dy == 0) {
            return;
        }

        stopTurnAngle = Math.atan2(dy, dx);
        headingX = dx;
        headingY = dy;
        snapTurnAtStop = true;
    }

    private void updateRenderPosition(double elapsedSeconds, double speedMultiplier) {
        double dx = x - renderX;
        double dy = y - renderY;
        double distance = Math.hypot(dx, dy);
        if (distance <= 0.0001) {
            renderX = x;
            renderY = y;
            return;
        }

        double step = BASE_TILES_PER_SECOND * speed * speedMultiplier * elapsedSeconds;
        if (step >= distance) {
            renderX = x;
            renderY = y;
        } else {
            renderX += dx / distance * step;
            renderY += dy / distance * step;
        }
    }

    private boolean isAtRenderDestination() {
        return Math.abs(renderX - x) < 0.001 && Math.abs(renderY - y) < 0.001;
    }

    private boolean advanceRoute(Route activeRoute, boolean loopAtEnd) {
        if (loopAtEnd) {
            routeIndex++;
            if (routeIndex >= activeRoute.size()) {
                routeIndex = 0;
            }
            return true;
        }

        maintenanceRouteIndex++;
        return maintenanceRouteIndex < activeRoute.size();
    }

    private void beginMaintenanceWait() {
        maintenanceRoute = null;
        maintenanceRouteIndex = 0;
        maintenanceWaitRemaining = MAINTENANCE_WAIT;
        moveCooldown = 0;
    }

    private void restoreRouteAfterMaintenance() {
        route = resumeRouteAfterMaintenance;
        routeIndex = route == null ? 0 : Math.max(0, Math.min(resumeRouteIndexAfterMaintenance, route.size() - 1));
        resumeRouteAfterMaintenance = null;
        resumeRouteIndexAfterMaintenance = 0;
        if (route != null) {
            faceNextRoutePoint(route, true);
        }
    }

    private Route createRouteFromPath(List<int[]> path) {
        Route createdRoute = new Route();
        for (int[] point : path) {
            createdRoute.addPoint(point[0], point[1]);
        }
        return createdRoute.size() > 0 ? createdRoute : null;
    }
}
