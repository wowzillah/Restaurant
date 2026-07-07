package model;

import java.io.Serial;
import java.io.Serializable;

public class OrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private MenuItem menuItem;
    private int quantity;
    private String specialInstruction;

    public OrderItem(MenuItem menuItem, int quantity, String specialInstruction) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstruction = specialInstruction;
    }

    public double getSubtotal(){
        return menuItem.getPrice() * quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSpecialInstruction(String specialInstruction) {
        this.specialInstruction = specialInstruction;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSpecialInstruction() {
        return specialInstruction;
    }
}
