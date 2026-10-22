package com.transportroutes.ui;

import com.transportroutes.core.Game;
import com.transportroutes.map.*;
import com.transportroutes.transport.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Random;

public class MapPanel extends JPanel implements Scrollable {

    private final Game game;
    private final GameMap map;
    
    private boolean waitingForDestination = false;
    private Vehicle pendingVehicle = null;
    
    private BuildMode currentMode = BuildMode.SELECT;
    private Garage selectedGarage = null;
    
    private static final int TILE_SIZE = 40;
    private Random random = new Random();
    
    private static final Color GRASS_COLOR = new Color(94, 168, 84);
    private static final Color GRASS_DARK = new Color(76, 148, 66);
    private static final Color ROAD_COLOR = new Color(63, 63, 63);
    private static final Color ROAD_EDGE = new Color(245, 245, 245);
    private static final Color ROAD_LINE = new Color(255, 255, 255);
    private static final Color FOREST_COLOR = new Color(34, 139, 34);
    private static final Color FOREST_DARK = new Color(24, 109, 24);
    private static final Color CITY_COLOR = new Color(180, 180, 200);
    private static final Color INDUSTRY_COLOR = new Color(139, 125, 107);
    
    public MapPanel(Game game) {
        this.game = game;
        this.map = game.getMap();
        
        setPreferredSize(new Dimension(
                map.getWidth() * TILE_SIZE,
                map.getHeight() * TILE_SIZE
        ));
        
        setBackground(new Color(20, 20, 25));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                repaint();
            }
        });
        
        final long[] lastVisualUpdate = {System.nanoTime()};
        new Timer(16, e -> {
            long now = System.nanoTime();
            double elapsedSeconds = Math.min(0.05, (now - lastVisualUpdate[0]) / 1_000_000_000.0);
            lastVisualUpdate[0] = now;
            game.updateVehicleVisuals(elapsedSeconds);
            repaint();
        }).start();
    }
    
    public void setBuildMode(BuildMode mode) {
        this.currentMode = mode;
        repaint();
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public void onGameLoaded() {
        waitingForDestination = false;
        pendingVehicle = null;
        selectedGarage = null;
        currentMode = BuildMode.SELECT;
        repaint();
    }
    
    public Garage getSelectedGarage() {
        return selectedGarage;
    }
    
    public void startRouteSelection(Vehicle v) {
        this.waitingForDestination = true;
        this.pendingVehicle = v;
    }
    
    private Component getParentFrame() {
        return SwingUtilities.getWindowAncestor(this);
    }
    
    private void handleClick(MouseEvent e) {
        int x = e.getX() / TILE_SIZE;
        int y = e.getY() / TILE_SIZE;
        if (!isValid(x, y)) return;
        
        if (waitingForDestination && pendingVehicle != null) {
            int startX = pendingVehicle.getTileX();
            int startY = pendingVehicle.getTileY();
            int[] target = findNearestRoad(x, y);
            if (target == null) {
                notify("Invalid Destination",
                    "Click on a road to set destination.", GameNotification.Kind.WARNING);
                return;
            }
            Route route = com.transportroutes.simulation.PathFinder
                    .findLoopRoute(map, startX, startY, target[0], target[1]);
            if (route != null && route.size() > 0) {
                pendingVehicle.setRoute(route);
                notify("Route Set", "Route created successfully.", GameNotification.Kind.SUCCESS);
            } else {
                notify("Path Error",
                    "No valid path found. Make sure roads are connected.",
                    GameNotification.Kind.ERROR);
            }
            waitingForDestination = false;
            pendingVehicle = null;
            return;
        }
        
        Tile tile = map.getTile(x, y);
        switch (currentMode) {
            case SELECT -> {
            }
            case ROAD -> {
                if (tile.getType() == TileType.EMPTY || tile.getType() == TileType.FOREST) {
                    if (!isConnectedToRoad(x, y)) {
                        notify("Road Not Connected",
                            "New roads must connect to an existing road.",
                            GameNotification.Kind.WARNING);
                    } else if (game.spendMoney(50)) {
                        tile.setType(TileType.ROAD);
                        currentMode = BuildMode.SELECT;
                    }
                    else showInsufficientFunds();
                }
            }
            case CITY -> {
                if (tile.getType() == TileType.EMPTY || tile.getType() == TileType.FOREST) {
                    if (game.spendMoney(350)) {
                        tile.setType(TileType.CITY);
                        currentMode = BuildMode.SELECT;
                    }
                    else showInsufficientFunds();
                }
            }
            case FOREST -> {
                if (tile.getType() == TileType.EMPTY) {
                    if (game.spendMoney(180)) {
                        tile.setType(TileType.FOREST);
                        currentMode = BuildMode.SELECT;
                    }
                    else showInsufficientFunds();
                }
            }
            case TRAFFIC_LIGHT -> {
                if (tile.getType() == TileType.ROAD) {
                    if (game.spendMoney(100)) {
                        tile.setType(TileType.TRAFFIC_LIGHT);
                        currentMode = BuildMode.SELECT;
                    }
                    else showInsufficientFunds();
                }
            }
            case STOP -> {
                if (tile.getType() == TileType.ROAD) {
                    if (game.spendMoney(150)) {
                        tile.setType(TileType.STOP);
                        game.addStop(new Stop(x, y));
                        currentMode = BuildMode.SELECT;
                    } else showInsufficientFunds();
                }
            }
            case ROAD_STOP -> {
                if (tile.getType() == TileType.ROAD) {
                    if (game.spendMoney(50)) {
                        tile.setType(TileType.ROAD_STOP);
                        currentMode = BuildMode.SELECT;
                    } else showInsufficientFunds();
                }
            }
            case ADD_WOOD -> {
                tile.addGoods(GoodsType.WOOD, 5);
            }
            case ADD_IRON -> {
                tile.addGoods(GoodsType.IRON, 5);
            }
            case ADD_STEEL -> {
                tile.addGoods(GoodsType.STEEL, 5);
            }
            case GARAGE -> {
                if (tile.getType() == TileType.ROAD && getGarageAt(x, y) == null) {
                    int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                        "Build a new garage here for $500?\nVehicles can be stored and purchased here.",
                        "Build Garage", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION && game.spendMoney(500)) {
                        tile.setType(TileType.GARAGE);
                        Garage garage = new Garage(x, y);
                        game.addGarage(garage);
                        selectedGarage = garage;
                        currentMode = BuildMode.SELECT;
                        notify("Garage Built",
                            "Garage selected. Use Buy Vehicle when you are ready.",
                            GameNotification.Kind.SUCCESS);
                    } else if (confirm == JOptionPane.YES_OPTION) showInsufficientFunds();
                } else {
                    notify("Cannot Build Garage",
                        "Garages must be placed on an empty road tile.",
                        GameNotification.Kind.WARNING);
                }
            }
            case ADD_PASSENGER -> {
                if (isRoad(x, y)) {
                    tile.addPassengers(5);
                    currentMode = BuildMode.SELECT;
                } else {
                    notify("Passengers Need Road",
                        "Passengers can only be added on roads or stops.",
                        GameNotification.Kind.WARNING);
                }
            }
            case BULLDOZE -> {
                if (tile.getType() != TileType.EMPTY) {
                    int confirm = JOptionPane.showConfirmDialog(getParentFrame(),
                        "Remove this structure for $25?", "Bulldoze",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION && game.spendMoney(25)) {
                        removePlacedStructure(x, y, tile);
                        tile.setType(TileType.EMPTY);
                    }
                }
            }
        }
        Garage clickedGarage = getGarageAt(x, y);
        if (clickedGarage != null || currentMode != BuildMode.GARAGE) {
            selectedGarage = clickedGarage;
        }
        repaint();
    }
    
    private void showInsufficientFunds() {
        notify("Insufficient Funds",
            "Not enough money. Earn more by transporting passengers and goods.",
            GameNotification.Kind.WARNING);
    }

    private void removePlacedStructure(int x, int y, Tile tile) {
        if (tile.getType() == TileType.GARAGE) {
            game.removeGarageAt(x, y);
        } else if (tile.getType() == TileType.STOP) {
            game.removeStopAt(x, y);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        drawGrid(g2);
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++)
                drawTile(g2, x, y, map.getTile(x, y));
        
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                if (tile.getType() == TileType.ROAD || tile.getType() == TileType.TRAFFIC_LIGHT ||
                    tile.getType() == TileType.STOP || tile.getType() == TileType.ROAD_STOP
                    || tile.getType() == TileType.GARAGE)
                    drawRoadTile(g2, x, y, tile);
            }
        
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                if (tile.getGoodsAmount() > 0) drawGoods(g2, x, y, tile);
                if (tile.getPassengers() > 0) drawPassengers(g2, x, y, tile);
            }
        
        for (Vehicle v : game.getVehicles()) drawVehicle(g2, v);
        
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            int hoverX = mousePos.x / TILE_SIZE, hoverY = mousePos.y / TILE_SIZE;
            if (isValid(hoverX, hoverY)) drawHover(g2, hoverX, hoverY);
        }
        drawUIOverlay(g2);
    }
    
    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(40, 40, 45));
        for (int x = 0; x <= map.getWidth(); x++)
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, map.getHeight() * TILE_SIZE);
        for (int y = 0; y <= map.getHeight(); y++)
            g2.drawLine(0, y * TILE_SIZE, map.getWidth() * TILE_SIZE, y * TILE_SIZE);
    }
    
    private void drawTile(Graphics2D g2, int x, int y, Tile tile) {
        int px = x * TILE_SIZE, py = y * TILE_SIZE;
        switch (tile.getType()) {
            case EMPTY -> {
                g2.setColor((x + y) % 2 == 0 ? GRASS_COLOR : GRASS_DARK);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g2.setColor(new Color(60, 130, 50, 50));
                for (int i = 0; i < 3; i++) {
                    int gx = px + random.nextInt(TILE_SIZE);
                    int gy = py + random.nextInt(TILE_SIZE);
                    g2.fillRect(gx, gy, 2, 4);
                }
            }
            case FOREST -> {
                g2.setColor((x + y) % 2 == 0 ? GRASS_COLOR : GRASS_DARK);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                drawForest(g2, px, py);
            }
            case CITY -> {
                g2.setColor(CITY_COLOR);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                drawCity(g2, px, py);
            }
            case INDUSTRY -> {
                g2.setColor(INDUSTRY_COLOR);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                drawIndustry(g2, px, py);
            }
            default -> {
                g2.setColor(GRASS_COLOR);
                g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            }
        }
    }
    
    private void drawForest(Graphics2D g2, int px, int py) {
        drawTree(g2, px + 8, py + 8, 24);
        drawTree(g2, px + 24, py + 20, 20);
        drawTree(g2, px + 16, py + 28, 16);
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(px + 8, py + 28, 24, 8);
        g2.fillOval(px + 24, py + 36, 20, 6);
    }
    
    private void drawTree(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(101, 67, 33));
        g2.fillRect(x + size/3, y + size/2, size/4, size/2);
        g2.setColor(FOREST_COLOR);
        g2.fillOval(x, y, size, size/2);
        g2.setColor(FOREST_DARK);
        g2.fillOval(x + size/4, y - size/4, size/2, size/2);
        g2.setColor(new Color(46, 179, 46));
        g2.fillOval(x + size/3, y - size/3, size/3, size/3);
    }
    
    private void drawRoadTile(Graphics2D g2, int x, int y, Tile tile) {
        int px = x * TILE_SIZE, py = y * TILE_SIZE;
        boolean left = isRoad(x-1,y), right = isRoad(x+1,y), up = isRoad(x,y-1), down = isRoad(x,y+1);
        int margin = 8;
        int road = TILE_SIZE - margin * 2;
        Area roadArea = new Area(new RoundRectangle2D.Double(px + margin, py + margin, road, road, 14, 14));
        if (left) roadArea.add(new Area(new Rectangle2D.Double(px, py + margin, TILE_SIZE / 2.0, road)));
        if (right) roadArea.add(new Area(new Rectangle2D.Double(px + TILE_SIZE / 2.0, py + margin, TILE_SIZE / 2.0, road)));
        if (up) roadArea.add(new Area(new Rectangle2D.Double(px + margin, py, road, TILE_SIZE / 2.0)));
        if (down) roadArea.add(new Area(new Rectangle2D.Double(px + margin, py + TILE_SIZE / 2.0, road, TILE_SIZE / 2.0)));

        Stroke oldStroke = g2.getStroke();
        g2.setColor(ROAD_COLOR);
        g2.fill(roadArea);
        g2.setColor(ROAD_EDGE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawRoadEdges(g2, px, py, margin, left, right, up, down);

        g2.setColor(ROAD_LINE);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[]{8, 7}, 0));
        if (left || right) {
            int x1 = left ? px : px + TILE_SIZE / 2;
            int x2 = right ? px + TILE_SIZE : px + TILE_SIZE / 2;
            g2.drawLine(x1, py + TILE_SIZE / 2, x2, py + TILE_SIZE / 2);
        }
        if (up || down) {
            int y1 = up ? py : py + TILE_SIZE / 2;
            int y2 = down ? py + TILE_SIZE : py + TILE_SIZE / 2;
            g2.drawLine(px + TILE_SIZE / 2, y1, px + TILE_SIZE / 2, y2);
        }
        g2.setStroke(oldStroke);
        if (tile.getType() == TileType.TRAFFIC_LIGHT) drawTrafficLight(g2, px, py);
        else if (tile.getType() == TileType.STOP) drawStop(g2, px, py);
        else if (tile.getType() == TileType.ROAD_STOP) drawRoadStop(g2, px, py);
        else if (tile.getType() == TileType.GARAGE) drawGarage(g2, px, py);
    }

    private void drawRoadEdges(Graphics2D g2, int px, int py, int margin,
                               boolean left, boolean right, boolean up, boolean down) {
        int top = py + margin;
        int bottom = py + TILE_SIZE - margin;
        int roadLeft = px + margin;
        int roadRight = px + TILE_SIZE - margin;
        int topStart = left ? px : roadLeft;
        int topEnd = right ? px + TILE_SIZE : roadRight;
        int sideStart = up ? py : top;
        int sideEnd = down ? py + TILE_SIZE : bottom;

        if (!up) g2.drawLine(topStart, top, topEnd, top);
        if (!down) g2.drawLine(topStart, bottom, topEnd, bottom);
        if (!left) g2.drawLine(roadLeft, sideStart, roadLeft, sideEnd);
        if (!right) g2.drawLine(roadRight, sideStart, roadRight, sideEnd);
    }
    
    private boolean isRoad(int x, int y) {
        if (!isValid(x, y)) return false;
        TileType t = map.getTile(x, y).getType();
        return t == TileType.ROAD || t == TileType.TRAFFIC_LIGHT || t == TileType.STOP
                || t == TileType.ROAD_STOP || t == TileType.GARAGE;
    }

    private boolean isConnectedToRoad(int x, int y) {
        return isRoad(x - 1, y) || isRoad(x + 1, y) || isRoad(x, y - 1) || isRoad(x, y + 1);
    }

    private boolean isDriveableRoad(int x, int y) {
        if (!isValid(x, y)) return false;
        TileType t = map.getTile(x, y).getType();
        return t == TileType.ROAD || t == TileType.TRAFFIC_LIGHT || t == TileType.STOP
                || t == TileType.ROAD_STOP || t == TileType.GARAGE;
    }
    
    private void drawTrafficLight(Graphics2D g2, int px, int py) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(px + TILE_SIZE - 8, py + 4, 4, 20);
        g2.fillRect(px + TILE_SIZE - 12, py + 2, 10, 16);
        boolean green = (System.currentTimeMillis() / 2000) % 2 == 0;
        g2.setColor(Color.RED);
        g2.fillOval(px + TILE_SIZE - 10, py + 4, 5, 5);
        g2.setColor(green ? Color.GREEN : Color.RED);
        g2.fillOval(px + TILE_SIZE - 10, py + 11, 5, 5);
    }
    
    private void drawStop(Graphics2D g2, int px, int py) {
        g2.setColor(Color.YELLOW);
        g2.fillRect(px + 4, py + 4, 8, 12);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.drawString("S", px + 6, py + 14);
        g2.setColor(new Color(100, 100, 100, 150));
        g2.fillRect(px + 14, py + 4, 12, 20);
    }

    private void drawRoadStop(Graphics2D g2, int px, int py) {
        g2.setColor(new Color(235, 235, 235));
        g2.fillRect(px + 7, py + TILE_SIZE / 2 - 2, TILE_SIZE - 14, 4);

        g2.setColor(new Color(130, 130, 130));
        g2.fillRect(px + 28, py + 8, 3, 16);

        Polygon sign = new Polygon();
        sign.addPoint(px + 23, py + 6);
        sign.addPoint(px + 29, py + 3);
        sign.addPoint(px + 35, py + 6);
        sign.addPoint(px + 38, py + 12);
        sign.addPoint(px + 35, py + 18);
        sign.addPoint(px + 29, py + 21);
        sign.addPoint(px + 23, py + 18);
        sign.addPoint(px + 20, py + 12);
        g2.setColor(new Color(204, 43, 43));
        g2.fillPolygon(sign);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(sign);
        g2.setFont(new Font("Arial", Font.BOLD, 7));
        g2.drawString("STOP", px + 21, py + 14);
        g2.setStroke(new BasicStroke(1));
    }
    
    private void drawGarage(Graphics2D g2, int px, int py) {
        g2.setColor(new Color(180, 100, 30));
        g2.fillRect(px + 4, py + 8, 24, 20);
        g2.setColor(new Color(150, 50, 20));
        Polygon roof = new Polygon();
        roof.addPoint(px + 2, py + 8); roof.addPoint(px + 30, py + 8); roof.addPoint(px + 16, py);
        g2.fillPolygon(roof);
        g2.setColor(new Color(100, 70, 40));
        g2.fillRect(px + 10, py + 18, 12, 10);
        g2.setColor(Color.YELLOW);
        g2.fillRect(px + 8, py + 2, 16, 6);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 6));
        g2.drawString("GARAGE", px + 9, py + 7);
    }
    
    private void drawCity(Graphics2D g2, int px, int py) {
        g2.setColor(new Color(206, 208, 215));
        g2.fillRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4);

        g2.setColor(new Color(102, 105, 114));
        g2.fillRect(px + 5, py + 10, 8, 20);

        g2.setColor(new Color(196, 198, 205));
        g2.fillRect(px + 15, py + 7, 9, 23);

        g2.setColor(new Color(128, 131, 140));
        g2.fillRect(px + 26, py + 4, 9, 26);

        g2.setColor(new Color(68, 72, 82));
        g2.fillRect(px + 34, py + 4, 2, 26);

        g2.setColor(new Color(242, 217, 77));
        g2.fillRect(px + 6, py + 12, 2, 2);
        g2.fillRect(px + 9, py + 12, 2, 2);
        g2.fillRect(px + 6, py + 16, 2, 2);
        g2.fillRect(px + 9, py + 16, 2, 2);

        g2.setColor(new Color(160, 163, 171));
        g2.drawRect(px + 2, py + 2, TILE_SIZE - 5, TILE_SIZE - 5);
    }
    
    private void drawIndustry(Graphics2D g2, int px, int py) {
        g2.setColor(new Color(100, 100, 100));
        g2.fillRect(px + 6, py + 8, 20, 24);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(px + 20, py + 2, 6, 10);
        g2.setColor(new Color(200, 200, 200, 150));
        g2.fillOval(px + 22, py - 2, 4, 4);
    }
    
    private void drawGoods(Graphics2D g2, int x, int y, Tile tile) {
        int px = x * TILE_SIZE, py = y * TILE_SIZE;
        GoodsType type = tile.getGoodsType();
        if (type == null) return;
        g2.setColor(getGoodsColor(type));
        g2.fillRoundRect(px + 24, py + 24, 12, 12, 3, 3);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(px + 24, py + 24, 12, 12, 3, 3);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.drawString(String.valueOf(tile.getGoodsAmount()), px + 27, py + 34);
    }
    
    private Color getGoodsColor(GoodsType type) {
        return switch (type) {
            case WOOD -> new Color(139, 69, 19);
            case IRON -> new Color(169, 169, 169);
            case STEEL -> new Color(192, 192, 192);
        };
    }
    
    private void drawPassengers(Graphics2D g2, int x, int y, Tile tile) {
        int px = x * TILE_SIZE, py = y * TILE_SIZE;
        int count = Math.min(tile.getPassengers(), 5);
        for (int i = 0; i < count; i++) {
            int offX = (i % 3) * 8, offY = (i / 3) * 8;
            drawPerson(g2, px + 8 + offX, py + 24 + offY);
        }
    }
    
    private void drawPerson(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(255, 220, 180));
        g2.fillOval(x + 2, y, 4, 4);
        g2.setColor(new Color(70, 130, 180));
        g2.fillRect(x + 1, y + 4, 6, 6);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 2, y + 10, 2, 3);
        g2.fillRect(x + 4, y + 10, 2, 3);
    }
    
    private void drawVehicle(Graphics2D g2, Vehicle v) {
        int px = (int)(v.getX() * TILE_SIZE) + TILE_SIZE/2;
        int py = (int)(v.getY() * TILE_SIZE) + TILE_SIZE/2;
        Graphics2D g2d = (Graphics2D) g2.create();
        double laneOffset = v.getLaneOffset() * TILE_SIZE;
        double laneX = -Math.sin(v.getAngle()) * laneOffset;
        double laneY = Math.cos(v.getAngle()) * laneOffset;
        g2d.translate(px + laneX, py + laneY);
        g2d.rotate(v.getAngle());
        g2d.setColor(new Color(0, 0, 0, 50));
        switch (v.getType()) {
            case CAR -> drawCar(g2d);
            case BUS -> drawBus(g2d);
            case CARGO -> drawTruck(g2d, v.getCurrentLoad() > 0);
        }
        g2d.dispose();
    }

    private void drawCar(Graphics2D g2d) {
        g2d.fillRoundRect(-10, -6, 20, 14, 7, 7);
        g2d.setColor(new Color(214, 55, 52));
        g2d.fillRoundRect(-9, -6, 18, 12, 7, 7);
        g2d.setColor(new Color(145, 205, 232));
        g2d.fillRoundRect(-4, -4, 7, 8, 3, 3);
        g2d.setColor(new Color(120, 35, 35));
        g2d.fillRoundRect(-8, -4, 4, 8, 3, 3);
        g2d.setColor(new Color(255, 237, 150));
        g2d.fillOval(7, -4, 3, 3);
        g2d.fillOval(7, 2, 3, 3);
        drawTopDownWheels(g2d, -6, 6);
    }

    private void drawBus(Graphics2D g2d) {
        g2d.fillRoundRect(-15, -7, 30, 16, 5, 5);
        g2d.setColor(new Color(238, 183, 45));
        g2d.fillRoundRect(-14, -7, 28, 14, 4, 4);

        g2d.setColor(new Color(58, 74, 88));
        g2d.fillRoundRect(9, -5, 4, 10, 2, 2);
        g2d.setColor(new Color(154, 218, 238));
        g2d.fillRoundRect(8, -4, 5, 8, 2, 2);

        g2d.setColor(new Color(41, 50, 61));
        g2d.fillRect(3, -6, 2, 12);
        g2d.setColor(new Color(231, 112, 47));
        g2d.fillRect(2, -5, 1, 10);

        g2d.setColor(new Color(132, 199, 226));
        for (int i = -11; i <= -1; i += 5) {
            g2d.fillRoundRect(i, -4, 4, 3, 2, 2);
            g2d.fillRoundRect(i, 1, 4, 3, 2, 2);
        }

        g2d.setColor(new Color(255, 241, 166));
        g2d.fillOval(13, -4, 3, 3);
        g2d.fillOval(13, 1, 3, 3);
        g2d.setColor(new Color(174, 121, 30));
        g2d.drawLine(-13, 0, 6, 0);
        drawTopDownWheels(g2d, -10, 10);
    }

    private void drawTruck(Graphics2D g2d, boolean loaded) {
        g2d.fillRoundRect(-16, -7, 32, 16, 4, 4);

        g2d.setColor(new Color(96, 105, 114));
        g2d.fillRoundRect(-16, -6, 19, 12, 3, 3);
        g2d.setColor(new Color(70, 78, 86));
        g2d.drawRoundRect(-16, -6, 19, 12, 3, 3);

        if (loaded) {
            g2d.setColor(new Color(139, 91, 48));
            g2d.fillRoundRect(-13, -4, 13, 8, 2, 2);
            g2d.setColor(new Color(179, 124, 69));
            g2d.drawLine(-11, -2, -2, -2);
            g2d.drawLine(-11, 1, -2, 1);
        } else {
            g2d.setColor(new Color(128, 139, 150));
            g2d.drawLine(-13, -3, 0, -3);
            g2d.drawLine(-13, 3, 0, 3);
        }

        g2d.setColor(new Color(214, 118, 41));
        g2d.fillRoundRect(3, -7, 13, 14, 4, 4);
        g2d.setColor(new Color(170, 74, 35));
        g2d.fillRect(3, -5, 2, 10);
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRoundRect(8, -4, 5, 8, 2, 2);
        g2d.setColor(new Color(255, 238, 155));
        g2d.fillOval(15, -4, 3, 3);
        g2d.fillOval(15, 1, 3, 3);
        drawTopDownWheels(g2d, -11, 10);
    }

    private void drawTopDownWheels(Graphics2D g2d, int rearX, int frontX) {
        g2d.setColor(new Color(26, 28, 32));
        g2d.fillRoundRect(rearX - 2, -9, 5, 4, 2, 2);
        g2d.fillRoundRect(frontX - 2, -9, 5, 4, 2, 2);
        g2d.fillRoundRect(rearX - 2, 5, 5, 4, 2, 2);
        g2d.fillRoundRect(frontX - 2, 5, 5, 4, 2, 2);
    }

    private void notify(String title, String message, GameNotification.Kind kind) {
        GameNotification.show(this, title, message, kind);
    }
    
    private void drawHover(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        g2.setStroke(new BasicStroke(1));
    }
    
    private void drawUIOverlay(Graphics2D g2) {
        GradientPaint grad = new GradientPaint(0, 0, new Color(0, 0, 0, 180), 0, 60, new Color(0, 0, 0, 100));
        g2.setPaint(grad);
        g2.fillRect(0, 0, getWidth(), 60);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("💰 $" + String.format("%,d", (int)game.getMoney()), 15, 38);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("🚗 " + game.getVehicles().size() + " Vehicles", 200, 38);
        double spd = game.getGameSpeed();
        String spdText = spd == 0 ? "⏸ PAUSED" : spd == 1 ? "▶ 1x" : spd == 2 ? "⏩ 2x" : "⚡ 4x";
        g2.drawString(spdText, 350, 38);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Tool: " + getToolLabel(), 500, 38);
        if (selectedGarage != null) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, getHeight() - 30, getWidth(), 30);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Garage Selected - Click 'Buy Vehicle' to add vehicles", 15, getHeight() - 10);
        }
        if (waitingForDestination) {
            g2.setColor(new Color(0, 100, 200, 200));
            g2.fillRect(0, getHeight() - 60, getWidth(), 30);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("Click on a road to set destination for new vehicle", 15, getHeight() - 40);
        }
    }
    
    private boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight();
    }
    
    private Garage getGarageAt(int x, int y) {
        return game.getGarages().stream()
                .filter(g -> g.getX() == x && g.getY() == y)
                .findFirst().orElse(null);
    }
    
    private int[] findNearestRoad(int x, int y) {
        for (int r = 0; r <= 5; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int nx = x + dx, ny = y + dy;
                    if (isValid(nx, ny) && isDriveableRoad(nx, ny)) return new int[]{nx, ny};
                }
            }
        }
        return null;
    }

    private String getToolLabel() {
        return switch (currentMode) {
            case SELECT -> "Select";
            case ROAD -> "Build roads";
            case CITY -> "Build cities";
            case STOP -> "Placing bus stops";
            case ROAD_STOP -> "Placing stops (1 tile)";
            case GARAGE -> "Build garages";
            case FOREST -> "Plant forests";
            case ADD_PASSENGER -> "Add passengers";
            case TRAFFIC_LIGHT -> "Place traffic lights";
            case ADD_WOOD -> "Add wood";
            case ADD_IRON -> "Add iron";
            case ADD_STEEL -> "Add steel";
            case BULLDOZE -> "Bulldoze";
        };
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(900, 700);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return TILE_SIZE;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.HORIZONTAL
                ? Math.max(TILE_SIZE, visibleRect.width - TILE_SIZE)
                : Math.max(TILE_SIZE, visibleRect.height - TILE_SIZE);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
