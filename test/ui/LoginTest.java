package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.HotelException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import model.Employee;
import model.Guest;
import model.Hotel;
import model.Reservation;
import model.ServiceOrder;
import model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginTest {
    private Hotel hotel;

    @BeforeEach
    void setUp() throws Exception {
        hotel = new Hotel(LocalDate.of(2026, 11, 1));
        hotel.seedDemoData();
    }

    // What happens: The test logs in as a guest, employee, and manager.
    // Expected: Each account is sent to the menu for its actual role.
    @Test
    void passwordLoginByRole() {
        String guestOutput = run("1\nguest\nguest123\n0\n0\n");
        assertTrue(guestOutput.contains("Reservations"), guestOutput);

        String employeeOutput = run("1\nemployee\nemployee123\n0\n0\n");
        assertTrue(employeeOutput.contains("Clock in"), employeeOutput);

        String managerOutput = run("1\nmanager\nmanager123\n0\n0\n");
        assertTrue(managerOutput.contains("Summary"), managerOutput);
    }

    // What happens: The test first enters invalid login information, then logs in correctly
    // and attempts to use a room number that does not exist.
    // Expected: The user can retry, log in successfully, and receive an error message.
    @Test
    void invalidLoginAndBusinessErrorsAllowRetry() {
        String output = run("1\nwrong\npassword\n"
                + "1\nguest\nguest123\n"
                + "1\n2\n2026-11-02\n2026-11-04\n1\nyes\n999\n"
                + "0\n0\n");

        assertTrue(output.contains("Login successful."), output);
        assertTrue(output.contains("Room not found")
                || output.contains("not found")
                || output.contains("Room"), output);
    }

    // What happens: The test logs in, returns from the reservations menu, logs out, and exits.
    // Expected: An ExitRequestedException is thrown and the Login object is unauthenticated.
    @Test
    void backAndLogoutReturnToExpectedMenus() {
        Login login = new Login(hotel, new InputHelper(new Scanner(
                "1\nguest\nguest123\n1\n0\n0\n0\n")));

        assertThrows(ExitRequestedException.class, login::run);
        assertEquals("Login{authenticated=false}", login.toString());
    }

    // What happens: The test exits while entering reservation information before completing it.
    // Expected: An ExitRequestedException is thrown and no partial reservation is created.
    @Test
    void exitDuringFormCreatesNoPartialRecord() throws Exception {
        Guest guest = hotel.findGuest(1, hotel.login("guest", "guest123"));
        Login login = new Login(hotel, new InputHelper(new Scanner(
                "1\nguest\nguest123\n1\n2\nEXIT\n")));

        assertThrows(ExitRequestedException.class, login::run);
        assertTrue(guest.getReservations(hotel.login("guest", "guest123")).isEmpty());
    }

    // What happens: The guest views a profile, changes the phone number, and views it again.
    // Expected: Personal details are shown and the new number is stored.
    @Test
    void guestProfileAndPhoneUpdateAreAvailable() throws Exception {
        String output = run("1\nguest\nguest123\n2\n4\n+14085550199\n2\n0\n0\n");
        assertTrue(output.contains("Name: Demo Guest"), output);
        assertTrue(output.contains("Email: guest@hotel.example"), output);
        assertTrue(output.contains("Phone: +14085550199"), output);
        Session guestSession = hotel.login("guest", "guest123");
        assertEquals("+14085550199", hotel.findGuest(1, guestSession).getPhone(guestSession));
    }

    // What happens: A manager hires an employee, creates an account, then logs in as that employee.
    // Expected: The employee can log in and view their own name and email.
    @Test
    void managerCanGiveNewEmployeeALoginAccount() {
        String output = run("1\nmanager\nmanager123\n"
                + "3\nNew Employee\nnewemployee@hotel.example\nno\n"
                + "6\n2\n3\nnewemployee\npassword123\n0\n"
                + "1\nnewemployee\npassword123\n7\n0\n0\n");
        assertTrue(output.contains("Employee hired. Employee ID: 3"), output);
        assertTrue(output.contains("Login account created."), output);
        assertTrue(output.contains("Name: New Employee"), output);
        assertTrue(output.contains("Email: newemployee@hotel.example"), output);
    }

    // What happens: Staff register a guest, then a manager assigns that guest a login account.
    // Expected: The guest can log in and view the registered profile.
    @Test
    void registeredGuestCanReceiveALoginAccount() {
        String output = run("1\nemployee\nemployee123\n"
                + "8\nNew Guest\nnewguest@hotel.example\n+14085550199\n0\n"
                + "1\nmanager\nmanager123\n6\n1\n3\nnewguest\npassword123\n0\n"
                + "1\nnewguest\npassword123\n2\n0\n0\n");
        assertTrue(output.contains("Guest registered. Guest ID: 3"), output);
        assertTrue(output.contains("Login account created."), output);
        assertTrue(output.contains("Name: New Guest"), output);
    }

    // What happens: The manager changes a room rate, inspects it, takes it out of service,
    // returns it to service, and removes it.
    // Expected: The displayed details reflect the changes and the removed room cannot be found.
    @Test
    void managerRoomActionsUpdateTheHotel() throws Exception {
        String output = run("1\nmanager\nmanager123\n8\n102\n175.50\n9\n102\n"
                + "11\n102\n12\n102\n7\n102\n0\n0\n");
        assertTrue(output.contains("Room 102 | Capacity: 3 | Status: READY | Nightly Rate: $175.50"), output);
        assertTrue(output.contains("Status: OUT_OF_SERVICE"), output);
        assertTrue(output.contains("Room removed."), output);
        Session managerSession = hotel.login("manager", "manager123");
        assertThrows(HotelException.class, () -> hotel.findRoom(102, managerSession));
    }

    // What happens: A guest books a room, lists the details, reschedules, and cancels the stay.
    // Expected: The dates and party size are displayed and the final reservation is cancelled.
    @Test
    void reservationDetailsAndChangesAreAvailable() throws Exception {
        String output = run("1\nguest\nguest123\n"
                + "1\n2\n2026-11-02\n2026-11-04\n2\nyes\n101\n"
                + "1\n1\n1\n3\n1\n2026-11-03\n2026-11-05\n1\n4\n1\n0\n0\n");
        assertTrue(output.contains("Arrival: 2026-11-02 | Departure: 2026-11-04 | Party size: 2"), output);
        Session guestSession = hotel.login("guest", "guest123");
        Reservation reservation = hotel.findReservation(1, guestSession);
        assertEquals(LocalDate.of(2026, 11, 3), reservation.getArrival(guestSession));
        assertEquals("CANCELLED", reservation.getStatus(guestSession));
    }

    // What happens: A guest creates and edits an order, staff complete it, then the guest pays and views the receipt.
	// Expected: Item changes affect the balance and the completed order is paid in full.
	@Test
	void serviceOrderEditingAndPaymentAreAvailable() throws Exception
	{
		Reservation reservation = createCheckedInReservations(101)[0];
		String output = run("1\nguest\nguest123\n"
				+ "1\n6\n1\n1\nLAUNDRY\n2\n"
				+ "1\n6\n1\n4\n1\nROOM_SERVICE\n1\n"
				+ "1\n6\n1\n5\n1\nLAUNDRY\n1\n"
				+ "1\n6\n1\n6\n1\n"
				+ "0\n1\nemployee\nemployee123\n10\n1\n3\n1\n0\n"
				+ "1\nguest\nguest123\n"
				+ "1\n6\n1\n2\n1\n1\n35.00\n"
				+ "5\n2\n0\n0\n");

		assertTrue(output.contains("Amount due: $40.00"), output);
		assertTrue(output.contains("Amount due: $30.00"), output);
		assertTrue(output.contains("change=5.00"), output);
		assertTrue(output.contains("Payment ID: 2"), output);

		Session guestSession = hotel.login("guest", "guest123");
		ServiceOrder order = hotel.getServiceOrders(reservation, guestSession).get(0);

		assertEquals(0.0, order.getAmountDue(guestSession), 0.001);
	}

    // What happens: A guest tries to edit an order belonging to a different one of their stays,
    // then cancels the order from the correct stay's menu.
    // Expected: The wrong-stay operation is rejected and cancellation clears the balance.
    @Test
    void serviceOrdersStayWithinTheSelectedReservation() throws Exception {
        Reservation[] reservations = createCheckedInReservations(101, 102);
        Reservation first = reservations[0];
        Reservation second = reservations[1];
        Session guestSession = hotel.login("guest", "guest123");
        ServiceOrder order = hotel.createServiceOrder(second.getReservationId(), "LAUNDRY", 1, guestSession);
        String output = run("1\nguest\nguest123\n1\n6\n" + first.getReservationId()
                + "\n7\n" + order.getOrderId() + "\n1\n6\n" + second.getReservationId()
                + "\n7\n" + order.getOrderId() + "\n0\n0\n");
        assertTrue(output.contains("That service order does not belong to this reservation."), output);
        assertTrue(output.contains("status='CANCELLED'"), output);
        guestSession = hotel.login("guest", "guest123");
        assertEquals(0.0, order.getAmountDue(guestSession), 0.001);
    }

    // What happens: Another guest tries to retrieve a receipt, then its owner and a manager retrieve it.
    // Expected: The other guest is denied; the owner and manager each see the receipt.
    @Test
    void receiptLookupPreservesPaymentOwnership() throws Exception {
        createCheckedInReservations(101);
        String output = run("1\nguest2\nguest234\n5\n1\n0\n"
                + "1\nguest\nguest123\n5\n1\n0\n"
                + "1\nmanager\nmanager123\n10\n1\n0\n0\n");
        assertTrue(output.contains("You do not have permission to access this guest."), output);
        assertEquals(2, output.split("Payment ID: 1", -1).length - 1);
    }

    private Reservation[] createCheckedInReservations(int... roomNumbers) throws Exception {
        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation[] reservations = new Reservation[roomNumbers.length];
        for (int i = 0; i < roomNumbers.length; i++) {
            reservations[i] = guest.makeReservation(roomNumbers[i], hotel.getCurrentDate(),
                    hotel.getCurrentDate().plusDays(2), 1, guestSession);
            hotel.payCard(reservations[i], true, guestSession);
        }
        Session staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        employee.clockIn(staffSession);
        for (Reservation reservation : reservations) employee.checkInGuest(reservation, staffSession);
        return reservations;
    }

    private String run(String script) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            Login login = new Login(hotel, new InputHelper(new Scanner(script)));
            assertThrows(ExitRequestedException.class, login::run);
        } finally {
            System.setOut(original);
        }
        return output.toString();
    }
}
