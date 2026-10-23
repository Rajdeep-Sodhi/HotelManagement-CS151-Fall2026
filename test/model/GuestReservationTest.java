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

}

