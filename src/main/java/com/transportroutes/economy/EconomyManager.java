package com.transportroutes.economy;

/**
 * Manages the economic system of the game.
 * 
 * <p>This class is responsible for storing and updating the player's money.
 * It provides simple methods for adding, subtracting, and retrieving the current balance.</p>
 */
public class EconomyManager {

    private double money;

    /**
     * Creates a new EconomyManager with a default starting balance.
     */
    public EconomyManager() {
        this.money = 100_000;
    }

    /**
     * Adds money to the player's balance.
     *
     * @param amount the amount to add
     */
    public void addMoney(double amount) {
        money += amount;
    }

    /**
     * Subtracts money from the player's balance.
     *
     * @param amount the amount to subtract
     */
    public void subtractMoney(double amount) {
        money -= amount;
    }

    /**
     * Returns the current amount of money.
     *
     * @return the player's balance
     */
    public double getMoney() {
        return money;
    }
}