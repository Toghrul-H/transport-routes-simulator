package com.transportroutes.transport;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void testInitialRouteIsEmpty() {
        Route route = new Route();

        assertEquals(0, route.size());
        assertTrue(route.getPoints().isEmpty());
    }

    @Test
    void testAddPointIncreasesSize() {
        Route route = new Route();

        route.addPoint(1, 2);

        assertEquals(1, route.size());
    }

    @Test
    void testAddMultiplePoints() {
        Route route = new Route();

        route.addPoint(1, 2);
        route.addPoint(3, 4);
        route.addPoint(5, 6);

        assertEquals(3, route.size());
    }

    @Test
    void testGetReturnsCorrectPoint() {
        Route route = new Route();

        route.addPoint(7, 8);

        int[] point = route.get(0);

        assertArrayEquals(new int[]{7, 8}, point);
    }

    @Test
    void testOrderIsMaintained() {
        Route route = new Route();

        route.addPoint(1, 1);
        route.addPoint(2, 2);
        route.addPoint(3, 3);

        assertArrayEquals(new int[]{1, 1}, route.get(0));
        assertArrayEquals(new int[]{2, 2}, route.get(1));
        assertArrayEquals(new int[]{3, 3}, route.get(2));
    }

    @Test
    void testGetPointsReturnsList() {
        Route route = new Route();

        route.addPoint(9, 10);

        List<int[]> points = route.getPoints();

        assertNotNull(points);
        assertEquals(1, points.size());
    }

    @Test
    void testGetInvalidIndexThrowsException() {
        Route route = new Route();

        assertThrows(IndexOutOfBoundsException.class, () -> {
            route.get(0);
        });
    }
}