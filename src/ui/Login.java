package ui;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import model.Guest;
import model.Reservation;
import model.Room;

public class Login {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, String> accounts = new HashMap<>();
    private static final Guest guest = new Guest(1, "Guest", 18, "", "", "");
    private static final Room room = new Room(101, "Standard", 100, 2);

    public static void main(String[] args) {
        System.out.println("=== Hotel Management Login ===");
        System.out.println("Format: role, username, password (one entry per line)");
        System.out.println("Roles: guest | employee | manager");
        System.out.println("Example: guest / guest / guest123");
        accounts.put("guest:guest", "guest123");
        accounts.put("employee:employee", "employee123");
        accounts.put("manager:manager", "manager123");

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Role (guest/employee/manager): ");
            String role = scanner.nextLine().toLowerCase();
            if (!validRole(role)) {
                System.out.println("Invalid role.");
                continue;
            }
            System.out.println("1 - Login");
            System.out.println("2 - Create account");
            System.out.print("Choose an option: ");
            String option = scanner.nextLine();

            if (option.equals("2")) {
                if (createAccount(role)) openMenu(role);
                return;
            }
            if (!option.equals("1")) {
                System.out.println("Invalid option.");
                continue;
            }

            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (validLogin(role, username, password)) {
                openMenu(role);
                return;
            }
            System.out.println("Invalid login.");
        }
        System.out.println("Too many failed attempts.");
    }

    private static boolean validLogin(String role, String username, String password) {
        return password.equals(accounts.get(role + ":" + username));
    }

    private static boolean validRole(String role) {
        return role.equals("guest") || role.equals("employee") || role.equals("manager");
    }

    private static boolean createAccount(String role) {
        System.out.print("New username: ");
        String username = scanner.nextLine();
        if (accounts.containsKey(role + ":" + username)) {
            System.out.println("That account already exists.");
            return false;
        }
        System.out.print("New password: ");
        String password = scanner.nextLine();
        accounts.put(role + ":" + username, password);
        System.out.println("Account created for " + role + ".");
        return true;
    }

    private static void openMenu(String role) {
        if (role.equals("guest")) guestLogin();
        else if (role.equals("employee")) employeeLogin();
        else managerLogin();
    }

    public static void guestLogin() {
        System.out.println("Guest login successful.");
        while (true) {
            System.out.println("\nGuest options:");
            System.out.println("1 - Make reservation");
            System.out.println("2 - Check in");
            System.out.println("3 - Check out");
            System.out.println("4 - View reservation");
            System.out.println("5 - Logout");
            System.out.print("Enter one menu number: ");
            String choice = scanner.nextLine();
            if (choice.equals("1")) makeReservation();
            else if (choice.equals("2")) checkIn();
            else if (choice.equals("3")) checkOut();
            else if (choice.equals("4")) viewReservation();
            else if (choice.equals("5")) return;
            else System.out.println("Invalid choice.");
        }
    }

    public static void makeReservation() {
        if (guest.getReservation() != null) {
            System.out.println("You already have a reservation.");
            return;
        }
        System.out.println("Enter dates as whole numbers, for example: 1 then 3");
        System.out.print("Check-in day (integer): ");
        int checkIn = Integer.parseInt(scanner.nextLine());
        System.out.print("Check-out day (integer): ");
        int checkOut = Integer.parseInt(scanner.nextLine());
        try {
            guest.makeReservation(1, room, checkIn, checkOut);
            System.out.println("Reservation created.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void checkIn() {
        Reservation reservation = guest.getReservation();
        if (reservation == null) System.out.println("Make a reservation first.");
        else if (!room.isAvailable()) System.out.println("You are already checked in.");
        else {
            room.markOccupied();
            System.out.println("Guest checked in.");
        }
    }

    public static void checkOut() {
        if (guest.getReservation() == null || room.isAvailable()) {
            System.out.println("You are not checked in.");
        } else {
            room.markAvailable();
            System.out.println("Guest checked out.");
        }
    }

    public static void viewReservation() {
        if (guest.getReservation() == null) System.out.println("No reservation found.");
        else guest.getReservation().displayInfo();
    }

    public static void employeeLogin() {
        System.out.println("Employee login successful.");
        System.out.println("Employee options: 1 - View room status, 2 - Logout");
        System.out.print("Enter one menu number: ");
        if (scanner.nextLine().equals("1")) System.out.println(room);
    }

    public static void managerLogin() {
        System.out.println("Manager login successful.");
        System.out.println("Manager options: 1 - View room status, 2 - View reservation, 3 - Logout");
        System.out.print("Enter one menu number: ");
        String choice = scanner.nextLine();
        if (choice.equals("1")) System.out.println(room);
        else if (choice.equals("2")) viewReservation();
    }
}
