package model;

import exception.AuthorizationException;
import exception.BookingConflictException;
import exception.CapacityExceededException;
import exception.HotelException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stores hotel records and coordinates shared checks. */
public class Hotel
{
	private static final int MAX_OBJECTS = 100; // upper cap on max instances of a class

	private final Map<Integer, Guest> guestMap; // map of guests
	private final Map<Integer, Employee> empMap; // map of  employees
	private final Map<Integer, Room> roomMap; // map of rooms
	private final Map<Integer, Reservation> resMap; // map of reservations
	private final Map<Integer, ServiceOrder> orderMap; // map of service orders
	private final Map<Integer, Payment> payMap; // map of payments
	private final Map<String, Account> acctMap; //map of accounts
	private Session activeSession; // tracks users active login session
	private LocalDate currentDate; // stores current date

	// constructor
	public Hotel(LocalDate initialDate)
	{
		if (initialDate == null)
		{
			throw new IllegalArgumentException("A hotel requires an initial date.");
		}

		guestMap = new LinkedHashMap<>();
		empMap = new LinkedHashMap<>();
		roomMap = new LinkedHashMap<>();
		resMap = new LinkedHashMap<>();
		orderMap = new LinkedHashMap<>();
		payMap = new LinkedHashMap<>();
		acctMap = new LinkedHashMap<>();
		currentDate = initialDate;
	}

	// loads demo data on starting program
	public void seedDemoData() throws HotelException
	{
		if (!guestMap.isEmpty() || !empMap.isEmpty() || !roomMap.isEmpty()
				|| !resMap.isEmpty() || !orderMap.isEmpty()
				|| !payMap.isEmpty() || !acctMap.isEmpty() || activeSession != null)
		{
			throw new HotelException("Demo data can only be added to a new hotel.");
		}

		checkLimit(guestMap.size());
		checkLimit(empMap.size());
		checkLimit(roomMap.size());
		checkLimit(acctMap.size());

		// Validate all sample objects before storing them.
		Employee manager = new Employee(
				1, "Demo Manager", "manager@hotel.example", true);

		Employee emp = new Employee(
				2, "Demo Employee", "employee@hotel.example", false);

		Guest firstGuest = new Guest(
				1, "Demo Guest", "guest@hotel.example", "+14085550102", this);

		Guest secondGuest = new Guest(
				2, "Second Guest", "guest2@hotel.example", "+14085550103", this);

		Room standardRoom = new Room(101, 2, 100.00, this);
		Room deluxeRoom = new Room(102, 3, 150.00, this);

		Account managerAcct = new Account(
				"manager", "manager123", null, manager);

		Account empAcct = new Account(
				"employee", "employee123", null, emp);

		Account firstGuestAcct = new Account(
				"guest", "guest123", firstGuest, null);

		Account secondGuestAcct = new Account(
				"guest2", "guest234", secondGuest, null);

		empMap.put(manager.getEmployeeId(), manager);
		empMap.put(emp.getEmployeeId(), emp);
		guestMap.put(firstGuest.getGuestId(), firstGuest);
		guestMap.put(secondGuest.getGuestId(), secondGuest);
		roomMap.put(standardRoom.getRoomNumber(), standardRoom);
		roomMap.put(deluxeRoom.getRoomNumber(), deluxeRoom);
		acctMap.put(managerAcct.username(), managerAcct);
		acctMap.put(empAcct.username(), empAcct);
		acctMap.put(firstGuestAcct.username(), firstGuestAcct);
		acctMap.put(secondGuestAcct.username(), secondGuestAcct);
	}

	// takes care of workflow for user login
	public Session login(String username, String password) throws HotelException
	{
		String cleanName = cleanUsername(username);
		Account acct = acctMap.get(cleanName);

		if (acct == null || !acct.authenticate(password))
		{
			throw new AuthorizationException("Incorrect username or password.");
		}

		if (activeSession != null)
		{
			activeSession.logout();
		}

		activeSession = new Session(acct);
		return activeSession;
	}

	// accounts for adding new guest to hotel
	public Guest registerGuest(String name, String email, String phone, Session session)
			throws HotelException
	{
		checkRole(session, new String[] {"EMPLOYEE", "MANAGER"});
		checkLimit(guestMap.size());

		Guest guest = new Guest(
				guestMap.size() + 1, name, email, phone, this);

		checkGuestEmail(guest.identityEmail());

		guestMap.put(guest.getGuestId(), guest);
		return guest;
	}

	// Create a guest and login account together.
	public Guest registerGuestAccount(String name, String email, String phone,
			String username, String password) throws HotelException
	{
		checkLimit(guestMap.size());
		checkLimit(acctMap.size());

		Guest guest = new Guest(
				guestMap.size() + 1, name, email, phone, this);

		checkGuestEmail(guest.identityEmail());

		Account acct = new Account(username, password, guest, null);
		checkUsername(acct.username());

		// Save both records after all checks pass.
		guestMap.put(guest.getGuestId(), guest);
		acctMap.put(acct.username(), acct);
		return guest;
	}

	// Lets managers add/hire employees to hotel
	public Employee hireEmployee(String name, String email, boolean manager, Session session)
			throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});
		checkLimit(empMap.size());

		Employee emp = new Employee(
				empMap.size() + 1, name, email, manager);

		for (Employee savedEmp : empMap.values())
		{
			if (savedEmp.identityEmail().equals(emp.identityEmail()))
			{
				throw new HotelException(
						"An employee with that email already exists.");
			}
		}

		empMap.put(emp.getEmployeeId(), emp);
		return emp;
	}

	// Creates a account in hotel
	public void createAccount(String username, String password, Guest guest,
			Employee emp, Session session) throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});
		checkLimit(acctMap.size());

		if ((guest == null) == (emp == null))
		{
			throw new HotelException(
					"Choose exactly one registered guest or employee.");
		}

		if (guest != null && guestMap.get(guest.getGuestId()) != guest)
		{
			throw new HotelException("The guest is not registered at this hotel.");
		}

		if (emp != null && empMap.get(emp.getEmployeeId()) != emp)
		{
			throw new HotelException(
					"The employee is not registered at this hotel.");
		}

		for (Account savedAcct : acctMap.values())
		{
			if ((guest != null && savedAcct.guest() == guest)
					|| (emp != null && savedAcct.employee() == emp))
			{
				throw new HotelException("That person already has an account.");
			}
		}

		Account acct = new Account(username, password, guest, emp);
		checkUsername(acct.username());

		acctMap.put(acct.username(), acct);
	}

	// adds available room in hotel
	public Room addRoom(int roomNum, int capacity, double rate, Session session)
			throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});
		checkLimit(roomMap.size());

		if (roomMap.containsKey(roomNum))
		{
			throw new HotelException("Room " + roomNum + " already exists.");
		}

		Room room = new Room(roomNum, capacity, rate, this);
		roomMap.put(roomNum, room);
		return room;
	}

	// removes available room in hotel
	public void removeRoom(int roomNum, Session session) throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});
		Room room = roomMap.get(roomNum);

		if (room == null)
		{
			throw new HotelException("Room " + roomNum + " does not exist.");
		}

		for (Reservation res : resMap.values())
		{
			if (res.room() == room)
			{
				throw new HotelException(
						"Cannot remove a room referenced by reservation history.");
			}
		}

		if ("OCCUPIED".equals(room.status()) || "CLEANING".equals(room.status()))
		{
			throw new HotelException(
					"Cannot remove a room that is occupied or being cleaned.");
		}

		roomMap.remove(roomNum);
	}

	// Find rooms that fit the group and dates.
	public List<Room> searchRooms(LocalDate arrival, LocalDate departure,
			int numGuests, Session session) throws HotelException
	{
		checkSession(session);
		checkDates(arrival, departure);

		if (numGuests <= 0)
		{
			throw new HotelException("Party size must be positive.");
		}

		List<Room> openRooms = new ArrayList<>();

		for (Room room : roomMap.values())
		{
			if (numGuests > room.capacity()
					|| "OUT_OF_SERVICE".equals(room.status())
					|| (arrival.equals(currentDate) && !"READY".equals(room.status())))
			{
				continue;
			}

			try
			{
				validateAvailability(room, arrival, departure, null);
				openRooms.add(room);
			}
			catch (BookingConflictException ignored)
			{
				// Skip rooms with conflicting bookings.
			}
		}

		return Collections.unmodifiableList(openRooms);
	}

	// checks if reservation details are good and adds reservation to resMap
	Reservation recordReservation(Guest guest, int roomNum, LocalDate arrival,
			LocalDate departure, int numGuests, Session session) throws HotelException
	{
		checkSession(session);

		if (guest == null || guestMap.get(guest.getGuestId()) != guest)
		{
			throw new HotelException("The guest is not registered at this hotel.");
		}

		session.requireGuestAccess(guest);
		Room room = roomMap.get(roomNum);

		if (room == null)
		{
			throw new HotelException("Room " + roomNum + " does not exist.");
		}

		checkDates(arrival, departure);

		if (numGuests <= 0 || numGuests > room.capacity())
		{
			throw new HotelException(
					"Party size must fit the room's positive capacity.");
		}

		checkLimit(resMap.size());

		for (Reservation bookedRes : resMap.values())
		{
			if (bookedRes.guest() == guest && bookedRes.room() == room
					&& arrival.equals(bookedRes.arrival())
					&& departure.equals(bookedRes.departure())
					&& isActiveRes(bookedRes))
			{
				throw new HotelException(
						"This guest already has that active reservation.");
			}
		}

		validateAvailability(room, arrival, departure, null);

		Reservation res = new Reservation(
				resMap.size() + 1, guest, room,
				arrival, departure, numGuests, this);

		resMap.put(res.getReservationId(), res);
		return res;
	}

	// creates a new serviceOrder
	public ServiceOrder createServiceOrder(int resId, String item,
			int quantity, Session session) throws HotelException
	{
		checkSession(session);
		Reservation res = resMap.get(resId);

		if (res == null)
		{
			throw new HotelException(
					"Reservation " + resId + " does not exist.");
		}

		res.requireAccess(session);

		if (!"CHECKED_IN".equals(res.status()))
		{
			throw new HotelException("Services require a checked-in stay.");
		}

		for (ServiceOrder savedOrder : orderMap.values())
		{
			if (savedOrder.reservation() == res
					&& "OPEN".equals(savedOrder.status()))
			{
				throw new HotelException(
						"This stay already has an open service order; add to it.");
			}
		}

		checkLimit(orderMap.size());

		ServiceOrder order = new ServiceOrder(
				orderMap.size() + 1, res, item, quantity);

		orderMap.put(order.getOrderId(), order);
		return order;
	}

	// Lists only orders belonging to an authorized, registered reservation.
	public List<ServiceOrder> getServiceOrders(Reservation reservation, Session session)
			throws HotelException
	{
		checkSession(session);
		if (reservation == null || resMap.get(reservation.getReservationId()) != reservation)
		{
			throw new HotelException("The reservation is not registered at this hotel.");
		}
		reservation.requireAccess(session);
		List<ServiceOrder> orders = new ArrayList<>();
		for (ServiceOrder order : orderMap.values())
		{
			if (order.reservation() == reservation) orders.add(order);
		}
		return Collections.unmodifiableList(orders);
	}

	// handles cash payment process
	public Payment payCash(Payable purchase, double cashPaid, Session session)
			throws HotelException
	{
		double amountDue = checkPayment(purchase, session);

		CashPayment pay = new CashPayment(
				payMap.size() + 1, purchase, amountDue, cashPaid);

		purchase.pay(pay, session);
		payMap.put(pay.getPaymentId(), pay);
		return pay;
	}

	// handles card payment process
	public Payment payCard(Payable purchase, boolean approved, Session session)
			throws HotelException
	{
		double amountDue = checkPayment(purchase, session);

		CardPayment pay = new CardPayment(
				payMap.size() + 1, purchase, amountDue, approved);

		purchase.pay(pay, session);
		payMap.put(pay.getPaymentId(), pay);
		return pay;
	}

	// finds a guest in hotel from guest map
	public Guest findGuest(int guestId, Session session) throws HotelException
	{
		checkSession(session);
		Guest guest = guestMap.get(guestId);

		if (guest == null)
		{
			throw new HotelException("Guest " + guestId + " does not exist.");
		}

		session.requireGuestAccess(guest);
		return guest;
	}

	// finds employee in hotel from employee map
	public Employee findEmployee(int empId, Session session) throws HotelException
	{
		checkSession(session);
		Employee emp = empMap.get(empId);

		if (emp == null)
		{
			throw new HotelException("Employee " + empId + " does not exist.");
		}

		session.requireEmployeeAccess(emp);
		return emp;
	}

	// finds room in room Map.
	public Room findRoom(int roomNum, Session session) throws HotelException
	{
		checkSession(session);
		Room room = roomMap.get(roomNum);

		if (room == null)
		{
			throw new HotelException("Room " + roomNum + " does not exist.");
		}

		return room;
	}

	// finds reservation in reservation map
	public Reservation findReservation(int resId, Session session)
			throws HotelException
	{
		checkSession(session);
		Reservation res = resMap.get(resId);

		if (res == null)
		{
			throw new HotelException("Reservation " + resId + " does not exist.");
		}

		res.requireAccess(session);
		return res;
	}

	// finds service order from order maps.
	public ServiceOrder findServiceOrder(int orderId, Session session)
			throws HotelException
	{
		checkSession(session);
		ServiceOrder order = orderMap.get(orderId);

		if (order == null)
		{
			throw new HotelException(
					"Service order " + orderId + " does not exist.");
		}

		order.reservation().requireAccess(session);
		return order;
	}

	// finds paments in payment map
	public Payment findPayment(int payId, Session session) throws HotelException
	{
		checkSession(session);
		Payment pay = payMap.get(payId);

		if (pay == null)
		{
			throw new HotelException("Payment " + payId + " does not exist.");
		}

		pay.target().getAmountDue(session);
		return pay;
	}

	// Show managers counts and money received.
	public String summary(Session session) throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});
		int occupiedRooms = 0;
		double totalPaid = 0.0;

		for (Room room : roomMap.values())
		{
			if ("OCCUPIED".equals(room.status()))
			{
				occupiedRooms++;
			}
		}

		for (Payment pay : payMap.values())
		{
			totalPaid = roundMoney(totalPaid + pay.processedAmount());
		}

		return "Guests: " + guestMap.size()
				+ ", employees: " + empMap.size()
				+ ", rooms: " + roomMap.size()
				+ ", occupied rooms: " + occupiedRooms
				+ ", reservations: " + resMap.size()
				+ ", service orders: " + orderMap.size()
				+ ", payments: " + payMap.size()
				+ ", total received: " + String.format("%.2f", totalPaid);
	}

	//returns current date
	public LocalDate getCurrentDate()
	{
		return currentDate;
	}

	// checks if new date is after currentdate
	public void advanceDate(LocalDate newDate, Session session) throws HotelException
	{
		checkRole(session, new String[] {"MANAGER"});

		if (newDate == null || !newDate.isAfter(currentDate))
		{
			throw new HotelException(
					"New date must be later than the current date.");
		}

		for (Reservation res : resMap.values())
		{
			if ("CONFIRMED".equals(res.status())
					&& res.arrival().isBefore(newDate))
			{
				throw new HotelException(
						"Resolve the confirmed arrival for reservation "
						+ res.getReservationId() + " before advancing.");
			}
		}

		currentDate = newDate;
	}

	// makes sure droom is available between arrival and departure dates
	void validateAvailability(Room room, LocalDate arrival, LocalDate departure,
			Integer skipResId) throws HotelException
	{
		checkDates(arrival, departure);

		if (room == null || roomMap.get(room.getRoomNumber()) != room)
		{
			throw new HotelException("The room is not registered at this hotel.");
		}

		if ("OUT_OF_SERVICE".equals(room.status()))
		{
			throw new HotelException(
					"Room " + room.getRoomNumber() + " is out of service.");
		}

		for (Reservation bookedRes : resMap.values())
		{
			if (bookedRes.room() == room && isActiveRes(bookedRes)
					&& (skipResId == null
						|| bookedRes.getReservationId() != skipResId)
					&& arrival.isBefore(bookedRes.departure())
					&& bookedRes.arrival().isBefore(departure))
			{
				throw new BookingConflictException(
						"Room " + room.getRoomNumber()
						+ " is already booked during those dates.");
			}
		}
	}

	// checks if room has a reservation
	boolean hasActiveReservation(Room room)
	{
		for (Reservation res : resMap.values())
		{
			if (res.room() == room && isActiveRes(res))
			{
				return true;
			}
		}

		return false;
	}

	// checks if reservation has service orders
	boolean hasUnresolvedServiceOrders(Reservation res, Session sess) throws HotelException
	{
		for (ServiceOrder order : orderMap.values())
		{
			if (order.reservation() == res && ("OPEN".equals(order.status()) || order.getAmountDue(sess) > 0.0))
			{
				return true;
			}
		}
		return false;
	}

	//returns infomation about hotel
	@Override
	public String toString()
	{
		return "Hotel{date=" + currentDate
				+ ", guests=" + guestMap.size()
				+ ", employees=" + empMap.size()
				+ ", rooms=" + roomMap.size()
				+ ", reservations=" + resMap.size()
				+ ", serviceOrders=" + orderMap.size()
				+ ", payments=" + payMap.size()
				+ ", accounts=" + acctMap.size() + "}";
	}

	// Check if their are any issues with payments.
	private double checkPayment(Payable purchase, Session session)
			throws HotelException
	{
		checkSession(session);
		checkLimit(payMap.size());

		if (purchase instanceof Reservation)
		{
			Reservation res = (Reservation) purchase;

			if (resMap.get(res.getReservationId()) != res)
			{
				throw new HotelException(
						"The reservation is not registered at this hotel.");
			}
		}
		else if (purchase instanceof ServiceOrder)
		{
			ServiceOrder order = (ServiceOrder) purchase;

			if (orderMap.get(order.getOrderId()) != order)
			{
				throw new HotelException(
						"The service order is not registered at this hotel.");
			}
		}
		else
		{
			throw new HotelException(
					"The purchase is not registered at this hotel.");
		}

		double amountDue = purchase.getAmountDue(session);

		if (!Double.isFinite(amountDue) || amountDue <= 0.0
				|| roundMoney(amountDue) <= 0.0)
		{
			throw new HotelException(
					"The purchase has no unpaid positive balance.");
		}

		return roundMoney(amountDue);
	}
	
	//helper method to make sure arrival date is before departure date.
	private void checkDates(LocalDate arrival, LocalDate departure)
			throws HotelException
	{
		if (arrival == null || departure == null
				|| arrival.isBefore(currentDate) || !departure.isAfter(arrival))
		{
			throw new HotelException(
					"Arrival must be today or later; departure must follow it.");
		}
	}

	//helper method to check reservations
	private static boolean isActiveRes(Reservation res)
	{
		return "CONFIRMED".equals(res.status())
				|| "CHECKED_IN".equals(res.status());
	}

	//helper methods to check login information
	
	private void checkGuestEmail(String email) throws HotelException
	{
		for (Guest guest : guestMap.values())
		{
			if (guest.identityEmail().equals(email))
			{
				throw new HotelException(
						"A guest with that email already exists.");
			}
		}
	}

	private void checkUsername(String username) throws HotelException
	{
		if (acctMap.containsKey(username))
		{
			throw new HotelException("That username already exists.");
		}
	}

	private static String cleanUsername(String username) throws HotelException
	{
		if (username == null || username.trim().isEmpty())
		{
			throw new HotelException("Enter a username.");
		}

		return username.trim().toLowerCase();
	}

	private void checkSession(Session session) throws AuthorizationException
	{
		if (session == null || session != activeSession)
		{
			throw new AuthorizationException(
					"Log in to this hotel to continue.");
		}

		session.requireAuthenticated();
	}

	private void checkRole(Session session, String[] roles)
			throws AuthorizationException
	{
		checkSession(session);
		session.requireRole(roles);
	}

	private void checkLimit(int count) throws CapacityExceededException
	{
		if (count >= MAX_OBJECTS)
		{
			throw new CapacityExceededException(
					"This registry is full (maximum " + MAX_OBJECTS + ").");
		}
	}

	// rounds money to cents.  Researched isFinite method to make sure double value is a number, decimal.
	static double roundMoney(double amount) throws HotelException
	{
		if (!Double.isFinite(amount) || amount < 0.0)
		{
			throw new HotelException(
					"Enter a positive monetary amount.");
		}

		return Math.round(amount * 100.0) / 100.0;
	}
}
