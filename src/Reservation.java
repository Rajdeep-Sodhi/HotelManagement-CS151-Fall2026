public class Reservation {

    // Basic information for each reservation
    private int reservationID;
    private Guest guest;
    private Room room;
    // Dates are consecutive day numbers, so their difference is the stay length.
    private int checkInDate;
    private int checkOutDate;

    // Creates a reservation with the information provided
    public Reservation(int reservationID, Guest guest, Room room,
                       int checkInDate, int checkOutDate) {
        if (guest == null || room == null) {
            throw new IllegalArgumentException("A reservation requires a guest and a room.");
        }
        if (checkOutDate <= checkInDate) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if ((long) checkOutDate - checkInDate > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The stay is too long.");
        }

        this.reservationID = reservationID;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Returns the reservation ID
    public int getReservationID() {
        return reservationID;
    }

    // Returns the guest who made the reservation
    public Guest getGuest() {
        return guest;
    }

    // Returns the reserved room
    public Room getRoom() {
        return room;
    }

    // Returns the check-in day
    public int getCheckInDate() {
        return checkInDate;
    }

    // Returns the check-out day
    public int getCheckOutDate() {
        return checkOutDate;
    }

    // Calculates the room cost for the number of nights reserved
    public double calculateTotal() {
        return room.calculateStayCost(checkOutDate - checkInDate);
    }
    
    public void rescheduleReservation(int newCheckInDate, int newCheckOutDate)
    {
		if(newCheckOutDate <= newCheckInDate)
		{
			throw new IllegalArgumentException("Check out should be after check in.");
			
		}
		if((long) newCheckOutDate - newCheckInDate > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("The stay is too long");
		}
		checkInDate = newCheckInDate;
		checkOutDate = newCheckOutDate;
	}

    // Displays the reservation and guest details
    public void displayInfo() {
        System.out.println("Reservation ID: " + reservationID);
        System.out.println("Guest Information:");
        guest.displayInfo();
        System.out.println("Room: " + room.getRoomNumber());
        System.out.println("Check-in Date: " + checkInDate);
        System.out.println("Check-out Date: " + checkOutDate);
        System.out.printf("Total: $%.2f%n", calculateTotal());
    }
}
