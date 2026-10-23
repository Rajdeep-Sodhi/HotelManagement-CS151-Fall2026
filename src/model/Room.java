package model;

import exception.AuthorizationException;
import exception.HotelException;

public class Room {

    // Represents physical readiness separately from the reservation calendar.
    private static final String READY = "READY";
    private static final String OCCUPIED = "OCCUPIED";
    private static final String DIRTY = "DIRTY";
    private static final String CLEANING = "CLEANING";
    private static final String OUT_OF_SERVICE = "OUT_OF_SERVICE";

    private final int roomNumber; // Positive, unique room number.
    private final int capacity; // Maximum positive number of occupants.
    private double nightlyRate; // Positive nightly rate rounded to cents.
    private String status; // READY, OCCUPIED, DIRTY, CLEANING, or OUT_OF_SERVICE; use String, not enum.
    private String previousStatus; // READY or DIRTY state to restore after maintenance.
    private final Hotel hotel; // Owning hotel used to check active reservations.

    /* Validate positive number/capacity, finite positive rate, and round the rate to cents;
       start in READY state. */
    Room(int number, int capacity, double rate, Hotel hotel) throws HotelException {
        if (number <= 0){
            throw new HotelException("Room number must be positive.");
        }
        if (capacity <= 0){
            throw new HotelException("Room capacity must be positive.");
        }
        if (hotel == null) {
            throw new HotelException("A room must belong to a hotel.");
        }

        this.roomNumber = number;
        this.capacity = capacity;
        this.nightlyRate = validRate(rate);
        this.status = READY;
        this.previousStatus = null;
        this.hotel = hotel;
    }

    /* On-duty staff change DIRTY to CLEANING. */
    public void startCleaning(Session actor) throws HotelException {
        requireSession(actor).requireOnDutyStaff();
        if (!status.equals(DIRTY)) {
            throw new HotelException("Only a DIRTY room can be cleaned. Room " + roomNumber + " is " + status + ".");
        }
        status = CLEANING;
    }

    /* On-duty staff change CLEANING to READY. */
    public void finishCleaning(Session actor) throws HotelException {
        requireSession(actor).requireOnDutyStaff();
        if (!status.equals(CLEANING)) {
            throw new HotelException("Room " + roomNumber + " is not being cleaned.");
        }
        status = READY;
    }

    /* Manager changes READY/DIRTY to OUT_OF_SERVICE only if Hotel has no active booking for
       this room; remember the prior readiness state. */
    public void takeOutOfService(Session actor) throws HotelException

    /* Manager restores the saved READY/DIRTY state. */
    public void returnToService(Session actor) throws HotelException

    /* Manager validates a new price; existing booking quotes stay fixed. */
    public void setNightlyRate(double rate, Session actor) throws HotelException

    /* Return the immutable, non-sensitive room number. */
    public int getRoomNumber() {
        return roomNumber;
    }

    /* Require active login before returning the maximum occupancy. */
    public int getCapacity(Session viewer) throws HotelException {
        requireSession(viewer).requireAuthenticated();
        return capacity;
    }

    /* Require active login before returning the current price. */
    public double getNightlyRate(Session viewer) throws HotelException {
        requireSession(viewer).requireAuthenticated();
        return nightlyRate;
    }

    /* Require active login before returning the room's readiness state. */
    public String getStatus(Session viewer) throws HotelException {
        requireSession(viewer).requireAuthenticated();
        return status;
    }

    /* Package-private transition from READY to OCCUPIED used by validated check-in. */
    void markOccupied() throws HotelException {
        if (!status.equals(READY)) {
            throw new HotelException("Room " + roomNumber + " is not ready. It is " + status + ".");
        }
        status = OCCUPIED;
    }

    /* Package-private transition from OCCUPIED to DIRTY used by validated checkout. */
    void markDirty() throws HotelException {
        if (!status.equals(OCCUPIED)) {
            throw new HotelException("Room " + roomNumber + " is not occupied.");
        }
        status = DIRTY;
    }

    /* Package-private capacity used during validated booking creation. */
    int capacity() {
        return capacity;
    }

    /* Package-private price used to capture a booking quote. */
    double rate() {
        return nightlyRate;
    }

    /* Package-private readiness state used by booking and maintenance checks. */
    String status() {
        return status;
    }

    // Rounds the rate to cents and rejects a rate that is zero after rounding
    private static double validRate(double rate) throws HotelException {
        double rounded = Hotel.roundMoney(rate);
        if (rounded <= 0) {
            throw new HotelException("Nightly rate must be greater than zero.");
        }
        return rounded;
    }

    // Rejects a missing session before any permission check
    private static Session requireSession(Session session) throws AuthorizationException {
        if (session == null) {
            throw new AuthorizationException("You must be logged in.");
        }
        return session;
    }

    /* Display room number, capacity, readiness, and current nightly rate. */
    @Override
    public String toString() {
        return String.format("Room %d | Capacity: %d | Status: %s | Nightly Rate: $%.2f",
                roomNumber, capacity, status, nightlyRate);
    }
}
