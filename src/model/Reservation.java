package model;

import model.enums.ReservationStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Reservation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String customerName;
    private String contactPhone;
    private LocalDateTime reservationTime;
    private int guestCount;
    private int assignedTableId;
    private ReservationStatus status;

    public Reservation(int id, String customerName, String contactPhone, LocalDateTime reservationTime, int guestCount, int assignedTableId) {
        this.id = id;
        this.customerName = customerName;
        this.contactPhone = contactPhone;
        this.reservationTime = reservationTime;
        this.guestCount = guestCount;
        this.assignedTableId = assignedTableId;
        this.status = ReservationStatus.CONFIRMED;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public int getAssignedTableId() {
        return assignedTableId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public void setAssignedTableId(int assignedTableId) {
        this.assignedTableId = assignedTableId;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
