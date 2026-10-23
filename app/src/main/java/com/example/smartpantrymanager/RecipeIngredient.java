package com.example.smartpantrymanager;

/** One ingredient that a recipe needs. */
public class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    /** e.g. "3 pcs eggs" or "0.5 kg rice" */
    public String getDisplayText() {
        String amount = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        return amount + " " + unit + " " + name;
    }
}