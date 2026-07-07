package model;

import model.enums.ReservationStatus;
import model.enums.TableStatus;

import java.io.Serial;
import java.io.Serializable;

public class Table implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private int tableNumber;
    private int capacity;
    private TableStatus status;
    private Order currentOrder; // nullable if the table is available
    private Reservation currentReservation;
    public Table(int id, int tableNumber, int capacity) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
    }

    public void assignReservation(Reservation reservation){
        if(reservation.getGuestCount() > this.capacity){
            throw new IllegalArgumentException("Party size (" + reservation.getGuestCount() + ") exceeds table capacity (" + this.capacity + ").");
        }
        this.currentReservation = reservation;
        this.status = TableStatus.RESERVED;
    }
    public void assignOrder(Order order){
        // If the table had a reservation waiting, mark that reservation as SEATED!
        if (this.currentReservation != null) {
            this.currentReservation.setStatus(ReservationStatus.SEATED);
            this.currentReservation = null; // Clear booking since they are now seated
        }
        this.currentOrder = order;
        this.status = TableStatus.OCCUPIED;
    }

    public void clearTable(){
        this.currentOrder = null;
        this.currentReservation = null;
        this.status = TableStatus.AVAILABLE;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public TableStatus getStatus() {
        return status;
    }

    public int getCapacity() {
        return capacity;
    }

    public Reservation getCurrentReservation() {
        return currentReservation;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getId() {
        return id;
    }
}
