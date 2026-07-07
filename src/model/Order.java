package model;

import model.enums.OrderStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private int tableId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private Employee waiter;

    /**
     * initialize the order
     * @param tableId : the id of the table
     * @param id : the order number
     * We set up the ArrayList to be able to add items to the order
     * at initialization the OrderStatus will always be PENDING
     * createdAt is self-explanatory
     */
    public Order(int tableId, int id, Employee waiter) {
        this.tableId = tableId;
        this.id = id;
        this.waiter = waiter;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(MenuItem item, int quantity, String instructions) {
        // Ensure instructions isn't null to avoid NullPointerExceptions during comparison
        String cleanInstructions = (instructions == null) ? "" : instructions.trim();

        // 1. Search existing items to see if this exact dish + instruction combo already exists
        Optional<OrderItem> existingItem = items.stream()
                .filter(orderItem -> orderItem.getMenuItem().getId() == item.getId())
                .filter(orderItem -> orderItem.getSpecialInstruction().equals(cleanInstructions))
                .findFirst();

        // 2. If found, just increment the quantity! Otherwise, add a new row.
        if (existingItem.isPresent()) {
            OrderItem match = existingItem.get();
            match.setQuantity(match.getQuantity() + quantity);
        } else {
            items.add(new OrderItem(item, quantity, cleanInstructions));
        }
    }

    public double calculateTotal(){
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public int getId() {
        return id;
    }

    public int getTableId() {
        return tableId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Employee getWaiter() {
        return waiter;
    }

    public void setWaiter(Employee waiter) {
        this.waiter = waiter;
    }

    /**
     * Decrements the quantity of an item by 1.
     * If the quantity drops to 0, removes the item line completely.
     */
    public void removeOrDecrementItem(int index) {
        if (index < 0 || index >= items.size()) return;

        OrderItem item = items.get(index);
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            items.remove(index);
        }
    }
}
