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
}