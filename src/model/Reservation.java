package model;

import exception.AuthorizationException;
import exception.HotelException;
import java.time.LocalDate;

public class Reservation {
    // Links one guest, room, and date interval; owns its lifecycle and payment flag.
    private static final String CONFIRMED = "CONFIRMED";
    private static final String CHECKED_IN = "CHECKED_IN";
    private static final String CHECKED_OUT = "CHECKED_OUT";
    private static final String CANCELLED = "CANCELLED";

    private final int reservationId; // Immutable reservation identifier.
    private final Guest guest; // Guest who owns the stay.
    private final Room room; // Room booked for this stay.
    private final Hotel hotel; // Owning hotel used for date, conflict, and service-order checks.
    private LocalDate arrival; // Scheduled arrival date; must initially be today or later.
    private LocalDate departure; // Scheduled departure date, strictly after arrival.
    private final int partySize; // Positive number of guests within the room's capacity.
    private final double bookedNightlyRate; // Price captured at booking; later room-rate changes do not affect it.
    private String status; // CONFIRMED, CHECKED_IN, CHECKED_OUT, or CANCELLED.
    private boolean paid; // True only after one successful full payment.

    /* Validate the guest, room, dates, and party size; capture the room's price and start CONFIRMED and unpaid. Booked dates use [arrival, departure), allowing adjacent stays. */
        Reservation(int id, Guest guest, Room room, LocalDate arrival, LocalDate departure, int partySize, Hotel hotel) throws HotelException
    {
        if (id <= 0)
        {
            throw new HotelException("Reservation ID must be positive.");
        }
        if (guest == null || room == null || hotel == null)
        {
            throw new HotelException("A reservation requires a guest, a room, and a hotel.");
        }
        validateDates(arrival, departure, hotel);
        if (partySize <= 0)
        {
            throw new HotelException("Party size must be at least 1.");
        }
        if (partySize > room.capacity())
        {
            throw new HotelException("Room " + room.getRoomNumber() + " holds at most " + room.capacity() + " guests.");
        }

        this.reservationId = id;
        this.guest = guest;
        this.room = room;
        this.hotel = hotel;
        this.arrival = arrival;
        this.departure = departure;
        this.partySize = partySize;
        this.bookedNightlyRate = room.rate();
        this.status = CONFIRMED;
        this.paid = false;
    }

    /* Return the immutable, non-sensitive reservation ID. */
    public int getReservationId()
    {
        return reservationId;
    }

    /* Shared owner/staff guard, also used by this stay's service orders. */
    void requireAccess(Session actor) throws HotelException 
    {
        requireSession(actor).requireGuestAccess(guest);
    }

    /* Authorize the owner or staff before returning the arrival date. */
    public LocalDate getArrival(Session viewer) throws HotelException 
    {
        requireAccess(viewer);
        return arrival;
    }

    /* Authorize the owner or staff before returning the departure date. */
    public LocalDate getDeparture(Session viewer) throws HotelException
    {
        requireAccess(viewer);
        return departure;
    }

    /* Authorize the owner or staff before returning the booked occupancy. */
    public int getPartySize(Session viewer) throws HotelException
    {
        requireAccess(viewer);
        return partySize;
    }
    /* Authorize the owner or staff before returning the booking's lifecycle state */
    public String getStatus(Session viewer) throws HotelException 
    {
        requireAccess(viewer);
        return status;
    }
    /* Package-private owner reference used by authorization and registry validation. */
    Guest guest()
    {
        return guest;
    }

    /* Package-private room reference used by conflict and removal checks. */
    Room room() 
    {
        return room;
    }
    /* Package-private start date used by calendar checks. */
    LocalDate arrival() 
    {
        return arrival;
    }

    /* Package-private end date used by calendar checks. */
    LocalDate departure() 
    {
        return departure;
    }

    /* Package-private lifecycle state used by Hotel's shared checks. */
    String status() 
    {
        return status;
    }

    // Rejects missing dates, an arrival before today, and a departure that is not after arrival
    private static void validateDates(LocalDate arrival, LocalDate departure, Hotel hotel) throws HotelException 
    {
        if (arrival == null || departure == null) {
            throw new HotelException("Arrival and departure dates are required.");
        }
        if (arrival.isBefore(hotel.getCurrentDate())) {
            throw new HotelException("Arrival cannot be before today, " + hotel.getCurrentDate() + ".");
        }
        if (!departure.isAfter(arrival)) {
            throw new HotelException("Departure must be after arrival.");
        }
    }

    // Rejects a missing session before any permission check
    private static Session requireSession(Session session) throws AuthorizationException {
        if (session == null) {
            throw new AuthorizationException("You must be logged in.");
        }
        return session;
    }


    /* Display reservation ID and lifecycle state; omit personal details and billing amounts. */
    @Override 
    public String toString() {
        return "Reservation " + reservationId + " | Status: " + status;
    }
}
