package com.transportroutes;

import com.transportroutes.core.Game;
import com.transportroutes.ui.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point of the Transport Routes application.
 * 
 * <p>This class initializes the core game logic and the graphical user interface.
 * It ensures that all UI components are created and updated on the Swing Event Dispatch Thread.</p>
 */
public class Main {

    /**
     * Main method that starts the application.
     * 
     * <p>It creates the {@code Game} instance, initializes the main window ({@code MainFrame}),
     * starts the game loop, and makes the UI visible.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            /** Core game logic instance */
            Game game = new Game();
            
            /** Main application window */
            MainFrame frame = new MainFrame(game);

            /** Start the game simulation */
            game.start();
            
            /** Display the UI */
            frame.setVisible(true);
        });
    }
}