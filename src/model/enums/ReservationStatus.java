package model.enums;

public enum ReservationStatus {
    CONFIRMED,
    SEATED, // Guest has arrived and sit at the table
    CANCELLED,
    NO_SHOW // Guests didn't arrived after X minutes
}
