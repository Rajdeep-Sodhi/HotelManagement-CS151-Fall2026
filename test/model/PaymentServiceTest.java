package model;

import exception.HotelException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests service orders and their payment behavior. */
class PaymentServiceTest
{
    private Hotel hotel;
    private Guest guest;
    private Employee employee;
    private Session guestSession;
    private Session employeeSession;
    private Reservation reservation;

    @BeforeEach
    void setUp() throws HotelException
    {
        hotel = new Hotel(LocalDate.of(2026, 11, 1));
        hotel.seedDemoData();

        // Create and pay for a reservation as the guest.
        guestSession = hotel.login("guest", "guest123");
        guest = hotel.findGuest(1, guestSession);

        LocalDate arrival = hotel.getCurrentDate();
        LocalDate departure = arrival.plusDays(2);

        reservation = guest.makeReservation(
                101, arrival, departure, 2, guestSession);

        hotel.payCard(reservation, true, guestSession);

        // Log in as staff, clock in, and check the guest into the room.
        employeeSession = hotel.login("employee", "employee123");
        employee = hotel.findEmployee(2, employeeSession);
        employee.clockIn(employeeSession);
        employee.checkInGuest(reservation, employeeSession);

        // Log the guest back in for service order tests.
        guestSession = hotel.login("guest", "guest123");
    }

    // Checks adding and removing items and verifies the updated balance.
    @Test
    void serviceOrderItemsUpdateBalanceTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "LAUNDRY",
                2,
                guestSession);

        assertEquals(20.00, order.getAmountDue(guestSession), 0.001);

        order.addItem("ROOM_SERVICE", 1, guestSession);
        assertEquals(40.00, order.getAmountDue(guestSession), 0.001);

        order.removeItem("LAUNDRY", 1, guestSession);
        assertEquals(30.00, order.getAmountDue(guestSession), 0.001);
    }

    // Makes sure invalid service changes are rejected.
    @Test
    void invalidServiceChangesAreRejectedTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "LAUNDRY",
                1,
                guestSession);

        try
        {
            order.addItem("SPA", 1, guestSession);
            fail("An unsupported service should be rejected.");
        }
        catch (HotelException ex)
        {

        }

        try
        {
            order.removeItem("LAUNDRY", 1, guestSession);
            fail("The last item should not be removed from an order.");
        }
        catch (HotelException ex)
        {

        }

        try
        {
            order.addItem("LAUNDRY", 0, guestSession);
            fail("A zero quantity should be rejected.");
        }
        catch (HotelException ex)
        {

        }
    }

    // Checks a completed service order can be paid successfully by card.
    @Test
    void completedServiceOrderCanBePaidTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "ROOM_SERVICE",
                2,
                guestSession);

        order.complete(guestSession);

        Payment payment = hotel.payCard(order, true, guestSession);

        assertEquals(0.0, order.getAmountDue(guestSession), 0.001);
        assertTrue(payment.receipt(guestSession).contains("40.00"));
    }

    // A declined card should not mark the service order as paid.
    @Test
    void declinedCardLeavesOrderUnpaidTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "LAUNDRY",
                2,
                guestSession);

        order.complete(guestSession);

        try
        {
            hotel.payCard(order, false, guestSession);
            fail("A declined card should not complete payment.");
        }
        catch (HotelException ex)
        {

        }

        assertEquals(20.00, order.getAmountDue(guestSession), 0.001);
    }

    // Checks cash payments and verifies change on the receipt.
    @Test
    void cashPaymentReturnsCorrectChangeTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "ROOM_SERVICE",
                1,
                guestSession);

        order.complete(guestSession);

        Payment payment = hotel.payCash(order, 25.00, guestSession);
        String receipt = payment.receipt(guestSession);

        assertTrue(receipt.contains("20.00"));
        assertTrue(receipt.contains("change=5.00"));
    }

    // Makes sure an order cannot be paid before it is completed.
    @Test
    void openServiceOrderCannotBePaidTest() throws HotelException
    {
        ServiceOrder order = hotel.createServiceOrder(
                reservation.getReservationId(),
                "LAUNDRY",
                1,
                guestSession);

        try
        {
            hotel.payCard(order, true, guestSession);
            fail("An open service order should not be paid.");
        }
        catch (HotelException ex)
        {

        }

        assertEquals(10.00, order.getAmountDue(guestSession), 0.001);
    }
}
