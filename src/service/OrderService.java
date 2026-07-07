package service;

import model.*;
import model.enums.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/*
t manages opening checks, sending items to the Kitchen Display System (KDS), and generating final bills with taxes.
 */
public class OrderService {
    private List<Order> activeOrders;
    private final List<Order> orderHistory;
    private int nextOrderIdCounter = 1;
    private final double TAX_RATE = 0.20; // 20% VAT/Tax rate

    public OrderService() {

        this.activeOrders = new ArrayList<>();
        this.orderHistory = new ArrayList<>();
    }

    /**
     * Opens a new order check for a table and links the acting waiter.
     */
    public Order openOrder(Table table, Employee waiter) {
        if (table.getCurrentOrder() != null) {
            throw new IllegalStateException("Table #" + table.getTableNumber() + " already has an open check!");
        }

        Order newOrder = new Order(nextOrderIdCounter++, table.getId(), waiter);
        activeOrders.add(newOrder);
        table.assignOrder(newOrder);
        return newOrder;
    }

    /**
     * Adds an item to an active order.
     */
    public void addItemToOrder(Order order, MenuItem item, int quantity, String notes) {
        if (!item.isAvailable()) {
            throw new IllegalStateException("Sorry, " + item.getName() + " is currently sold out!");
        }
        order.addItem(item, quantity, notes);
    }

    /**
     * KDS Method: Fetches all orders currently waiting for kitchen preparation.
     */
    public List<Order> getKitchenQueue() {
        return activeOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.IN_PREPARATION)
                .collect(Collectors.toList());
    }

    /**
     * Advances order workflow status (e.g., Chef clicks "Ready").
     */
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        order.setStatus(newStatus);
    }

    /**
     * Calculates the final checkout receipt data (Subtotal, Tax, Final Total).
     */
    public Receipt generateBill(Order order) {
        double subtotal = order.calculateTotal();
        double tax = subtotal * TAX_RATE;
        double finalTotal = subtotal + tax;

        return new Receipt(order.getId(), subtotal, tax, finalTotal);
    }

    /**
     * Closes out the table upon payment settlement.
     */
    public void settleOrder(Order order, Table table) {
        order.setStatus(OrderStatus.CLOSED);
        table.clearTable();
        activeOrders.remove(order);
        orderHistory.add(order);
    }

    // Helper data record/class for billing
    public static class Receipt {
        public final int orderId;
        public final double subtotal;
        public final double tax;
        public final double total;

        public Receipt(int orderId, double subtotal, double tax, double total) {
            this.orderId = orderId;
            this.subtotal = subtotal;
            this.tax = tax;
            this.total = total;
        }
    }

    public List<Order> getActiveOrders() {
        return this.activeOrders;
    }

    public void setActiveOrders(List<Order> loadedOrders) {
        this.activeOrders.clear();
        this.activeOrders.addAll(loadedOrders);

        // Smart Counter Sync: Find the highest ID among loaded orders
        int maxId = loadedOrders.stream()
                .mapToInt(Order::getId)
                .max()
                .orElse(0); // If list is empty, maxId is 0

        // Ensure the next order created gets the next available number!
        this.nextOrderIdCounter = maxId + 1;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<Order> loadedHistory) {
        this.orderHistory.clear();
        this.orderHistory.addAll(loadedHistory);
    }
    /**
     * Calculates total gross revenue across all settled checks.
     */
    public double getTotalRevenue() {
        return orderHistory.stream()
                .mapToDouble(order -> order.calculateTotal() * (1 + TAX_RATE))
                .sum();
    }
}