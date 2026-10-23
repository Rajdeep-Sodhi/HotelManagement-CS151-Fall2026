package model;

import exception.AuthorizationException;
import exception.HotelException;

public class Room {

    //Readiness states for a room
    private static final String READY = "READY";
    private static final String OCCUPIED = "OCCUPIED";
    private static final String DIRTY = "DIRTY";
    private static final String CLEANING = "CLEANING";
    private static final String OUT_OF_SERVICE = "OUT OF SERVICE";

    // Basic information for each hotel room
    private final int roomNumber;
    private double nightlyRate;
    private final int capacity;
    private String status;
    private String previousStatus;
    private final Hotel hotel;

    // Creates a room with the information provided
    public Room(int number, int capacity, double rate, Hotel hotel) throws HotelException {
        if (number <= 0){
            throw new HotelException("Room number must be positive.");
        }
        if (capacity <= 0){
            throw new HotelException("Room capacity mustbe positive.");
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


    // Displays the main details of the room
    @Override
    public String toString() {
        
    }

}
