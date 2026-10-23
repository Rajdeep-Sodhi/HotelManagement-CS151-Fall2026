# Hotel Management System

## Overview

Hotel Management System is a Java 17 console application for a single hotel. It manages guest accounts, rooms, reservations, check-in and checkout, housekeeping, service orders, and simulated cash or card payments. Data is stored in memory and resets when the application closes.

Guests can search for rooms by date and party size, reserve a room, change or cancel an unpaid reservation, order laundry or room service during a stay, and pay the full amount due. Employees handle guest arrivals and departures and prepare rooms. Managers also manage rooms and employees, view a hotel summary, and advance the demonstration date.

## Developers

Each developer owns five production Java files. The files below identify the primary owner; integration and review are shared by the team.

Yash
- `src/app/Main.java`
- `src/model/Hotel.java`
- `src/model/Guest.java`
- `src/model/CashPayment.java`
- `src/exception/BookingConflictException.java`
- `test/model/GuestReservationTest.java`
- `test/model/AuthorizationTest.java`
- `README.md`
- 'pom.xml'

Raj
- `src/model/Payable.java`
- `src/model/Payment.java`
- `src/model/CardPayment.java`
- `src/ui/Login.java`
- `src/exception/HotelException.java`
- `test/ui/LoginTest.java`
- Root-level UML diagram (`uml.png` or `uml.pdf`)

Kristy
- `src/model/Employee.java`
- `src/model/Room.java`
- `src/model/Reservation.java`
- `src/ui/InputHelper.java`
- `src/exception/CapacityExceededException.java`
- `test/model/StaffRoomTest.java`
- `test/ui/InputHelperTest.java`

Bhoomika
- `src/model/ServiceOrder.java`
- `src/model/Account.java`
- `src/model/Session.java`
- `src/ui/ExitRequestedException.java`
- `src/exception/AuthorizationException.java`
- `test/model/HotelTest.java`
- `test/model/PaymentServiceTest.java`

  ## Contributions

Yash focused on the central hotel workflow, guest functionality, cash payments, application startup, booking conflict handling, and related tests.

Raj focused on the payment abstraction and interface, card payments, the console menu system, base hotel exceptions, login tests, and the final UML diagram.

Kristy focused on employee and room operations, reservations, validated user input, capacity handling, and staff, room, and input tests.

Bhoomika focused on account and session authorization, service orders, authorization and exit exceptions, hotel registry tests, and payment and service-order tests.

All team members contributed to integration, debugging, design discussions, testing, and reviewing how the classes interact.

## Design

The source is divided into `app`, `model`, `ui`, and `exception` packages. `Main` starts the hotel and owns the only runtime `Scanner`. `Login` runs the menus, while `InputHelper` validates input and handles the `EXIT` command. `Hotel` maintains the registries and shared booking checks. Each model object handles its own behavior: for example, `Reservation` controls its check-in and checkout state, and `Room` controls housekeeping and maintenance state.

`Guest` and `Employee` are separate classes. An employee's manager flag grants management permissions; there is no generic `Person` superclass. `Account` stores credentials and derives a guest, employee, or manager role from the linked record. `Session` checks login status, role, and record ownership in the model, even when a menu option is hidden.

`Payment` is an abstract class. Its `CashPayment` and `CardPayment` subclasses validate their respective tender types. `Reservation` and `ServiceOrder` both implement `Payable`, allowing the same payment workflow to accept either purchase. Card approval is simulated; the application does not collect real card numbers. Payments must cover the full balance. Paid reservations cannot be cancelled or rescheduled, and paid service orders cannot be edited or cancelled.

Room availability uses arrival and departure dates, party size, and existing reservations. Adjacent stays are allowed, while overlapping active stays are rejected. Check-in requires payment, an available room, the scheduled arrival date, and an employee on duty. Checkout requires the departure date or later and no open service orders; the room becomes dirty until cleaned.

All fields are private. Authorized getters protect personal and billing information, setters validate changes, and `toString()` methods avoid exposing passwords. A shared maximum of 100 records per registry is enforced before creation; errors are reported through `HotelException` and the more specific `CapacityExceededException`, `BookingConflictException`, and `AuthorizationException`. The program handles invalid input without closing unexpectedly. Records are not saved between runs.

## Installation Instructions

Download the repository from `https://github.com/Rajdeep-Sodhi/CS151-Hotel_Management` or clone it:

```bash
git clone https://github.com/Rajdeep-Sodhi/CS151-Hotel_Management.git
cd HotelManagement-CS151-Fall2026
```

Install JDK 17 or newer. Maven is needed to run the JUnit 5 tests.

With Maven, run these commands from the repository root:

```bash
mvn clean test
mvn package
java -cp target/classes app.Main
```

Without Maven, compile and run the application from the `src` directory:

```bash
cd src
javac app/Main.java
java app.Main
```

The second option runs the application without the JUnit tests. On Windows Command Prompt, the same commands work; use `cd src` before `javac`.

## Usage

Start the program using either installation option. The application begins with two rooms, one manager account, one employee account, and two guest accounts. Demo logins are(username / password):

```text
manager  / manager123
employee / employee123
guest    / guest123
guest2   / guest234
```

The login role comes from the account; users cannot choose a role at the prompt. New guests can register through the root menu. A manager can add employees and assign accounts to registered guests or staff.

Follow the numbered menus. Guests can search rooms, make or change unpaid reservations, and pay by cash or simulated card. During a checked-in stay they can order laundry or room service, pay for an order, and view their own records. Employees must clock in before check-in, checkout, cleaning, or fulfilling a paid service order. Managers can also add or remove unused rooms, change room prices, place rooms out of service, add employees, view a summary, and advance the application's date. The date cannot skip the arrival of a confirmed reservation.

Enter dates as `YYYY-MM-DD`. Enter money as a nonnegative number with no more than two decimal places. Invalid entries produce an error and another prompt. Choose `0` for Back or Logout where offered. Enter `EXIT` at any prompt to leave the application. Data is reset at the next launch.

