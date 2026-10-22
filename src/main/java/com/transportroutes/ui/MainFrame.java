package com.transportroutes.ui;

import com.transportroutes.core.Game;
import com.transportroutes.persistence.GameState;
import com.transportroutes.persistence.SaveGameManager;
import com.transportroutes.transport.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainFrame extends JFrame {
    
    private static final Color DARK_BG = new Color(22, 25, 31);
    private static final Color SIDEBAR_BOTTOM = new Color(15, 18, 24);
    private static final Color ACCENT_COLOR = new Color(86, 150, 222);
    private static final Color CARD_TOP = new Color(58, 65, 78);
    private static final Color CARD_BOTTOM = new Color(39, 44, 54);
    private static final Color CARD_BORDER = new Color(96, 137, 193);
    private static final Color TEXT_COLOR = new Color(237, 241, 248);
    private static final Color MUTED_TEXT = new Color(176, 185, 201);
    private static final Color SUCCESS_COLOR = new Color(112, 232, 139);
    private static final Color WARNING_COLOR = new Color(255, 168, 91);
    private static final int MAINTENANCE_RETURN_COST = 250;
    private static final DateTimeFormatter SAVE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
    
    private JLabel moneyLabel;
    private JLabel vehicleLabel;
    private JLabel speedLabel;
    
    public MainFrame(Game game) {
        setTitle("🚂 Transport Tycoon");
        setTitle("Transport Tycoon");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        MapPanel mapPanel = new MapPanel(game);
        JScrollPane mapScrollPane = createMapScrollPane(mapPanel);
        JScrollPane sidebarScrollPane = createSidebarScrollPane(createSidebar(game, mapPanel, mapScrollPane));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(sidebarScrollPane);
        splitPane.setRightComponent(mapScrollPane);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(3);
        
        add(splitPane);
        startStatsUpdater(game);
        SwingUtilities.invokeLater(mapPanel::requestFocusInWindow);
    }

    private JScrollPane createSidebarScrollPane(JPanel sidebar) {
        JScrollPane scrollPane = new JScrollPane(sidebar);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(DARK_BG);
        return scrollPane;
    }
    
    private JScrollPane createMapScrollPane(MapPanel mapPanel) {
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(mapPanel.getTileSize());
        scrollPane.getVerticalScrollBar().setUnitIncrement(mapPanel.getTileSize());
        registerMapNavigation(mapPanel, scrollPane);
        return scrollPane;
    }
    
    private JPanel createSidebar(Game game, MapPanel mapPanel, JScrollPane mapScrollPane) {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, DARK_BG, 0, getHeight(), SIDEBAR_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        
        JLabel titleLabel = new JLabel("🚂 TRANSPORT TYCOON");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(createHeaderPanel());
        sidebar.add(Box.createVerticalStrut(18));
        
        sidebar.add(createStatsPanel(game));
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(createSectionLabel("MINIMAP"));
        MiniMapPanel miniMapPanel = new MiniMapPanel(game, mapPanel, mapScrollPane);
        miniMapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(miniMapPanel);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createMiniMapHintLabel());
        sidebar.add(Box.createVerticalStrut(20));
        
        sidebar.add(createSectionLabel("BUILD"));
        sidebar.add(createButton("Select",
            () -> mapPanel.setBuildMode(BuildMode.SELECT)));
        sidebar.add(createButton("Build Road ($50)", 
            () -> mapPanel.setBuildMode(BuildMode.ROAD)));
        sidebar.add(createButton("Build City ($350)",
            () -> mapPanel.setBuildMode(BuildMode.CITY)));
        sidebar.add(createButton("Plant Forest ($180)", 
            () -> mapPanel.setBuildMode(BuildMode.FOREST)));
        sidebar.add(createButton("Traffic Light ($100)", 
            () -> mapPanel.setBuildMode(BuildMode.TRAFFIC_LIGHT)));
        sidebar.add(createButton("Add Stop ($50)",
            () -> mapPanel.setBuildMode(BuildMode.ROAD_STOP)));
        sidebar.add(createButton("Bus Stop ($150)", 
            () -> mapPanel.setBuildMode(BuildMode.STOP)));
        sidebar.add(createButton("Build Garage ($500)", 
            () -> mapPanel.setBuildMode(BuildMode.GARAGE)));
        sidebar.add(createButton("Bulldoze ($25)", 
            () -> mapPanel.setBuildMode(BuildMode.BULLDOZE)));
        
        sidebar.add(Box.createVerticalStrut(15));
        
        sidebar.add(createSectionLabel("VEHICLES"));
        sidebar.add(createButton("Buy Vehicle", 
            () -> buyVehicle(game, mapPanel)));
        sidebar.add(createButton("Route Editor",
            () -> editVehicleRoute(game, mapPanel)));
        sidebar.add(createButton("Service Vehicle ($250)",
            () -> sendVehicleForMaintenance(game, mapPanel)));
        
        sidebar.add(Box.createVerticalStrut(15));
        
        sidebar.add(createSectionLabel("ADD GOODS"));
        sidebar.add(createButton("Add Wood (Free)", 
            () -> mapPanel.setBuildMode(BuildMode.ADD_WOOD)));
        sidebar.add(createButton("Add Iron (Free)", 
            () -> mapPanel.setBuildMode(BuildMode.ADD_IRON)));
        sidebar.add(createButton("Add Steel (Free)", 
            () -> mapPanel.setBuildMode(BuildMode.ADD_STEEL)));
        
        sidebar.add(Box.createVerticalStrut(15));
        
        sidebar.add(createSectionLabel("POPULATION"));
        sidebar.add(createButton("Add Passengers", 
            () -> mapPanel.setBuildMode(BuildMode.ADD_PASSENGER)));
        
        sidebar.add(Box.createVerticalStrut(15));
        
        sidebar.add(createSectionLabel("TIME"));
        sidebar.add(createButton("Pause", () -> game.setGameSpeed(0)));
        sidebar.add(createButton("Speed x1", () -> game.setGameSpeed(1)));
        sidebar.add(createButton("Speed x2", () -> game.setGameSpeed(2)));
        sidebar.add(createButton("Speed x4", () -> game.setGameSpeed(4)));
        
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSectionLabel("SYSTEM"));
        sidebar.add(createButton("Save Game", () -> saveGame(game)));
        sidebar.add(createButton("Load Game", () -> loadGame(game, mapPanel)));
        sidebar.add(createButton("Quit Game", this::quitGame));

        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(createSectionLabel("HELP"));
        sidebar.add(createButton("Open Help", this::showHelp));
        
        return sidebar;
    }
    
    private JPanel createStatsPanel(Game game) {
        JPanel panel = createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        moneyLabel = new JLabel("$" + String.format("%,d", (int)game.getMoney()));
        moneyLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        moneyLabel.setForeground(SUCCESS_COLOR);
        moneyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        vehicleLabel = new JLabel(game.getVehicles().size() + " vehicles");
        vehicleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        vehicleLabel.setForeground(MUTED_TEXT);
        vehicleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        speedLabel = new JLabel("Speed: 1x");
        speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        speedLabel.setForeground(MUTED_TEXT);
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(moneyLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(vehicleLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(speedLabel);
        
        return panel;
    }
    
    private void startStatsUpdater(Game game) {
        Timer timer = new Timer(100, e -> {
            moneyLabel.setText("$" + String.format("%,d", (int)game.getMoney()));
            moneyLabel.setForeground(game.getMoney() > 1000 ? SUCCESS_COLOR : WARNING_COLOR);
            vehicleLabel.setText(game.getVehicles().size() + " vehicles");
            
            double speed = game.getGameSpeed();
            String speedText = speed == 0 ? "Paused" : 
                              speed == 1 ? "x1" :
                              speed == 2 ? "x2" : "x4";
            speedLabel.setText("Speed: " + speedText);
        });
        timer.start();
    }
    
    private void buyVehicle(Game game, MapPanel mapPanel) {
        if (mapPanel.getSelectedGarage() == null) {
            notify("No Garage Selected",
                "Please select a garage first.\nClick on a garage to select it.",
                GameNotification.Kind.WARNING);
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Select Vehicle Type:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel);
        
        ButtonGroup group = new ButtonGroup();
        JRadioButton carButton = createVehicleRadio("🚗 Car", 
            "Speed: Fast | Capacity: 4 | Price: $200", true);
        JRadioButton busButton = createVehicleRadio("🚌 Bus", 
            "Speed: Medium | Capacity: 20 | Price: $500", false);
        JRadioButton cargoButton = createVehicleRadio("📦 Cargo Truck", 
            "Speed: Slow | Capacity: 30 | Price: $800", false);
        
        group.add(carButton);
        group.add(busButton);
        group.add(cargoButton);
        
        panel.add(carButton);
        panel.add(busButton);
        panel.add(cargoButton);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Purchase Vehicle", JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            VehicleType type = null;
            if (carButton.isSelected()) type = VehicleType.CAR;
            else if (busButton.isSelected()) type = VehicleType.BUS;
            else if (cargoButton.isSelected()) type = VehicleType.CARGO;
            
            if (type != null) {
                if (!game.spendMoney(type.price)) {
                    notify("Insufficient Funds",
                        "Not enough money. Need $" + (int) type.price + ".",
                        GameNotification.Kind.ERROR);
                    return;
                }
                
                int gx = mapPanel.getSelectedGarage().getX();
                int gy = mapPanel.getSelectedGarage().getY();
                
                Vehicle v = new Vehicle(gx, gy, type, game.getMap(), game);
                game.addVehicle(v);
                mapPanel.getSelectedGarage().addVehicle(v);
                
                notify("Vehicle Purchased",
                    "Use Route Editor to connect this vehicle to a stop.",
                    GameNotification.Kind.SUCCESS);
            }
        }
    }

    private void sendVehicleForMaintenance(Game game, MapPanel mapPanel) {
        Garage garage = mapPanel.getSelectedGarage();
        if (garage == null) {
            notify("No Garage Selected",
                "Please select a garage first.\nClick on a garage to manage its vehicles.",
                GameNotification.Kind.WARNING);
            return;
        }

        List<Vehicle> availableVehicles = garage.getVehicles().stream()
                .filter(game.getVehicles()::contains)
                .toList();

        if (availableVehicles.isEmpty()) {
            notify("No Vehicles",
                "This garage has no vehicles to send for maintenance.",
                GameNotification.Kind.INFO);
            return;
        }

        String[] vehicleOptions = new String[availableVehicles.size()];
        for (int i = 0; i < availableVehicles.size(); i++) {
            vehicleOptions[i] = formatVehicleOption(availableVehicles.get(i), i + 1);
        }

        String selectedVehicleOption = (String) JOptionPane.showInputDialog(
            this,
            "Select a vehicle to return to its garage for maintenance:",
            "Vehicle Maintenance",
            JOptionPane.PLAIN_MESSAGE,
            null,
            vehicleOptions,
            vehicleOptions[0]
        );

        if (selectedVehicleOption == null) {
            return;
        }

        int selectedIndex = -1;
        for (int i = 0; i < vehicleOptions.length; i++) {
            if (vehicleOptions[i].equals(selectedVehicleOption)) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex < 0) {
            return;
        }

        if (game.getMoney() < MAINTENANCE_RETURN_COST) {
            notify("Insufficient Funds",
                "Not enough money. Need $" + MAINTENANCE_RETURN_COST + ".",
                GameNotification.Kind.ERROR);
            return;
        }

        Vehicle vehicle = availableVehicles.get(selectedIndex);
        String error = vehicle.sendToMaintenance();
        if (error != null) {
            notify("Maintenance Unavailable", error, GameNotification.Kind.WARNING);
            return;
        }

        game.spendMoney(MAINTENANCE_RETURN_COST);
        mapPanel.repaint();

        notify("Maintenance Started",
            "The vehicle is returning to its garage for maintenance.",
            GameNotification.Kind.INFO);
    }
    
    private JRadioButton createVehicleRadio(String name, String details, boolean selected) {
        JRadioButton radio = new JRadioButton("<html><b>" + name + "</b><br>" + 
                                               "<font size='2'>" + details + "</font></html>");
        radio.setSelected(selected);
        radio.setFont(new Font("Arial", Font.PLAIN, 12));
        return radio;
    }

    private String formatVehicleOption(Vehicle vehicle, int number) {
        String status = vehicle.isInMaintenance() || vehicle.getMaintenanceWaitRemaining() > 0
                ? " | In maintenance"
                : "";
        return number + ". " + vehicle.getType()
                + " at (" + (int) vehicle.getX() + ", " + (int) vehicle.getY() + ")" + status;
    }

    private JLabel createMiniMapHintLabel() {
        JLabel label = new JLabel("Click or drag to navigate");
        label.setForeground(MUTED_TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void registerMapNavigation(MapPanel mapPanel, JScrollPane mapScrollPane) {
        mapPanel.setFocusable(true);

        int step = mapPanel.getTileSize() * 2;
        bindScrollAction(mapPanel, mapScrollPane, "LEFT", "scroll-left", -step, 0);
        bindScrollAction(mapPanel, mapScrollPane, "RIGHT", "scroll-right", step, 0);
        bindScrollAction(mapPanel, mapScrollPane, "UP", "scroll-up", 0, -step);
        bindScrollAction(mapPanel, mapScrollPane, "DOWN", "scroll-down", 0, step);
        bindScrollAction(mapPanel, mapScrollPane, "A", "scroll-left-a", -step, 0);
        bindScrollAction(mapPanel, mapScrollPane, "D", "scroll-right-d", step, 0);
        bindScrollAction(mapPanel, mapScrollPane, "W", "scroll-up-w", 0, -step);
        bindScrollAction(mapPanel, mapScrollPane, "S", "scroll-down-s", 0, step);
    }

    private void bindScrollAction(MapPanel mapPanel, JScrollPane mapScrollPane,
                                  String keyStroke, String actionName, int dx, int dy) {
        InputMap inputMap = mapPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mapPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyStroke), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                scrollMapBy(mapScrollPane, dx, dy);
            }
        });
    }

    private void scrollMapBy(JScrollPane mapScrollPane, int dx, int dy) {
        JViewport viewport = mapScrollPane.getViewport();
        Point viewPosition = viewport.getViewPosition();
        Dimension viewSize = viewport.getExtentSize();
        Dimension mapSize = viewport.getViewSize();

        int maxX = Math.max(0, mapSize.width - viewSize.width);
        int maxY = Math.max(0, mapSize.height - viewSize.height);

        int nextX = Math.max(0, Math.min(maxX, viewPosition.x + dx));
        int nextY = Math.max(0, Math.min(maxY, viewPosition.y + dy));
        viewport.setViewPosition(new Point(nextX, nextY));
    }

    private void saveGame(Game game) {
        String saveName = JOptionPane.showInputDialog(
            this,
            "Enter a name for this save file:",
            createDefaultSaveName()
        );

        if (saveName == null) {
            return;
        }

        saveName = saveName.trim();
        if (saveName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Save name cannot be empty.",
                "Invalid Save Name",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path savePath = SaveGameManager.getSavePath(saveName);
            if (Files.exists(savePath)) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                    "Overwrite existing save '" + SaveGameManager.toDisplayName(savePath) + "'?",
                    "Overwrite Save",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            SaveGameManager.save(game.createGameState(), saveName);
            JOptionPane.showMessageDialog(this,
                "Game saved to " + savePath.getFileName() + ".",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save the game:\n" + ex.getMessage(),
                "Save Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGame(Game game, MapPanel mapPanel) {
        try {
            List<Path> saveFiles = SaveGameManager.listSaveFiles();
            if (saveFiles.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No save files found yet.",
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] saveOptions = saveFiles.stream()
                    .map(SaveGameManager::toDisplayName)
                    .toArray(String[]::new);

            String selectedSave = (String) JOptionPane.showInputDialog(
                this,
                "Select a save file to load:",
                "Load Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                saveOptions,
                saveOptions[0]
            );

            if (selectedSave == null) {
                return;
            }

            GameState loadedState = SaveGameManager.load(SaveGameManager.getSavePath(selectedSave));
            game.loadGameState(loadedState);
            mapPanel.onGameLoaded();

            JOptionPane.showMessageDialog(this,
                "Loaded " + selectedSave + ".",
                "Load Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ClassNotFoundException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load the game:\n" + ex.getMessage(),
                "Load Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitGame() {
        int result = JOptionPane.showConfirmDialog(this,
            "Quit the game?",
            "Quit Game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private String createDefaultSaveName() {
        return "save-" + LocalDateTime.now().format(SAVE_NAME_FORMATTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrowLabel = new JLabel("ROUTE CONTROL");
        eyebrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrowLabel.setForeground(MUTED_TEXT);
        eyebrowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Transport Tycoon");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html>Build clean networks, move cargo, and keep the city flowing.</html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(eyebrowLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitleLabel);
        return panel;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, 22, 22);
                g2.setPaint(new GradientPaint(0, 0, CARD_TOP, 0, getHeight(), CARD_BOTTOM));
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 22, 22);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }
    
    private void showHelp() {
        String helpText = """
            🚂 TRANSPORT TYCOON HELP
            
            🏗 BUILDING:
            • Roads: Connect cities and industries ($50)
            • Cities: Place urban blocks on empty land ($350)
            • Forests: Source of wood goods ($180)
            • Traffic Lights: Control traffic flow ($100)
            • Bus Stops: Pickup/dropoff points ($150)
            • Garages: Buy and store vehicles ($500)
            
            🚗 VEHICLES:
            • Cars: Fast, 4 passengers, $200
            • Buses: Medium speed, 20 passengers, $500
            • Cargo Trucks: Slow, 30 goods capacity, $800
            
            📦 GOODS:
            • Wood: Free to place, cargo earns $70
            • Iron: Free to place, cargo earns $120
            • Steel: Free to place, cargo earns $90

            SAVE SYSTEM:
            • Save Game: Stores the full current map and vehicle state
            • Load Game: Restores any previously saved file
            • Multiple Saves: Use different save names to keep multiple files
            
            💡 TIPS:
            1. Build roads connecting forests to cities
            2. Set up bus routes between populated areas
            3. Use traffic lights at busy intersections
            4. Watch your money - build strategically!
            """;
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(45, 45, 50));
        textArea.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JButton createButton(String text, Runnable action) {
        JButton btn = new SidebarButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setPreferredSize(new Dimension(190, 38));
        btn.setMaximumSize(new Dimension(190, 38));
        btn.setMinimumSize(new Dimension(190, 38));
        
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void editVehicleRoute(Game game, MapPanel mapPanel) {
        List<Vehicle> vehicles = game.getVehicles();
        if (vehicles.isEmpty()) {
            notify("No Vehicles", "Buy a vehicle before editing routes.", GameNotification.Kind.WARNING);
            return;
        }

        List<int[]> stops = getRouteStops(game);
        if (stops.isEmpty()) {
            notify("No Stops", "Build at least one bus stop before editing routes.", GameNotification.Kind.WARNING);
            return;
        }

        String[] vehicleOptions = new String[vehicles.size()];
        for (int i = 0; i < vehicles.size(); i++) {
            vehicleOptions[i] = formatVehicleOption(vehicles.get(i), i + 1);
        }

        String selectedVehicleOption = (String) JOptionPane.showInputDialog(
            this,
            "Choose the vehicle to route:",
            "Route Editor",
            JOptionPane.PLAIN_MESSAGE,
            null,
            vehicleOptions,
            vehicleOptions[0]
        );

        if (selectedVehicleOption == null) {
            return;
        }

        int vehicleIndex = findSelectedIndex(vehicleOptions, selectedVehicleOption);
        if (vehicleIndex < 0) {
            return;
        }

        String[] stopOptions = new String[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            int[] stop = stops.get(i);
            stopOptions[i] = (i + 1) + ". Stop at (" + stop[0] + ", " + stop[1] + ")";
        }

        String selectedStopOption = (String) JOptionPane.showInputDialog(
            this,
            "Choose destination stop:",
            "Route Editor",
            JOptionPane.PLAIN_MESSAGE,
            null,
            stopOptions,
            stopOptions[0]
        );

        if (selectedStopOption == null) {
            return;
        }

        int stopIndex = findSelectedIndex(stopOptions, selectedStopOption);
        if (stopIndex < 0) {
            return;
        }

        Vehicle vehicle = vehicles.get(vehicleIndex);
        int[] stop = stops.get(stopIndex);
        Route route = com.transportroutes.simulation.PathFinder.findLoopRoute(
                game.getMap(),
                vehicle.getTileX(),
                vehicle.getTileY(),
                stop[0],
                stop[1]
        );

        if (route == null || route.size() == 0) {
            notify("Route Unavailable",
                "No connected road path reaches that stop.",
                GameNotification.Kind.ERROR);
            return;
        }

        vehicle.setRoute(route);
        mapPanel.repaint();
        notify("Route Updated",
            vehicle.getType() + " is now routed to stop (" + stop[0] + ", " + stop[1] + ").",
            GameNotification.Kind.SUCCESS);
    }

    private List<int[]> getRouteStops(Game game) {
        List<int[]> stops = new ArrayList<>();
        for (int x = 0; x < game.getMap().getWidth(); x++) {
            for (int y = 0; y < game.getMap().getHeight(); y++) {
                var type = game.getMap().getTile(x, y).getType();
                if (type == com.transportroutes.map.TileType.STOP
                        || type == com.transportroutes.map.TileType.ROAD_STOP) {
                    stops.add(new int[]{x, y});
                }
            }
        }
        return stops;
    }

    private int findSelectedIndex(String[] options, String selectedOption) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedOption)) {
                return i;
            }
        }
        return -1;
    }

    private void notify(String title, String message, GameNotification.Kind kind) {
        GameNotification.show(this, title, message, kind);
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static final class SidebarButton extends JButton {

        private SidebarButton(String text) {
            super(text);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(TEXT_COLOR);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setIconTextGap(8);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ButtonModel model = getModel();
            Color top = model.isPressed() ? new Color(66, 105, 160)
                    : model.isRollover() ? new Color(90, 132, 191)
                    : new Color(72, 78, 92);
            Color bottom = model.isPressed() ? new Color(47, 77, 124)
                    : model.isRollover() ? new Color(64, 96, 145)
                    : new Color(50, 55, 66);
            Color border = model.isRollover() ? new Color(139, 177, 233) : CARD_BORDER;

            g2.setColor(new Color(0, 0, 0, 65));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, 18, 18);
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 18, 18);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 18, 18);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
