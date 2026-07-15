package model;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private int id;
    private String name;
    private int stockQuantity;
    private int lowStockThreshold;
    private double unitCost;
    private static final long serialVersionUID = 1L;
    public Ingredient(int id, String name, int initialStock, int lowStockThreshold, double unitCost) {


        this.id = id;
        this.name = name;
        this.stockQuantity = initialStock;
        this.lowStockThreshold = lowStockThreshold;
        this.unitCost = unitCost;
    }

    public void deductStock(int amount) {
        this.stockQuantity -= amount;
        if(this.stockQuantity < 0) {
            this.stockQuantity = 0;
        }

    }

    public void addStock(int amount){
        this.stockQuantity += amount;
    }

    public boolean isLowStock() {
        return this.stockQuantity < this.lowStockThreshold;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public double getUnitCost() {
        return unitCost;
    }
}
