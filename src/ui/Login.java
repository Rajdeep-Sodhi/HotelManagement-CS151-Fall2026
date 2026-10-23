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
import java.util.List;

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

    // Keep showing the appropriate menu until the user exits
    public void run() {
        while (true) {
            try {
                if (session == null) {
                    rootMenu();
                } else {
                    // Route users according to the role stored in their session
                    String role = session.getRole();
                    if ("GUEST".equals(role)) guestMenu();
                    else if ("EMPLOYEE".equals(role)) employeeMenu();
                    else managerMenu();
                }
            } catch (HotelException exception) {
                // Report a rejected operation, then let the user continue from the menu.
                System.out.println(exception.getMessage());
            }
        }
    }

    // Offer login, guest self-registration, or program exit before authentication.
    private void rootMenu() throws HotelException {
        System.out.println("\n1 - Log in\n2 - Register guest account\n0 - Exit");
        int choice = input.readInt("Choose an option: ", 0, 2);
        if (choice == 0) throw new ExitRequestedException();
        if (choice == 1) {
            session = hotel.login(input.readRequiredText("Username: "), input.readLine("Password: "));
            System.out.println("Login successful.");
            return;
        }
        // Registration creates the guest and their login account together.
        hotel.registerGuestAccount(input.readRequiredText("Guest name: "),
                input.readRequiredText("Email: "), input.readRequiredText("Phone: "),
                input.readRequiredText("Username: "), input.readLine("Password: "));
        System.out.println("Guest account created.");
    }

    // Provide profile, reservation, password, and receipt actions to the logged-in guest.
    private void guestMenu() throws HotelException {
        Guest guest = hotel.findGuest(session.getPersonId(), session);
        System.out.println("\n1 - Reservations\n2 - Profile\n3 - Change password\n"
                + "4 - Update phone number\n5 - View payment receipt\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 5);
        if (choice == 0) logout();
        else if (choice == 1) reservationMenu(guest);
        else if (choice == 2) showGuestProfile(guest);
        else if (choice == 3) changePassword();
        else if (choice == 4) {
            guest.setPhone(input.readRequiredText("New phone number: "), session);
            System.out.println("Phone number updated.");
        } else viewPaymentReceipt();
    }

    // Provide work actions and profile/registration tools to an employee.
    private void employeeMenu() throws HotelException {
        Employee employee = hotel.findEmployee(session.getPersonId(), session);
        System.out.println("\n1 - Clock in\n2 - Clock out\n3 - Check in\n4 - Check out\n"
                + "5 - Clean room\n6 - Find guest\n7 - My employee profile\n"
                + "8 - Register guest\n9 - View payment receipt\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 9);
        if (choice == 0) logout();
        else if (choice == 1) employee.clockIn(session);
        else if (choice == 2) employee.clockOut(session);
        else if (choice == 3 || choice == 4) {
            // Both actions operate on a reservation selected by its ID.
            Reservation reservation = hotel.findReservation(id("Reservation ID: "), session);
            if (choice == 3) employee.checkInGuest(reservation, session);
            else employee.checkOutGuest(reservation, session);
        } else if (choice == 5) {
            // Cleaning is one operation here: start and finish the room's cleaning cycle.
            Room room = hotel.findRoom(id("Room number: "), session);
            room.startCleaning(session);
            room.finishCleaning(session);
        } else if (choice == 6) {
            showGuestProfile(hotel.findGuest(id("Guest ID: "), session));
        } else if (choice == 7) {
            System.out.printf("Employee ID: %d%nName: %s%nEmail: %s%n",
                    employee.getEmployeeId(), employee.getName(session), employee.getEmail(session));
        } else if (choice == 8) {
            Guest guest = hotel.registerGuest(input.readRequiredText("Guest name: "),
                    input.readRequiredText("Email: "), input.readRequiredText("Phone: "), session);
            System.out.println("Guest registered. Guest ID: " + guest.getGuestId());
        } else viewPaymentReceipt();
    }

    // Provide management tools; each model operation still checks manager permissions.
    private void managerMenu() throws HotelException {
        System.out.println("\n1 - Staff actions\n2 - Add room\n3 - Hire employee\n"
                + "4 - Summary\n5 - Advance date\n6 - Create login account\n7 - Remove room\n"
                + "8 - Update room rate\n9 - Room details\n10 - View payment receipt\n"
                + "11 - Take room out of service\n12 - Return room to service\n0 - Logout");
        int choice = input.readInt("Choose an option: ", 0, 12);
        if (choice == 0) logout();
        else if (choice == 1) employeeMenu();
        else if (choice == 2) {
            Room room = hotel.addRoom(id("Room number: "), input.readInt("Capacity: ", 1, Integer.MAX_VALUE),
                    input.readMoney("Nightly rate: ", false), session);
            showRoom(room);
        } else if (choice == 3) {
            // Hiring creates the employee record; the manager can create credentials separately.
            Employee employee = hotel.hireEmployee(input.readRequiredText("Name: "),
                    input.readRequiredText("Email: "), input.readYesNo("Manager? (yes/no): "), session);
            System.out.println("Employee hired. Employee ID: " + employee.getEmployeeId());
            System.out.println("Use Create login account to give this employee access.");
        } else if (choice == 4) {
            System.out.println(hotel.summary(session));
        } else if (choice == 5) {
            hotel.advanceDate(input.readDate("New date (YYYY-MM-DD): "), session);
        } else if (choice == 6) {
            createAccount();
        } else if (choice == 7) {
            hotel.removeRoom(id("Room number: "), session);
            System.out.println("Room removed.");
        } else if (choice == 10) {
            viewPaymentReceipt();
        } else {
            // Choices 8, 9, 11, and 12 all select a room before changing or showing its state.
            Room room = hotel.findRoom(id("Room number: "), session);
            if (choice == 8) room.setNightlyRate(input.readMoney("New nightly rate: ", false), session);
            else if (choice == 11) room.takeOutOfService(session);
            else if (choice == 12) room.returnToService(session);
            showRoom(room);
        }
    }

    // List the guest's stays, search for a room, or apply an action to one selected stay.
    private void reservationMenu(Guest guest) throws HotelException {
        System.out.println("\n1 - List\n2 - Search/book\n3 - Reschedule\n4 - Cancel\n5 - Pay\n6 - Services\n0 - Back");
        int choice = input.readInt("Choose an option: ", 0, 6);
        if (choice == 0) return;
        if (choice == 1) {
            // Show the authorized details for every reservation owned by this guest.
            List<Reservation> reservations = guest.getReservations(session);
            if (reservations.isEmpty()) System.out.println("No reservations found.");
            for (Reservation reservation : reservations) showReservation(reservation);
            return;
        }
        if (choice == 2) {
            // Search first, then create a reservation only if the guest confirms a room choice.
            LocalDate arrival = input.readDate("Arrival (YYYY-MM-DD): ");
            LocalDate departure = input.readDate("Departure (YYYY-MM-DD): ");
            int partySize = input.readInt("Party size: ", 1, Integer.MAX_VALUE);
            for (Room room : hotel.searchRooms(arrival, departure, partySize, session)) {
                showRoom(room);
            }
            if (input.readYesNo("Book a room? (yes/no): ")) {
                Reservation reservation = guest.makeReservation(id("Room number: "), arrival, departure, partySize, session);
                System.out.println("Reservation created.");
                showReservation(reservation);
            }
            return;
        }
        // Remaining choices require a reservation ID before acting on that stay.
        Reservation reservation = hotel.findReservation(id("Reservation ID: "), session);
        if (choice == 3) {
            guest.rescheduleReservation(reservation, input.readDate("New arrival (YYYY-MM-DD): "),
                    input.readDate("New departure (YYYY-MM-DD): "), session);
        } else if (choice == 4) {
            guest.cancelReservation(reservation, session);
        } else if (choice == 5) {
            paymentMenu(reservation);
        } else {
            serviceMenu(reservation);
        }
    }

    // Create, edit, complete, cancel, view, or pay for orders attached to one stay.
    private void serviceMenu(Reservation reservation) throws HotelException {
        System.out.println(ServiceOrder.catalog());
        List<ServiceOrder> orders = hotel.getServiceOrders(reservation, session);
        if (orders.isEmpty()) System.out.println("No service orders for this reservation.");
        for (ServiceOrder order : orders) showServiceOrder(order);
        System.out.println("1 - Create order\n2 - Pay order\n3 - Complete order\n"
                + "4 - Add service item\n5 - Remove service item\n6 - View order\n7 - Cancel order\n0 - Back");
        int choice = input.readInt("Choose an option: ", 0, 7);
        if (choice == 0) return;
        if (choice == 1) {
            ServiceOrder order = hotel.createServiceOrder(reservation.getReservationId(), input.readRequiredText("Service: "),
                    input.readInt("Quantity: ", 1, Integer.MAX_VALUE), session);
            showServiceOrder(order);
        } else {
            ServiceOrder order = hotel.findServiceOrder(id("Order ID: "), session);
            // The guest may own several stays, so prevent using this menu to act on another stay's order.
            if (!orders.contains(order)) {
                throw new HotelException("That service order does not belong to this reservation.");
            }
            if (choice == 2) paymentMenu(order);
            else if (choice == 3) order.complete(session);
            else if (choice == 4) {
                order.addItem(input.readRequiredText("Service: "),
                        input.readInt("Quantity to add: ", 1, Integer.MAX_VALUE), session);
            } else if (choice == 5) {
                order.removeItem(input.readRequiredText("Service: "),
                        input.readInt("Quantity to remove: ", 1, Integer.MAX_VALUE), session);
            } else if (choice == 7) order.cancel(session);
            showServiceOrder(order);
        }
    }

    // Display personal details through model getters that authorize the current session.
    private void showGuestProfile(Guest guest) throws HotelException {
        System.out.printf("Guest ID: %d%nName: %s%nEmail: %s%nPhone: %s%n",
                guest.getGuestId(), guest.getName(session), guest.getEmail(session), guest.getPhone(session));
    }

    // Display room information using getters that require an active session.
    private void showRoom(Room room) throws HotelException {
        System.out.printf("Room %d | Capacity: %d | Status: %s | Nightly Rate: $%.2f%n",
                room.getRoomNumber(), room.getCapacity(session), room.getStatus(session), room.getNightlyRate(session));
    }

    // Display reservation details using getters that verify access for this session.
    private void showReservation(Reservation reservation) throws HotelException {
        System.out.printf("Reservation %d | Status: %s | Arrival: %s | Departure: %s | Party size: %d%n",
                reservation.getReservationId(), reservation.getStatus(session), reservation.getArrival(session),
                reservation.getDeparture(session), reservation.getPartySize(session));
    }

    // Show the order state and the remaining amount due to its authorized viewer.
    private void showServiceOrder(ServiceOrder order) throws HotelException {
        double due = order.getAmountDue(session);
        System.out.println(order);
        System.out.printf("Amount due: $%.2f%n", due);
    }

    // Find a payment through the hotel's ownership checks, then render its receipt.
    private void viewPaymentReceipt() throws HotelException {
        Payment payment = hotel.findPayment(id("Payment ID: "), session);
        System.out.println(payment.receipt(session));
    }

    // Attach credentials to an existing registered guest or employee chosen by the manager.
    private void createAccount() throws HotelException {
        int type = input.readInt("1 - Guest account\n2 - Employee account\n0 - Back\nChoose an option: ", 0, 2);
        if (type == 0) return;
        Guest guest = null;
        Employee employee = null;
        if (type == 1) guest = hotel.findGuest(id("Guest ID: "), session);
        else employee = hotel.findEmployee(id("Employee ID: "), session);
        hotel.createAccount(input.readRequiredText("New username: "), input.readLine("New password: "),
                guest, employee, session);
        System.out.println("Login account created.");
    }

    // Reuse the same payment flow for reservations and completed service orders.
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

    // Read a positive integer identifier for a hotel record.
    private int id(String prompt) {
        return input.readInt(prompt, 1, Integer.MAX_VALUE);
    }

    // Change the current account's password after verifying the existing password.
    private void changePassword() throws HotelException {
        session.changePassword(input.readLine("Current password: "), input.readLine("New password: "));
    }

    // Invalidate the session and return the next loop iteration to the root menu.
    private void logout() {
        session.logout();
        session = null;
    }

    // Summarize whether this menu controller currently has an authenticated session.
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
