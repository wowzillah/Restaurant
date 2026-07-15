package service;

import model.Ingredient;
import model.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryService {
    public final List<Ingredient> inventory;

    public InventoryService() {
        this.inventory = new ArrayList<>();
    }

    //Basic CRUD Operations

    public void addIngredient(Ingredient ingredient) {
        this.inventory.add(ingredient);
    }

    public List<Ingredient> getInventory() {
        return inventory;
    }

    public Optional<Ingredient> getIngredientById(int id) {
        return inventory.stream()
                .filter(ingredient -> ingredient.getId() == id)
                .findFirst();
    }

    // --- The Core Business Logic ---

    /**
     * Loops through a dish's recipe to verify we have enough of EVERY ingredient.
     */
    public boolean canCook(MenuItem item) {
        for (Map.Entry<Ingredient, Integer> entry : item.getRecipe().entrySet()) {
            Ingredient ingredient = entry.getKey();
            int requiredQty = entry.getValue();

            // If even one ingredient is short, we cannot cook the dish!
            if (ingredient.getStockQuantity() < requiredQty) {
                return false;
            }
        }
        return true; // All clear!
    }

    /**
     * Physically removes the required ingredients from our virtual stock room.
     * Call this ONLY after checking canCook() is true!
     */
    public void deductStockFor(MenuItem item) {
        for (java.util.Map.Entry<Ingredient, Integer> entry : item.getRecipe().entrySet()) {
            Ingredient ingredient = entry.getKey();
            int requiredQty = entry.getValue();
            ingredient.deductStock(requiredQty);
        }
    }

    public boolean isStockLowFor(MenuItem item) {
        for(Ingredient ingredient : item.getRecipe().keySet()) {
            if(ingredient.isLowStock()){
                return true;
            }
        }
        return false;
    }


    //setter for loading from persistence Later
    public void setInventory(List<Ingredient> loadedInventory) {
        this.inventory.clear();
        this.inventory.addAll(loadedInventory);
    }

}
