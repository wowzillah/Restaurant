package model;

import model.enums.MenuCategory;
import model.enums.MenuStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MenuItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String description;
    private double price;
    private MenuCategory category;
    private boolean available;
    private MenuStatus status;

    private Map<Ingredient, Integer> recipe;

    public MenuItem(int id, String name, String description, double price, MenuStatus status, MenuCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.available = true;

        this.recipe = new HashMap<>();
    }

    public void addIngredientToRecipe(Ingredient ingredient, int quantityRequired) {
        recipe.put(ingredient, quantityRequired);
    }

    public Map<Ingredient, Integer> getRecipe() {
        return recipe;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public MenuCategory getCategory() {
        return category;
    }

    public void setCategory(MenuCategory category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public MenuStatus getStatus() {
        return status;
    }

    public void setStatus(MenuStatus status) {
        this.status = status;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
