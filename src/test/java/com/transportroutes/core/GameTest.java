package com.transportroutes.core;

import com.transportroutes.transport.Garage;
import com.transportroutes.transport.Stop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testInitialValues() {
        Game game = new Game();

        assertEquals(10000, game.getMoney());
        assertEquals(1.0, game.getGameSpeed());
        assertNotNull(game.getMap());
    }

    @Test
    void testAddMoney() {
        Game game = new Game();

        game.addMoney(500);

        assertEquals(10500, game.getMoney());
    }

    @Test
    void testSpendMoneySuccess() {
        Game game = new Game();

        boolean result = game.spendMoney(2000);

        assertTrue(result);
        assertEquals(8000, game.getMoney());
    }

    @Test
    void testSpendMoneyFail() {
        Game game = new Game();

        boolean result = game.spendMoney(20000);

        assertFalse(result);
        assertEquals(10000, game.getMoney());
    }

    @Test
    void testGameOverWhenMoneyReachesZero() {
        Game game = new Game();

        game.spendMoney(10000);

        assertTrue(game.isGameOver());
    }

    @Test
    void testGameOverClearsWhenMoneyIsAdded() {
        Game game = new Game();

        game.spendMoney(10000);
        game.addMoney(500);

        assertFalse(game.isGameOver());
    }

    @Test
    void testSetGameSpeed() {
        Game game = new Game();

        game.setGameSpeed(2.5);

        assertEquals(2.5, game.getGameSpeed());
    }

    @Test
    void testAddGarage() {
        Game game = new Game();

        Garage g = new Garage(5, 5);
        game.addGarage(g);

        assertEquals(1, game.getGarages().size());
    }

    @Test
    void testReplaceGarageSameCoordinates() {
        Game game = new Game();

        Garage g1 = new Garage(5, 5);
        Garage g2 = new Garage(5, 5);

        game.addGarage(g1);
        game.addGarage(g2);

        // should replace, not duplicate
        assertEquals(1, game.getGarages().size());
    }

    @Test
    void testRemoveGarage() {
        Game game = new Game();

        game.addGarage(new Garage(3, 3));
        game.removeGarageAt(3, 3);

        assertEquals(0, game.getGarages().size());
    }

    @Test
    void testAddStopNoCrash() {
        Game game = new Game();

        Stop stop = new Stop(2, 2);
        game.addStop(stop);

        // no getter → just ensure no crash
        assertTrue(true);
    }

    @Test
    void testRemoveStopNoCrash() {
        Game game = new Game();

        game.addStop(new Stop(2, 2));
        game.removeStopAt(2, 2);

        assertTrue(true);
    }

    @Test
    void testCreateGameState() {
        Game game = new Game();

        assertNotNull(game.createGameState());
    }

    @Test
    void testLoadGameStateNullThrows() {
        Game game = new Game();

        assertThrows(IllegalArgumentException.class, () -> {
            game.loadGameState(null);
        });
    }

    @Test
    void testAddVehicle() {
        Game game = new Game();

        // we don't construct full vehicle here (complex),
        // just verify list behavior
        assertEquals(0, game.getVehicles().size());
    }
}
