package com.transportroutes.ui;

import com.transportroutes.core.Game;
import com.transportroutes.map.GameMap;
import com.transportroutes.map.Tile;
import com.transportroutes.map.TileType;
import com.transportroutes.transport.Vehicle;
import static com.transportroutes.transport.VehicleType.BUS;
import static com.transportroutes.transport.VehicleType.CAR;
import static com.transportroutes.transport.VehicleType.CARGO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class MiniMapPanel extends JPanel {

    private static final int PANEL_SIZE = 210;
    private static final int PADDING = 8;

    private final Game game;
    private final MapPanel mapPanel;
    private final JScrollPane mapScrollPane;

    public MiniMapPanel(Game game, MapPanel mapPanel, JScrollPane mapScrollPane) {
        this.game = game;
        this.mapPanel = mapPanel;
        this.mapScrollPane = mapScrollPane;

        Dimension size = new Dimension(PANEL_SIZE, PANEL_SIZE);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setBackground(new Color(45, 45, 50));
        setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter navigator = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                navigateTo(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                navigateTo(e.getPoint());
            }
        };

        addMouseListener(navigator);
        addMouseMotionListener(navigator);

        mapScrollPane.getViewport().addChangeListener(e -> repaint());
        new Timer(100, e -> repaint()).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle bounds = getMiniMapBounds();
        drawTiles(g2, bounds);
        drawVehicles(g2, bounds);
        drawViewport(g2, bounds);

        g2.dispose();
    }

    private void drawTiles(Graphics2D g2, Rectangle bounds) {
        GameMap map = game.getMap();
        double tileWidth = bounds.getWidth() / map.getWidth();
        double tileHeight = bounds.getHeight() / map.getHeight();

        g2.setColor(new Color(30, 30, 35));
        g2.fillRoundRect(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4, 8, 8);

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                g2.setColor(getTileColor(tile));

                int drawX = bounds.x + (int) Math.floor(x * tileWidth);
                int drawY = bounds.y + (int) Math.floor(y * tileHeight);
                int drawWidth = Math.max(1, (int) Math.ceil((x + 1) * tileWidth) - (int) Math.floor(x * tileWidth));
                int drawHeight = Math.max(1, (int) Math.ceil((y + 1) * tileHeight) - (int) Math.floor(y * tileHeight));
                g2.fillRect(drawX, drawY, drawWidth, drawHeight);
            }
        }

        g2.setColor(new Color(140, 170, 210));
        g2.drawRoundRect(bounds.x - 1, bounds.y - 1, bounds.width + 1, bounds.height + 1, 8, 8);
    }

    private void drawVehicles(Graphics2D g2, Rectangle bounds) {
        GameMap map = game.getMap();
        double tileWidth = bounds.getWidth() / map.getWidth();
        double tileHeight = bounds.getHeight() / map.getHeight();

        for (Vehicle vehicle : game.getVehicles()) {
            int centerX = bounds.x + (int) Math.round((vehicle.getX() + 0.5) * tileWidth);
            int centerY = bounds.y + (int) Math.round((vehicle.getY() + 0.5) * tileHeight);
            int size = Math.max(3, Math.min(6, (int) Math.round(Math.min(tileWidth, tileHeight))));

            g2.setColor(switch (vehicle.getType()) {
                case CAR -> new Color(220, 70, 70);
                case BUS -> new Color(255, 215, 0);
                case CARGO -> new Color(255, 140, 0);
            });
            g2.fillOval(centerX - size / 2, centerY - size / 2, size, size);
            g2.setColor(Color.BLACK);
            g2.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        }
    }

    private void drawViewport(Graphics2D g2, Rectangle bounds) {
        Dimension fullMapSize = mapPanel.getPreferredSize();
        Rectangle viewRect = mapScrollPane.getViewport().getViewRect();

        double scaleX = bounds.getWidth() / fullMapSize.getWidth();
        double scaleY = bounds.getHeight() / fullMapSize.getHeight();

        Rectangle2D viewportRect = new Rectangle2D.Double(
                bounds.x + (viewRect.x * scaleX),
                bounds.y + (viewRect.y * scaleY),
                Math.max(6, viewRect.width * scaleX),
                Math.max(6, viewRect.height * scaleY));

        g2.setColor(new Color(255, 255, 255, 55));
        g2.fill(viewportRect);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(viewportRect);
    }

    private void navigateTo(Point clickPoint) {
        Rectangle bounds = getMiniMapBounds();
        Dimension fullMapSize = mapPanel.getPreferredSize();
        JViewport viewport = mapScrollPane.getViewport();
        Dimension viewSize = viewport.getExtentSize();

        double normalizedX = clamp((clickPoint.x - bounds.x) / (double) bounds.width, 0.0, 1.0);
        double normalizedY = clamp((clickPoint.y - bounds.y) / (double) bounds.height, 0.0, 1.0);

        int targetCenterX = (int) Math.round(normalizedX * fullMapSize.width);
        int targetCenterY = (int) Math.round(normalizedY * fullMapSize.height);

        int maxX = Math.max(0, fullMapSize.width - viewSize.width);
        int maxY = Math.max(0, fullMapSize.height - viewSize.height);
        int viewX = clamp(targetCenterX - viewSize.width / 2, 0, maxX);
        int viewY = clamp(targetCenterY - viewSize.height / 2, 0, maxY);

        viewport.setViewPosition(new Point(viewX, viewY));
        mapPanel.requestFocusInWindow();
    }

    private Rectangle getMiniMapBounds() {
        int availableWidth = Math.max(1, getWidth() - (PADDING * 2));
        int availableHeight = Math.max(1, getHeight() - (PADDING * 2));

        Dimension fullMapSize = mapPanel.getPreferredSize();
        double scale = Math.min(
                availableWidth / (double) fullMapSize.width,
                availableHeight / (double) fullMapSize.height);

        int drawWidth = Math.max(1, (int) Math.round(fullMapSize.width * scale));
        int drawHeight = Math.max(1, (int) Math.round(fullMapSize.height * scale));
        int drawX = (getWidth() - drawWidth) / 2;
        int drawY = (getHeight() - drawHeight) / 2;

        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    private Color getTileColor(Tile tile) {
        TileType type = tile.getType();
        return switch (type) {
            case ROAD -> new Color(85, 85, 90);
            case STOP -> new Color(215, 185, 50);
            case GARAGE -> new Color(170, 95, 40);
            case TRAFFIC_LIGHT -> new Color(190, 70, 70);
            case FOREST -> new Color(34, 139, 34);
            case CITY -> new Color(185, 188, 198);
            case INDUSTRY -> new Color(135, 123, 105);
            default -> new Color(94, 168, 84);
        };
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
