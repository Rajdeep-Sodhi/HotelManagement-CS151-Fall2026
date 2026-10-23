package model;

import exception.BookingConflictException;
import exception.HotelException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests guest booking rules and reservation history. */
class GuestReservationTest
{
	private Hotel hotel; //hotel instance for test
	private Guest guest; // guest instance for test
	private Session session; // session instance for test

	@BeforeEach
	void setUp() throws HotelException
	{
		hotel = new Hotel(LocalDate.now());
		hotel.seedDemoData();
		session = hotel.login("guest", "guest123");
		guest = hotel.findGuest(1, session);
	}

	// tests if back to back reservations are allowed
	@Test
	void allowsBackToBackStays() throws HotelException
	{
		LocalDate arrival = hotel.getCurrentDate().plusDays(1);
		LocalDate sharedDate = arrival.plusDays(2);

		Reservation firstRes = guest.makeReservation(
				101, arrival, sharedDate, 2, session);

		Reservation nextRes = guest.makeReservation(
				101, sharedDate, sharedDate.plusDays(2), 1, session);

		assertEquals(sharedDate, firstRes.getDeparture(session));
		assertEquals(sharedDate, nextRes.getArrival(session));
	}
	
	// tests if bad room bookings are rejected
	@Test
	void rejectsBadBookings() throws HotelException
	{
		LocalDate arrival = hotel.getCurrentDate().plusDays(2);

		guest.makeReservation(
				101, arrival, arrival.plusDays(3), 2, session);

		//checks if overlapping bookings are rejeceted
		try
		{
			guest.makeReservation(
					101, arrival.plusDays(1),
					arrival.plusDays(4), 1, session);

			fail("Overlapping bookings should be rejected.");
		}
		catch (BookingConflictException ex)
		{
			
		}
		
		//checks if overlapping bookings are rejected
		try
		{
			guest.makeReservation(
					101, arrival, arrival.plusDays(3), 2, session);

			fail("Duplicate bookings should be rejected.");
		}
		catch (HotelException ex)
		{
			
		}
		
		//checks if old arrival dates get rejected
		try
		{
			guest.makeReservation(
					101, arrival.minusDays(3),
					arrival.plusDays(1), 1, session);

			fail("Past arrival dates should be rejected.");
		}
		catch (HotelException ex)
		{
			
		}

		//checks that departure dates are after arrival dates
		try
		{
			guest.makeReservation(
					101, arrival, arrival, 1, session);

			fail("Departure must follow arrival.");
		}
		catch (HotelException ex)
		{
			
		}

		//checks if groups that exceed room capacity are't able to book that room
		try
		{
			guest.makeReservation(
					102, arrival.plusDays(4),
					arrival.plusDays(5), 4, session);

			fail("Groups exceeding room capacity should be rejected.");
		}
		catch (HotelException ex)
		{
			
		}
	}
	
	// checks If changing reservation dates fail, original dates are kept
	@Test
	void keepsDatesAfterFailedChange() throws HotelException
	{
		LocalDate today = hotel.getCurrentDate();

		Reservation firstRes = guest.makeReservation(
				101, today.plusDays(1), today.plusDays(3), 1, session);

		guest.makeReservation(
				101, today.plusDays(5), today.plusDays(7), 1, session);

		try
		{
			guest.rescheduleReservation(
					firstRes, today.plusDays(6),
					today.plusDays(8), session);

			fail("Conflicting date changes should be rejected.");
		}
		catch (BookingConflictException ex)
		{
			
		}

		assertEquals(today.plusDays(1), firstRes.getArrival(session));
		assertEquals(today.plusDays(3), firstRes.getDeparture(session));
	}



}

