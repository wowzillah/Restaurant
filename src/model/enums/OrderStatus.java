package model.enums;

public enum OrderStatus {
    PENDING,          // Order placed by waiter
    IN_PREPARATION,   // Acknowledged by kitchen
    READY_TO_SERVE,   // Prepared by kitchen
    SERVED,           // Delivered to table
    CLOSED,           // Bill paid
    CANCELLED
}
