package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import exception.AuthorizationException;
import exception.HotelException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffRoomTest
{
    // Tests employee duty, stay transitions, housekeeping, and maintenance.
    private Hotel hotel; // Fresh hotel with staff, rooms, and stay fixtures.
    private Session staffSession; // Authenticated ordinary employee session.
    private Session managerSession; // Manager session used when required; log in again when switching roles.

    /* Create the staff, room, and guest fixtures; obtain sessions through login. */
    @BeforeEach
    void setUp() throws HotelException
    {
        hotel = new Hotel(LocalDate.of(2026, 11, 1));
        hotel.seedDemoData();

        managerSession = hotel.login("manager", "manager123");
        staffSession = hotel.login("employee", "employee123");
    }

    /* Check clock-in/out, repeated transitions, and attempts to change another employee's
       duty. */
    @Test
    void dutyChangesRequireOwnSession() throws HotelException
    {
        Employee employee = hotel.findEmployee(2, staffSession);

        // Cannot clock out before clocking in
        try
        {
            employee.clockOut(staffSession);
            fail("An off-duty employee should not clock out.");
        }
        catch (HotelException ex)
        {

        }

        employee.clockIn(staffSession);
        assertTrue(employee.isOnDuty());

        // Cannot clock in twice
        try
        {
            employee.clockIn(staffSession);
            fail("An on-duty employee should not clock in again.");
        }
        catch (HotelException ex)
        {

        }

        // A manager cannot change another employee's duty
        managerSession = hotel.login("manager", "manager123");
        try
        {
            employee.clockOut(managerSession);
            fail("A manager should not clock out another employee.");
        }
        catch (AuthorizationException ex)
        {

        }
        assertTrue(employee.isOnDuty());

        // The old staff session was logged out by the manager login
        try
        {
            employee.clockOut(staffSession);
            fail("A logged-out session should not change duty.");
        }
        catch (AuthorizationException ex)
        {

        }

        staffSession = hotel.login("employee", "employee123");
        employee.clockOut(staffSession);
        assertFalse(employee.isOnDuty());
    }

    /* Reject off-duty, unpaid, wrong-day, or unready arrivals; verify a valid arrival
       changes both stay and room. */
    @Test
    void checkInRequiresDutyPaymentDateAndReadyRoom() throws HotelException
    {
        LocalDate day0 = hotel.getCurrentDate();

        // Guest 1 books room 101 today and room 102 tomorrow
        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation today = guest.makeReservation(101, day0, day0.plusDays(2), 1, guestSession);
        Reservation tomorrow = guest.makeReservation(102, day0.plusDays(1), day0.plusDays(3), 1, guestSession);

        // Guest 2 books room 102 for tonight, leaving tomorrow
        Session secondGuestSession = hotel.login("guest2", "guest234");
        Guest secondGuest = hotel.findGuest(2, secondGuestSession);
        Reservation tonight = secondGuest.makeReservation(102, day0, day0.plusDays(1), 1, secondGuestSession);

        staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        Room room101 = hotel.findRoom(101, staffSession);
        Room room102 = hotel.findRoom(102, staffSession);

        // Off duty
        try
        {
            employee.checkInGuest(today, staffSession);
            fail("An off-duty employee should not check in a guest.");
        }
        catch (HotelException ex)
        {

        }

        // Unpaid
        employee.clockIn(staffSession);
        try
        {
            employee.checkInGuest(today, staffSession);
            fail("An unpaid stay should not check in.");
        }
        catch (HotelException ex)
        {

        }
        assertEquals("CONFIRMED", today.status());

        // Pay for all three stays
        guestSession = hotel.login("guest", "guest123");
        hotel.payCard(today, true, guestSession);
        hotel.payCard(tomorrow, true, guestSession);
        secondGuestSession = hotel.login("guest2", "guest234");
        hotel.payCard(tonight, true, secondGuestSession);

        // Wrong day: tomorrow's stay cannot check in today
        staffSession = hotel.login("employee", "employee123");
        try
        {
            employee.checkInGuest(tomorrow, staffSession);
            fail("A stay should not check in before its arrival date.");
        }
        catch (HotelException ex)
        {

        }

        // A valid arrival changes both the stay and the room
        employee.checkInGuest(today, staffSession);
        assertEquals("CHECKED_IN", today.status());
        assertEquals("OCCUPIED", room101.status());

        // Tonight's guest in room 102 checks in today and out tomorrow, leaving the room DIRTY
        employee.checkInGuest(tonight, staffSession);
        managerSession = hotel.login("manager", "manager123");
        hotel.advanceDate(day0.plusDays(1), managerSession);
        staffSession = hotel.login("employee", "employee123");
        employee.checkOutGuest(tonight, staffSession);
        assertEquals("DIRTY", room102.status());

        // Unready room: tomorrow's stay cannot check in to a DIRTY room
        try
        {
            employee.checkInGuest(tomorrow, staffSession);
            fail("A guest should not check in to a DIRTY room.");
        }
        catch (HotelException ex)
        {

        }
        assertEquals("CONFIRMED", tomorrow.status());
        assertEquals("DIRTY", room102.status());
    }

    /* Reject early checkout and OPEN services; verify a valid departure changes the booking
       and room. */
    @Test
    void checkoutRequiresDepartureAndResolvedServices() throws HotelException
    {
        LocalDate day0 = hotel.getCurrentDate();
        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation stay = guest.makeReservation(101, day0, day0.plusDays(1), 1, guestSession);
        hotel.payCard(stay, true, guestSession);

        staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        Room room = hotel.findRoom(101, staffSession);
        employee.clockIn(staffSession);
        employee.checkInGuest(stay, staffSession);

        // Early checkout, before the departure date
        try
        {
            employee.checkOutGuest(stay, staffSession);
            fail("A stay should not check out before its departure date.");
        }
        catch (HotelException ex)
        {

        }

        // The guest orders laundry, leaving an OPEN service order
        guestSession = hotel.login("guest", "guest123");
        ServiceOrder order = hotel.createServiceOrder(stay.getReservationId(), "laundry", 1, guestSession);

        managerSession = hotel.login("manager", "manager123");
        hotel.advanceDate(day0.plusDays(1), managerSession);

        // OPEN service order blocks checkout
        staffSession = hotel.login("employee", "employee123");
        try
        {
            employee.checkOutGuest(stay, staffSession);
            fail("A stay with an OPEN service order should not check out.");
        }
        catch (HotelException ex)
        {

        }
        assertEquals("CHECKED_IN", stay.status());

        // After the order is resolved, a valid departure changes both the booking and the room
        order.cancel(staffSession);
        employee.checkOutGuest(stay, staffSession);
        assertEquals("CHECKED_OUT", stay.status());
        assertEquals("DIRTY", room.status());
    }

    /* Verify checkout creates DIRTY, then cleaning follows DIRTY to CLEANING to READY. */
    @Test
    void housekeepingRestoresRoomReadiness() throws HotelException
    {
        LocalDate day0 = hotel.getCurrentDate();
        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation stay = guest.makeReservation(101, day0, day0.plusDays(1), 1, guestSession);
        hotel.payCard(stay, true, guestSession);

        staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        Room room = hotel.findRoom(101, staffSession);
        employee.clockIn(staffSession);
        employee.checkInGuest(stay, staffSession);

        // An OCCUPIED room cannot be cleaned
        try
        {
            room.startCleaning(staffSession);
            fail("An OCCUPIED room should not be cleaned.");
        }
        catch (HotelException ex)
        {

        }

        managerSession = hotel.login("manager", "manager123");
        hotel.advanceDate(day0.plusDays(1), managerSession);
        staffSession = hotel.login("employee", "employee123");
        employee.checkOutGuest(stay, staffSession);

        // Checkout creates DIRTY
        assertEquals("DIRTY", room.status());

        // Cannot finish cleaning before starting
        try
        {
            room.finishCleaning(staffSession);
            fail("Cleaning should not finish before it starts.");
        }
        catch (HotelException ex)
        {

        }

        // Off-duty staff cannot clean
        employee.clockOut(staffSession);
        try
        {
            room.startCleaning(staffSession);
            fail("Off-duty staff should not clean rooms.");
        }
        catch (HotelException ex)
        {

        }
        employee.clockIn(staffSession);

        // DIRTY to CLEANING to READY
        room.startCleaning(staffSession);
        assertEquals("CLEANING", room.status());
        try
        {
            room.startCleaning(staffSession);
            fail("Cleaning should not start twice.");
        }
        catch (HotelException ex)
        {

        }
        room.finishCleaning(staffSession);
        assertEquals("READY", room.status());

        // Guests cannot clean rooms
        guestSession = hotel.login("guest", "guest123");
        try
        {
            room.startCleaning(guestSession);
            fail("Guests should not clean rooms.");
        }
        catch (AuthorizationException ex)
        {

        }
    }

    /* Check permission/booking guards and restoration of the prior READY or DIRTY state. */
    @Test
    void maintenanceRequiresManagerAndNoActiveBooking() throws HotelException
    {
        LocalDate day0 = hotel.getCurrentDate();
        Room room101 = hotel.findRoom(101, staffSession);
        Room room102 = hotel.findRoom(102, staffSession);

        // Ordinary staff cannot take a room out of service
        try
        {
            room102.takeOutOfService(staffSession);
            fail("Ordinary staff should not take rooms out of service.");
        }
        catch (AuthorizationException ex)
        {

        }
        assertEquals("READY", room102.status());

        // A manager can, and returning it restores READY
        managerSession = hotel.login("manager", "manager123");
        room102.takeOutOfService(managerSession);
        assertEquals("OUT_OF_SERVICE", room102.status());
        try
        {
            room102.takeOutOfService(managerSession);
            fail("A room already out of service should not be taken out again.");
        }
        catch (HotelException ex)
        {

        }
        room102.returnToService(managerSession);
        assertEquals("READY", room102.status());

        // A room with an active booking cannot be taken out of service
        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation stay = guest.makeReservation(101, day0, day0.plusDays(1), 1, guestSession);
        hotel.payCard(stay, true, guestSession);
        managerSession = hotel.login("manager", "manager123");
        try
        {
            room101.takeOutOfService(managerSession);
            fail("A room with an active booking should not be taken out of service.");
        }
        catch (HotelException ex)
        {

        }
        assertEquals("READY", room101.status());

        // After the stay ends the room is DIRTY; maintenance restores DIRTY, not READY
        staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        employee.clockIn(staffSession);
        employee.checkInGuest(stay, staffSession);
        managerSession = hotel.login("manager", "manager123");
        hotel.advanceDate(day0.plusDays(1), managerSession);
        staffSession = hotel.login("employee", "employee123");
        employee.checkOutGuest(stay, staffSession);

        managerSession = hotel.login("manager", "manager123");
        room101.takeOutOfService(managerSession);
        assertEquals("OUT_OF_SERVICE", room101.status());
        room101.returnToService(managerSession);
        assertEquals("DIRTY", room101.status());
    }

    /* Reject backwards/skipped-arrival dates; allow exact arrival/departure dates without
       automatic check-in/out. */
    @Test
    void dateAdvancementDoesNotSkipConfirmedArrivals() throws HotelException
    {
        LocalDate day0 = hotel.getCurrentDate();
        LocalDate arrival = day0.plusDays(2);
        LocalDate departure = day0.plusDays(4);

        Session guestSession = hotel.login("guest", "guest123");
        Guest guest = hotel.findGuest(1, guestSession);
        Reservation stay = guest.makeReservation(101, arrival, departure, 1, guestSession);
        hotel.payCard(stay, true, guestSession);

        // Ordinary staff cannot change the date
        staffSession = hotel.login("employee", "employee123");
        try
        {
            hotel.advanceDate(day0.plusDays(1), staffSession);
            fail("Ordinary staff should not advance the date.");
        }
        catch (AuthorizationException ex)
        {

        }

        // Backwards, unchanged, and skipped-arrival dates are rejected
        managerSession = hotel.login("manager", "manager123");
        try
        {
            hotel.advanceDate(day0.minusDays(1), managerSession);
            fail("The date should not move backwards.");
        }
        catch (HotelException ex)
        {

        }
        try
        {
            hotel.advanceDate(day0, managerSession);
            fail("The date should not stay the same.");
        }
        catch (HotelException ex)
        {

        }
        try
        {
            hotel.advanceDate(arrival.plusDays(1), managerSession);
            fail("The date should not skip a confirmed arrival.");
        }
        catch (HotelException ex)
        {

        }
        assertEquals(day0, hotel.getCurrentDate());

        // The exact arrival date is allowed and does not check the guest in
        hotel.advanceDate(arrival, managerSession);
        assertEquals(arrival, hotel.getCurrentDate());
        assertEquals("CONFIRMED", stay.status());

        // The date cannot move past a confirmed arrival
        try
        {
            hotel.advanceDate(arrival.plusDays(1), managerSession);
            fail("The date should not move past a confirmed arrival.");
        }
        catch (HotelException ex)
        {

        }

        // After check-in, the exact departure date is allowed and does not check the guest out
        staffSession = hotel.login("employee", "employee123");
        Employee employee = hotel.findEmployee(2, staffSession);
        employee.clockIn(staffSession);
        employee.checkInGuest(stay, staffSession);

        managerSession = hotel.login("manager", "manager123");
        hotel.advanceDate(departure, managerSession);
        assertEquals(departure, hotel.getCurrentDate());
        assertEquals("CHECKED_IN", stay.status());
    }
}
