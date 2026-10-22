package com.transportroutes.economy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EconomyManagerTest {

    @Test
    void testInitialMoney() {
        EconomyManager eco = new EconomyManager();

        assertEquals(100000, eco.getMoney());
    }

    @Test
    void testAddMoney() {
        EconomyManager eco = new EconomyManager();

        eco.addMoney(5000);

        assertEquals(105000, eco.getMoney());
    }

    @Test
    void testSubtractMoney() {
        EconomyManager eco = new EconomyManager();

        eco.subtractMoney(20000);

        assertEquals(80000, eco.getMoney());
    }

    @Test
    void testSubtractMoreThanAvailable() {
        EconomyManager eco = new EconomyManager();

        eco.subtractMoney(200000);

        assertEquals(-100000, eco.getMoney());
    }

    @Test
    void testAddNegativeMoney() {
        EconomyManager eco = new EconomyManager();

        eco.addMoney(-5000);

        assertEquals(95000, eco.getMoney());
    }
}