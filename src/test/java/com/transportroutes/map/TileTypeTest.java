package com.transportroutes.map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileTypeTest {

    @Test
    void testEnumValuesExist() {
        assertNotNull(TileType.EMPTY);
        assertNotNull(TileType.ROAD);
        assertNotNull(TileType.CITY);
        assertNotNull(TileType.FOREST);
        assertNotNull(TileType.INDUSTRY);
        assertNotNull(TileType.STOP);
        assertNotNull(TileType.ROAD_STOP);
        assertNotNull(TileType.GARAGE);
        assertNotNull(TileType.TRAFFIC_LIGHT);
    }

    @Test
    void testEnumValueOf() {
        TileType type = TileType.valueOf("ROAD");

        assertEquals(TileType.ROAD, type);
    }

    @Test
    void testEnumValuesArray() {
        TileType[] values = TileType.values();

        assertTrue(values.length >= 9);
    }
}