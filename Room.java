public class Room {

    // Basic information for each hotel room
    private int roomNumber;
    private String roomType;
    private double nightlyRate;
    private int capacity;
    private boolean available;

    // Creates a room with the information provided
    public Room(int roomNumber, String roomType, double nightlyRate, int capacity) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.nightlyRate = nightlyRate;
        this.capacity = capacity;
        this.available = true;
    }

    // Returns the room number
    public int getRoomNumber() {
        return roomNumber;
    }

    // Returns the type of room
    public String getRoomType() {
        return roomType;
    }

    // Returns the price of the room per night
    public double getNightlyRate() {
        return nightlyRate;
    }

    // Returns the maximum number of guests allowed
    public int getCapacity() {
        return capacity;
    }

    // Checks if the room is currently available
    public boolean isAvailable() {
        return available;
    }

        // Updates the room number if it is valid
    public void setRoomNumber(int roomNumber) {
        if (roomNumber > 0) {
            this.roomNumber = roomNumber;
        }
    }

    // Updates the room type if a type was provided
    public void setRoomType(String roomType) {
        if (roomType != null && !roomType.trim().isEmpty()) {
            this.roomType = roomType;
        }
    }

    // Makes sure the nightly rate cannot be negative
    public void setNightlyRate(double nightlyRate) {
        if (nightlyRate >= 0) {
            this.nightlyRate = nightlyRate;
        }
    }

    // Makes sure every room can hold at least one guest
    public void setCapacity(int capacity) {
        if (capacity > 0) {
            this.capacity = capacity;
        }
    }

    // Updates the current availability of the room
    public void setAvailable(boolean available) {
        this.available = available;
    }
}