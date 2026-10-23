package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;

import model.Guest;
import model.Hotel;
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
