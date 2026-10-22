package com.transportroutes.simulation;

import com.transportroutes.core.Game;
import javax.swing.Timer;

/**
 * Controls the main simulation loop of the game.
 * 
 * <p>This class uses a Swing Timer to repeatedly update the game state
 * at a fixed interval. It also supports variable game speed and pausing.</p>
 */
public class GameLoop {

    private final Game game;
    private final Timer timer;

    /**
     * Creates a new GameLoop linked to the given game instance.
     * 
     * <p>The timer triggers updates every 50 milliseconds.</p>
     *
     * @param game the game instance to update
     */
    public GameLoop(Game game) {
        this.game = game;
        this.timer = new Timer(50, event -> update());
    }

    /**
     * Starts the simulation loop.
     */
    public void start() {
        timer.start();
    }

    /**
     * Updates the game based on the current speed.
     * 
     * <p>If the speed is zero, the game is paused. Otherwise,
     * the game update is executed multiple times depending on
     * the speed multiplier.</p>
     */
    public void update() {
        double speed = game.getGameSpeed();

        // Pause if speed is 0
        if (speed == 0) {
            return;
        }

        // Run multiple updates based on speed
        int steps = Math.max(1, (int) speed);
        for (int i = 0; i < steps; i++) {
            game.update();
        }
    }
}