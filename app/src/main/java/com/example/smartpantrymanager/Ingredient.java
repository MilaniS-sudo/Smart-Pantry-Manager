package com.example.smartpantrymanager;

/** Model class for one row in the ingredients table. */
public class Ingredient {

    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;

    public Ingredient(int id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    /** Shows 2.0 as "2" but keeps 0.5 as "0.5". */
            public String getQuantityText() {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}