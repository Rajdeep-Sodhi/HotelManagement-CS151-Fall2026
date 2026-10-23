package model;

import exception.AuthorizationException;
import exception.HotelException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores guest details and reservations*/
public class Guest
{
	private final int guestId; // unique guest id
	private final String name; // guest's name
	private final String email;// guest's email
	private String phone; //guest's phone number
	private final Hotel hotel; //instance of hotel guest stays at
	private final List<Reservation> resList; //reservation list of guest

	// Validates guest details
	Guest(int id, String name, String email, String phone, Hotel hotel)
			throws HotelException
	{
		if (id <= 0 || hotel == null)
		{
			throw new HotelException("A guest requires a valid ID and a hotel.");
		}

		guestId = id;
		this.name = checkName(name);
		this.email = checkEmail(email);
		this.phone = checkPhone(phone);
		this.hotel = hotel;
		resList = new ArrayList<>();
	}

	// allows guest to make reservations for hotel
	public Reservation makeReservation(int roomNum, LocalDate arrival,
			LocalDate departure, int numGuests, Session session)
			throws HotelException
	{
		checkAccess(session);

		for (Reservation bookedRes : resList)
		{
			if (bookedRes.room().getRoomNumber() == roomNum
					&& arrival != null && arrival.equals(bookedRes.arrival())
					&& departure != null && departure.equals(bookedRes.departure())
					&& isActiveRes(bookedRes))
			{
				throw new HotelException(
						"This guest already has that active reservation.");
			}
		}

		Reservation res = hotel.recordReservation(this, roomNum,
				arrival, departure, numGuests, session);

		resList.add(res);
		return res;
	}

	// allows guest to cancel a reservation
	public void cancelReservation(Reservation res, Session session)
			throws HotelException
	{
		checkResOwner(res, session);
		res.cancel(session);
	}

	//allows guest to reschedule their reservation
	public void rescheduleReservation(Reservation res, LocalDate arrival,
			LocalDate departure, Session session) throws HotelException
	{
		checkResOwner(res, session);
		res.reschedule(arrival, departure, session);
	}

	// Returns a list of reservations
	public List<Reservation> getReservations(Session session)
			throws HotelException
	{
		checkAccess(session);
		return Collections.unmodifiableList(new ArrayList<>(resList));
	}

	// getter methods for guest information
	public String getName(Session session) throws HotelException
	{
		checkAccess(session);
		return name;
	}

	public String getEmail(Session session) throws HotelException
	{
		checkAccess(session);
		return email;
	}

	public String getPhone(Session session) throws HotelException
	{
		checkAccess(session);
		return phone;
	}

	// Setter methods for guest information
	public void setPhone(String phone, Session session) throws HotelException
	{
		checkAccess(session);
		this.phone = checkPhone(phone);
	}

	public int getGuestId()
	{
		return guestId;
	}

	// checks for duplicates
	String identityEmail()
	{
		return email;
	}

	//toString method that prints basic guest info.
	@Override
	public String toString()
	{
		// Keep contact details private.
		return "Guest{id=" + guestId
				+ ", reservationCount=" + resList.size() + "}";
	}

	//
	private void checkAccess(Session session) throws HotelException
	{
		if (session == null)
		{
			throw new AuthorizationException(
					"Log in to access a guest profile.");
		}

		session.requireGuestAccess(this);
	}

	// The booking must belong to this guest.
	private void checkResOwner(Reservation res, Session session)
			throws HotelException
	{
		checkAccess(session);

		if (res == null || !resList.contains(res))
		{
			throw new HotelException(
					"That reservation does not belong to this guest.");
		}
	}

	//helper methods to check guest information
	private boolean isActiveRes(Reservation res)
	{
		return "CONFIRMED".equals(res.status())
				|| "CHECKED_IN".equals(res.status());
	}

	private static String checkName(String name) throws HotelException
	{
		if (name == null || name.trim().isEmpty())
		{
			throw new HotelException("Guest name cannot be blank.");
		}

		return name.trim();
	}
	
	//researched how emails and phone number formats are usually validated in real world scenarios, because I couldn't think of easy way to validate them.
	private static String checkEmail(String email) throws HotelException
	{
		if (email == null)
		{
			throw new HotelException("Guest email is required.");
		}

		String cleanEmail = email.trim().toLowerCase();

		if (!cleanEmail.matches("[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}"))
		{
			throw new HotelException("Enter a valid guest email address.");
		}

		return cleanEmail;
	}

	private static String checkPhone(String phone) throws HotelException
	{
		if (phone == null)
		{
			throw new HotelException("Guest phone number is required.");
		}

		String cleanPhone = phone.trim();
		String digits = cleanPhone.replaceAll("\\D", "");

		if (!cleanPhone.matches("[+()0-9 .\\-]+")
				|| digits.length() < 7 || digits.length() > 15)
		{
			throw new HotelException("Enter a valid guest phone number.");
		}

		return cleanPhone;
	}
}
