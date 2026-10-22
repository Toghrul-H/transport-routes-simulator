package com.transportroutes.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Deque;

public final class GameNotification {

    public enum Kind {
        INFO(new Color(72, 128, 206), "i"),
        SUCCESS(new Color(71, 166, 95), "!"),
        WARNING(new Color(221, 158, 54), "!"),
        ERROR(new Color(207, 74, 64), "!");

        private final Color color;
        private final String marker;

        Kind(Color color, String marker) {
            this.color = color;
            this.marker = marker;
        }
    }

    private static final int WIDTH = 320;
    private static final int MIN_HEIGHT = 82;
    private static final int GAP = 12;
    private static final int MARGIN = 18;
    private static final int DISPLAY_MS = 4000;
    private static final Deque<JWindow> WINDOWS = new ArrayDeque<>();

    private GameNotification() {
    }

    public static void show(Component parent, String title, String message, Kind kind) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JWindow window = new JWindow(owner);
        JPanel content = createContent(window, title, message, kind);
        window.setContentPane(content);
        window.pack();

        Rectangle bounds = owner == null
                ? GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
                : owner.getBounds();
        int targetX = bounds.x + bounds.width - WIDTH - MARGIN;
        int targetY = bounds.y + MARGIN + WINDOWS.size() * (window.getHeight() + GAP);
        int startX = bounds.x + bounds.width + 8;

        window.setLocation(startX, targetY);
        window.setAlwaysOnTop(false);
        WINDOWS.addLast(window);
        window.setVisible(true);

        animate(window, startX, targetX, targetY, true, () -> {
            Timer hold = new Timer(DISPLAY_MS, event -> dismiss(window));
            hold.setRepeats(false);
            hold.start();
        });
    }

    private static JPanel createContent(JWindow window, String title, String message, Kind kind) {
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 16, 16);
                g2.setColor(new Color(246, 248, 252));
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 16, 16);
                g2.setColor(kind.color);
                g2.fillRoundRect(0, 0, 7, getHeight() - 8, 7, 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 18, 18, 14));
        panel.setPreferredSize(new Dimension(WIDTH, MIN_HEIGHT));

        JLabel icon = new JLabel(kind.marker, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(Color.WHITE);
        icon.setOpaque(true);
        icon.setBackground(kind.color);
        icon.setPreferredSize(new Dimension(30, 30));
        panel.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(28, 31, 36));

        JLabel messageLabel = new JLabel("<html><body style='width:210px'>" + escape(message) + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(58, 64, 74));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(messageLabel);
        panel.add(textPanel, BorderLayout.CENTER);

        JButton close = new JButton("X");
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.setContentAreaFilled(false);
        close.setFont(new Font("Segoe UI", Font.BOLD, 12));
        close.setForeground(new Color(64, 68, 76));
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener((ActionEvent event) -> dismiss(window));
        panel.add(close, BorderLayout.EAST);

        return panel;
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }

    private static void dismiss(JWindow window) {
        if (!window.isVisible()) {
            return;
        }

        int startX = window.getX();
        int endX = startX + WIDTH + 32;
        int y = window.getY();
        animate(window, startX, endX, y, false, () -> {
            WINDOWS.remove(window);
            window.dispose();
            restack();
        });
    }

    private static void animate(JWindow window, int startX, int endX, int y, boolean opening, Runnable done) {
        final int frames = 18;
        final int[] frame = {0};
        Timer timer = new Timer(12, null);
        timer.addActionListener(event -> {
            frame[0]++;
            double t = Math.min(1.0, frame[0] / (double) frames);
            double eased = opening ? easeOutBack(t) : easeIn(t);
            int x = (int) Math.round(startX + (endX - startX) * eased);
            window.setLocation(x, y);
            if (t >= 1.0) {
                timer.stop();
                done.run();
            }
        });
        timer.start();
    }

    private static double easeIn(double t) {
        return t * t;
    }

    private static double easeOutBack(double t) {
        double c1 = 1.2;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    private static void restack() {
        int i = 0;
        for (JWindow window : WINDOWS) {
            Window owner = window.getOwner();
            int y = (owner == null ? 0 : owner.getBounds().y) + MARGIN + i * (window.getHeight() + GAP);
            window.setLocation(window.getX(), y);
            i++;
        }
    }
}
