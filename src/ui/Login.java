package ui;

import exception.HotelException;
import model.Employee;
import model.Guest;
import model.Hotel;
import model.Payable;
import model.Payment;
import model.Reservation;
import model.Room;
import model.ServiceOrder;
import model.Session;

import java.time.LocalDate;

public class Login {
    private final Hotel hotel;
    private final InputHelper input;
    private Session session;

    public Login(Hotel hotel, InputHelper input) {
        if (hotel == null || input == null) {
            throw new IllegalArgumentException("Hotel and input helper are required.");
        }
        this.hotel = hotel;
        this.input = input;
    }

    public void run() {
        while (true) {
            try {
                if (session == null) {
                    rootMenu();
                } else {
                    String role = session.getRole();
                    if ("GUEST".equals(role)) guestMenu();
                    else if ("EMPLOYEE".equals(role)) employeeMenu();
                    else managerMenu();
                }
            } catch (HotelException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private void rootMenu() throws HotelException {
        System.out.println("\n1 - Log in\n2 - Register guest account\n0 - Exit");
        int choice = input.readInt("Choose an option: ", 0, 2);
        if (choice == 0) throw new ExitRequestedException();
        if (choice == 1) {
            session = hotel.login(input.readRequiredText("Username: "), input.readLine("Password: "));
            System.out.println("Login successful.");
            return;
        }
        hotel.registerGuestAccount(input.readRequiredText("Guest name: "),
                input.readRequiredText("Email: "), input.readRequiredText("Phone: "),
                input.readRequiredText("Username: "), input.readLine("Password: "));
        System.out.println("Guest account created.");
    }

    private void guestMenu() throws HotelException {
        Guest guest = hotel.findGuest(session.getPersonId(), session);
        System.out.println("\n1 - Reservations\n2 - Profile\n3 - Change password\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 3);
        if (choice == 0) logout();
        else if (choice == 1) reservationMenu(guest);
        else if (choice == 2) System.out.println(guest);
        else changePassword();
    }

    private void employeeMenu() throws HotelException {
        Employee employee = hotel.findEmployee(session.getPersonId(), session);
        System.out.println("\n1 - Clock in\n2 - Clock out\n3 - Check in\n4 - Check out\n"
                + "5 - Clean room\n6 - Find guest\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 6);
        if (choice == 0) logout();
        else if (choice == 1) employee.clockIn(session);
        else if (choice == 2) employee.clockOut(session);
        else if (choice == 3 || choice == 4) {
            Reservation reservation = hotel.findReservation(id("Reservation ID: "), session);
            if (choice == 3) employee.checkInGuest(reservation, session);
            else employee.checkOutGuest(reservation, session);
        } else if (choice == 5) {
            Room room = hotel.findRoom(id("Room number: "), session);
            room.startCleaning(session);
            room.finishCleaning(session);
        } else {
            System.out.println(hotel.findGuest(id("Guest ID: "), session));
        }
    }

    private void managerMenu() throws HotelException {
        System.out.println("\n1 - Staff actions\n2 - Add room\n3 - Hire employee\n"
                + "4 - Summary\n5 - Advance date\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 5);
        if (choice == 0) logout();
        else if (choice == 1) employeeMenu();
        else if (choice == 2) {
            hotel.addRoom(id("Room number: "), input.readInt("Capacity: ", 1, Integer.MAX_VALUE),
                    input.readMoney("Nightly rate: ", false), session);
        } else if (choice == 3) {
            hotel.hireEmployee(input.readRequiredText("Name: "),
                    input.readRequiredText("Email: "), input.readYesNo("Manager? (yes/no): "), session);
        } else if (choice == 4) {
            System.out.println(hotel.summary(session));
        } else {
            hotel.advanceDate(input.readDate("New date (YYYY-MM-DD): "), session);
        }
    }

    private void reservationMenu(Guest guest) throws HotelException {
        System.out.println("\n1 - List\n2 - Search/book\n3 - Reschedule\n4 - Cancel\n5 - Pay\n6 - Services\n0 - Back");
        int choice = input.readInt("Choose an option: ", 0, 6);
        if (choice == 0) return;
        if (choice == 1) {
            for (Reservation reservation : guest.getReservations(session)) System.out.println(reservation);
            return;
        }
        if (choice == 2) {
            LocalDate arrival = input.readDate("Arrival (YYYY-MM-DD): ");
            LocalDate departure = input.readDate("Departure (YYYY-MM-DD): ");
            int partySize = input.readInt("Party size: ", 1, Integer.MAX_VALUE);
            for (Room room : hotel.searchRooms(arrival, departure, partySize, session)) {
                System.out.println(room);
            }
            if (input.readYesNo("Book a room? (yes/no): ")) {
                guest.makeReservation(id("Room number: "), arrival, departure, partySize, session);
            }
            return;
        }
        Reservation reservation = hotel.findReservation(id("Reservation ID: "), session);
        if (choice == 3) {
            reservation.reschedule(input.readDate("New arrival (YYYY-MM-DD): "),
                    input.readDate("New departure (YYYY-MM-DD): "), session);
        } else if (choice == 4) {
            reservation.cancel(session);
        } else if (choice == 5) {
            paymentMenu(reservation);
        } else {
            serviceMenu(reservation);
        }
    }

    private void serviceMenu(Reservation reservation) throws HotelException {
        System.out.println(ServiceOrder.catalog());
        System.out.println("1 - Create order\n2 - Pay order\n3 - Complete order\n0 - Back");
        int choice = input.readInt("Choose an option: ", 0, 3);
        if (choice == 0) return;
        if (choice == 1) {
            hotel.createServiceOrder(reservation.getReservationId(), input.readRequiredText("Service: "),
                    input.readInt("Quantity: ", 1, Integer.MAX_VALUE), session);
        } else {
            ServiceOrder order = hotel.findServiceOrder(id("Order ID: "), session);
            if (choice == 2) paymentMenu(order);
            else order.complete(session);
        }
    }

    private void paymentMenu(Payable purchase) throws HotelException {
        double due = purchase.getAmountDue(session);
        if (due <= 0) return;
        System.out.printf("Amount due: $%.2f%n1 - Cash\n2 - Card\n0 - Back%n", due);
        int choice = input.readInt("Payment type: ", 0, 2);
        if (choice == 0) return;
        Payment payment;
        if (choice == 1) {
            payment = hotel.payCash(purchase, input.readMoney("Cash tendered: ", false), session);
        } else {
            payment = hotel.payCard(purchase, input.readYesNo("Card approved? (yes/no): "), session);
        }
        System.out.println(payment.receipt(session));
    }

    private int id(String prompt) {
        return input.readInt(prompt, 1, Integer.MAX_VALUE);
    }

    private void changePassword() throws HotelException {
        session.changePassword(input.readLine("Current password: "), input.readLine("New password: "));
    }

    private void logout() {
        session.logout();
        session = null;
    }

    @Override
    public String toString() {
        if (session == null) return "Login{authenticated=false}";
        try {
            return "Login{authenticated=true, role=" + session.getRole() + "}";
        } catch (HotelException exception) {
            return "Login{authenticated=false}";
        }
    }
}
