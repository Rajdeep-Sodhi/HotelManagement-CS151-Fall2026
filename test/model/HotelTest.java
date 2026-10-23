package model;

import exception.CapacityExceededException;
import exception.HotelException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests hotel registry limits and duplicate protection. */
class HotelTest
{
    private Hotel hotel;
    private Session managerSession;

    @BeforeEach
    void setUp() throws HotelException
    {
        hotel = new Hotel(LocalDate.of(2026, 11, 1));
        hotel.seedDemoData();
        managerSession = hotel.login("manager", "manager123");
    }

    // Makes sure the room registry accepts 100 rooms but rejects the 101st.
    @Test
    void roomRegistryStopsAtOneHundredTest() throws HotelException
    {
        // Demo data already contains two rooms, so add 98 more.
        for (int roomNum = 103; roomNum <= 200; roomNum++)
        {
            hotel.addRoom(roomNum, 2, 100.00, managerSession);
        }

        try
        {
            hotel.addRoom(201, 2, 100.00, managerSession);
            fail("The 101st room should be rejected.");
        }
        catch (CapacityExceededException ex)
        {

        }
    }

    // Makes sure duplicate room numbers are not allowed.
    @Test
    void duplicateRoomNumberIsRejectedTest() throws HotelException
    {
        try
        {
            hotel.addRoom(101, 2, 120.00, managerSession);
            fail("A duplicate room number should be rejected.");
        }
        catch (HotelException ex)
        {

        }
    }

    // Makes sure guest emails cannot be registered more than once.
    @Test
    void duplicateGuestEmailIsRejectedTest() throws HotelException
    {
        try
        {
            hotel.registerGuest(
                    "Another Guest",
                    "guest@hotel.example",
                    "+14085550199",
                    managerSession);

            fail("A duplicate guest email should be rejected.");
        }
        catch (HotelException ex)
        {

        }
    }

    // A failed account registration should not leave a guest behind.
    @Test
    void failedGuestAccountRegistrationIsAtomicTest() throws HotelException
    {
        try
        {
            hotel.registerGuestAccount(
                    "New Guest",
                    "newguest@hotel.example",
                    "+14085550155",
                    "guest",
                    "password123");

            fail("A duplicate username should be rejected.");
        }
        catch (HotelException ex)
        {

        }

        // If the failed guest was not stored, the next guest should still get ID 3.
        Guest guest = hotel.registerGuest(
                "Valid Guest",
                "validguest@hotel.example",
                "+14085550156",
                managerSession);

        assertEquals(3, guest.getGuestId());
    }
}
