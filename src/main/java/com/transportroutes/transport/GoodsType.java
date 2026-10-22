package com.transportroutes.transport;

public enum GoodsType {

    WOOD(70),
    STEEL(90),
    IRON(120);

    public final double price;

    GoodsType(double price) {
        this.price = price;
    }
}