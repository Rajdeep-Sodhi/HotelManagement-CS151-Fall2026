package model;

import exception.AuthorizationException;
import exception.HotelException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests permission settings */
class AuthorizationTest
{
	private Hotel hotel; // hotel instance

	//loads hotel instance with demo data
	@BeforeEach
	void setUp() throws HotelException
	{
		hotel = new Hotel(LocalDate.now());
		hotel.seedDemoData();
	}

	// tests that one guest cannot access other guest's records
	@Test
	void blocksOtherGuestAccessTest() throws HotelException
	{
		Session guest2Session = hotel.login("guest2", "guest234");
		Guest guest2 = hotel.findGuest(2, guest2Session);

		Reservation res = guest2.makeReservation(102, hotel.getCurrentDate(), hotel.getCurrentDate().plusDays(1), 1, guest2Session);

		Payment pay = hotel.payCash( res, res.getAmountDue(guest2Session), guest2Session);

		Session empSession = hotel.login("employee", "employee123");
		Employee emp = hotel.findEmployee(2, empSession);

		emp.clockIn(empSession);
		emp.checkInGuest(res, empSession);

		ServiceOrder order = hotel.createServiceOrder(
				res.getReservationId(), "LAUNDRY", 1, empSession);

		Session guest1Session = hotel.login("guest", "guest123");

		try
		{
			hotel.findGuest(2, guest1Session);
			fail("Guests should not access another guest's profile.");
		}
		catch (AuthorizationException ex)
		{
			// Expected denial.
		}

		try
		{
			guest2.getEmail(guest1Session);
			fail("Guests should not access another guest's email.");
		}
		catch (AuthorizationException ex)
		{
			
		}

		try
		{
			hotel.findReservation(res.getReservationId(), guest1Session);
			fail("Guests should not access another guest's reservation.");
		}
		catch (AuthorizationException ex)
		{
		}

		try
		{
			hotel.findServiceOrder(order.getOrderId(), guest1Session);
			fail("Guests should not access another guest's service order.");
		}
		catch (AuthorizationException ex)
		{
		}

		try
		{
			hotel.findPayment(pay.getPaymentId(), guest1Session);
			fail("Guests should not access another guest's payment.");
		}
		catch (AuthorizationException ex)
		{
		}
	}
	
	//tests that regular employees can't do manager actions
	@Test
	void blocksStaffFromManagerActionsTest() throws HotelException
	{
		Session empSession = hotel.login("employee", "employee123");
		Room room = hotel.findRoom(101, empSession);

		try
		{
			hotel.hireEmployee(
					"New Staff", "new@hotel.example", false, empSession);

			fail("Regular staff should not hire employees.");
		}
		catch (AuthorizationException ex)
		{
		
		}

		try
		{
			hotel.addRoom(301, 2, 90.00, empSession);
			fail("Regular staff should not add rooms.");
		}
		catch (AuthorizationException ex)
		{
			
		}

		try
		{
			hotel.removeRoom(101, empSession);
			fail("Regular staff should not remove rooms.");
		}
		catch (AuthorizationException ex)
		{
			
		}

		try
		{
			room.takeOutOfService(empSession);
			fail("Regular staff should not take rooms out of service.");
		}
		catch (AuthorizationException ex)
		{
			
		}

		try
		{
			hotel.summary(empSession);
			fail("Regular staff should not access the manager report.");
		}
		catch (AuthorizationException ex)
		{
			
		}

		try
		{
			hotel.advanceDate(
					hotel.getCurrentDate().plusDays(1), empSession);

			fail("Regular staff should not advance the date.");
		}
		catch (AuthorizationException ex)
		{
			
		}
	}
	
	//checks if new singups create just guest accounts, not manager or employee
	@Test
	void signupCreatesGuestAccounts() throws HotelException
	{
		Guest newGuest = hotel.registerGuestAccount( "New Guest", "new@guest.example", "4085559876", "newguest", "newpass123");

		Session session = hotel.login("newguest", "newpass123");

		assertEquals("GUEST", session.getRole());
		assertEquals(newGuest.getGuestId(), session.getPersonId());

		try
		{
			hotel.hireEmployee(
					"Unauthorized", "staff@hotel.example", true, session);

			fail("A guest account should not have manager permissions.");
		}
		catch (AuthorizationException ex)
		{
			
		}
	}

}
