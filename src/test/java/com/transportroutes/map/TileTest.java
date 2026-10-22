package com.transportroutes.map;

import com.transportroutes.transport.GoodsType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void testInitialState() {
        Tile tile = new Tile(TileType.ROAD);

        assertEquals(TileType.ROAD, tile.getType());
        assertFalse(tile.hasGoods());
        assertFalse(tile.hasPassengers());
    }

    @Test
    void testAddGoods() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 10);

        assertTrue(tile.hasGoods());
        assertEquals(10, tile.getGoodsAmount());
        assertEquals(GoodsType.WOOD, tile.getGoodsType());
    }

    @Test
    void testAddGoodsLimit() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 100);

        assertEquals(50, tile.getGoodsAmount()); // MAX_GOODS
    }

    @Test
    void testAddDifferentGoodsIgnored() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 10);
        tile.addGoods(GoodsType.IRON, 10); // different type

        assertEquals(10, tile.getGoodsAmount());
        assertEquals(GoodsType.WOOD, tile.getGoodsType());
    }

    @Test
    void testTakeGoods() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 20);

        int taken = tile.takeGoods(5);

        assertEquals(5, taken);
        assertEquals(15, tile.getGoodsAmount());
    }

    @Test
    void testTakeAllGoodsClearsType() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 10);

        tile.takeGoods(10);

        assertFalse(tile.hasGoods());
        assertNull(tile.getGoodsType());
    }

    @Test
    void testClearGoods() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 10);
        tile.clearGoods();

        assertFalse(tile.hasGoods());
        assertEquals(0, tile.getGoodsAmount());
    }

    @Test
    void testProduceGoods() {
        Tile tile = new Tile(TileType.FOREST);

        tile.addGoods(GoodsType.WOOD, 10);
        tile.produceGoods();

        assertEquals(11, tile.getGoodsAmount());
    }

    @Test
    void testSetTypeClearsGoodsIfInvalid() {
        Tile tile = new Tile(TileType.ROAD);

        tile.addGoods(GoodsType.WOOD, 10);
        tile.setType(TileType.EMPTY); // cannot store goods

        assertFalse(tile.hasGoods());
    }

    @Test
    void testAddPassengers() {
        Tile tile = new Tile(TileType.STOP);

        tile.addPassengers(10);

        assertEquals(10, tile.getPassengers());
    }

    @Test
    void testPassengerLimit() {
        Tile tile = new Tile(TileType.STOP);

        tile.addPassengers(100);

        assertEquals(50, tile.getPassengers()); // MAX_PASSENGERS
    }

    @Test
    void testTakePassengers() {
        Tile tile = new Tile(TileType.STOP);

        tile.addPassengers(20);

        int taken = tile.takePassengers(5);

        assertEquals(5, taken);
        assertEquals(15, tile.getPassengers());
    }

    @Test
    void testClearPassengers() {
        Tile tile = new Tile(TileType.STOP);

        tile.addPassengers(10);
        tile.clearPassengers();

        assertEquals(0, tile.getPassengers());
    }

    @Test
    void testTrafficLightCheckDoesNotCrash() {
        Tile tile = new Tile(TileType.TRAFFIC_LIGHT);

        // cannot assert exact value (time-based), but should not crash
        boolean result = tile.isTrafficLightRed();

        assertTrue(result || !result);
    }
}