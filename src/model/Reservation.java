package model;

import exception.AuthorizationException;
import exception.HotelException;
import java.time.LocalDate;

public class Reservation {
    // Links one guest, room, and date interval; owns its lifecycle and payment flag.

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
        if (arrival == null || departure == null)
        {
            throw new HotelException("Arrival and departure dates are required.");
        }
        if (arrival.isBefore(hotel.getCurrentDate()))
        {
            throw new HotelException("Arrival cannot be before today, " + hotel.getCurrentDate() + ".");
        }
        if (!departure.isAfter(arrival))
        {
            throw new HotelException("Departure must be after arrival.");
        }


        this.reservationId = id;
        this.guest = guest;
        this.room = room;
        this.hotel = hotel;
        this.arrival = arrival;
        this.departure = departure;
        this.partySize = partySize;
        this.bookedNightlyRate = room.rate();
        this.paid = false;
    }


    /* Owner/staff; CONFIRMED and unpaid only. Ask Hotel to check dates and conflicts
       excluding this booking, then replace both dates together. */
    public void reschedule(LocalDate arrival, LocalDate departure, Session actor) throws HotelException
    {
        requireAccess(actor);
        if (!status.equals("CONFIRMED"))
        {
            throw new HotelException("Reservation " + reservationId + " cannot be rescheduled. It is " + status + ".");
        }
        if (paid)
        {
            throw new HotelException("Reservation " + reservationId + " is already paid and cannot be rescheduled.");
        }
        if (arrival == null || departure == null)
        {
            throw new HotelException("Arrival and departure dates are required.");
        }
        if (arrival.isBefore(hotel.getCurrentDate()))
        {
            throw new HotelException("Arrival cannot be before today, " + hotel.getCurrentDate() + ".");
        }
        if (!departure.isAfter(arrival))
        {
            throw new HotelException("Departure must be after arrival.");
        }
        hotel.validateAvailability(room, arrival, departure, reservationId);
        this.arrival = arrival;
        this.departure = departure;
    }

    /* Owner/staff; CONFIRMED and unpaid only. Change status to CANCELLED. */
    public void cancel(Session actor) throws HotelException
    {
        requireAccess(actor);
        if (!status.equals("CONFIRMED"))
        {
            throw new HotelException("Reservation " + reservationId + " cannot be cancelled. It is " + status + ".");
        }
        if (paid)
        {
            throw new HotelException("Reservation " + reservationId + " is already paid and cannot be cancelled.");
        }
        status = "CANCELLED";
    }

    /* Return the immutable, non-sensitive reservation ID. */
    public int getReservationId()
    {
        return reservationId;
    }

    /* Shared owner/staff guard, also used by this stay's service orders. */
    void requireAccess(Session actor) throws HotelException
    {
        if (actor == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        actor.requireGuestAccess(guest);
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

    /* Authorize the owner or staff before returning the booking's lifecycle state. */
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

    /* Display reservation ID and lifecycle state; omit personal details and billing amounts. */
    @Override
    public String toString()
    {
        return "Reservation " + reservationId + " | Status: " + status;
    }
}