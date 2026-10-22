package com.transportroutes.transport;


public enum VehicleType {

    CAR(1.2, 4, 5, 200),
    BUS(0.8, 20, 10, 500),
    CARGO(0.6, 30, 15, 800);

    public final double speed;
    public final int capacity;
    public final double maintenance;
    public final double price;

    VehicleType(double speed, int capacity, double maintenance, double price) {
        this.speed = speed;
        this.capacity = capacity;
        this.maintenance = maintenance;
        this.price = price;
    }

    public boolean isCargo() {
        return this == CARGO;
    }
}