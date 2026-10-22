HOTEL MANAGEMENT SYSTEM

OVERVIEW

The Hotel Management System is a Java console application for managing one hotel. Guests can manage reservations and service orders. Employees can handle guest arrivals, departures, and room preparation. Managers can oversee rooms and staff, process authorized refunds, and view reports.

Cash and card payments are simulated. Application data is stored in memory and resets when the program exits.

DEVELOPERS

Yash:
-src/app/Main.java
-src/model/Hotel.java
-src/model/Guest.java
-src/model/Reservation.java
-src/model/Session.java
-src/ui/Login.java
-src/exception/BookingConflictException.java
-src/exception/AuthorizationException.java
-test/model/HotelTest.java
-test/ui/LoginTest.java
-README

Raj:
-src/model/Payment.java
-src/model/CardPayment.java
-src/model/Account.java
-src/ui/InputHelper.java
-src/exception/HotelException.java
-src/exception/InvalidOperationException.java
-test/model/PaymentServiceTest.java
-test/model/AuthorizationReportTest.java
-test/ui/InputHelperTest.java
-UML diagram

Christy
-src/model/Employee.java
-src/model/Room.java
-src/model/Payable.java
-src/exception/DuplicateEntityException.java
-src/exception/CapacityExceededException.java
-test/model/StaffRoomTest.java

Bhoomika:
-src/model/Manager.java
-src/model/ServiceOrder.java
-src/model/CashPayment.java
-src/ui/ExitRequestedException.java
-src/exception/PaymentDeclinedException.java
-test/model/GuestReservationTest.java


DESIGN

The source code is organized into four packages.

    app 		- Starts the program and connects the user interface to the model.

    model 		- Contains the hotel data and business rules. Hotel coordinates guests,
					employees, rooms, reservations, service orders, accounts, sessions, and
					payments.

    ui 			- Handles login, menus, and validated console input. The program creates
					one Scanner and shares it through the input helper.

    exception 	- Contains specific errors for duplicate records, capacity limits, booking
					conflicts, invalid operations, authorization failures, and declined
					payments.

Guest and Employee are separate classes. They do not inherit from a general Person
class. Manager extends Employee because managers share employee responsibilities
and have additional management duties.

Payment is an abstract class with CashPayment and CardPayment subclasses. Each
subclass provides its own tender validation and refund behavior. The Payable
interface is implemented by Reservation and ServiceOrder so the same payment
workflow can handle either type of balance.

Hotel coordinates the system's operations and checks role and ownership permissions
through the active Session. It enforces a maximum of 100 managed instances of each
class and coordinates changes so failed operations do not leave partial updates.

The project design requires private fields, validated setters, permission-gated
sensitive getters, and an accurate toString() method for each class. Status and role
values are validated strings; the project does not use enums. Card processing is
simulated and must not collect real card details.

Hotel, Guest, Employee, Manager, Room, Reservation, and ServiceOrder are designed
to have state-changing business methods that enforce the rules for their
responsibilities.


INSTALLATION INSTRUCTIONS

Download or clone the GitHub repository. Install JDK 17 or newer.

Option 1: Use Maven to build the project and run the JUnit tests

Open a terminal in the repository's root folder and run:

    mvn clean test
    mvn package

Run the application from the repository root:

    java -cp target/classes app.Main

The Maven configuration must use src as the source directory and test as the test
directory, and include JUnit 5.

Option 2: Compile and run the application without Maven

Open a terminal in the repository's root folder. Change into the src folder, then
compile and run the application:

    cd src
    javac app/Main.java
    java app.Main

Java finds the other package files relative to the current source folder. Since
Main belongs to the app package, run it by its package and class name: app.Main.

This option runs the application but does not run the JUnit tests. To run the tests
without Maven, use an IDE configured with JUnit 5.

The uploaded repository snapshot currently predates src/app/Main.java, the Maven
configuration, and the test tree. These commands apply after the planned files and
build configuration are added.


USAGE

1. Start the program and log in with an account and password. The account
   determines the user's role; users cannot freely select a role at login.

2. Guests can find rooms, create or change reservations, place service orders, and
   make payments.

3. Employees can perform assigned operational tasks, such as check-in, check-out,
   and room cleaning.

4. Managers can manage rooms and staff, handle permitted refunds, and view hotel
   reports.

5. Choose cash or simulated card when paying an eligible reservation or service
   order. No real transaction is sent to a payment provider.

6. Follow each prompt's accepted format. Invalid input should show an error and
   prompt again. Use Back or Logout to navigate. Enter EXIT at a prompt to leave
   the application cleanly.

The current prototype's demonstration logins are:

    Guest
        Username: guest
        Password: guest123

    Employee
        Username: employee
        Password: employee123

    Manager
        Username: manager
        Password: manager123


CONTRIBUTIONS

The work is assigned by feature area so the core model and application flow can be
developed alongside payments, staff operations, and tests.

Yash is assigned the application entry point, hotel coordination, guest
reservations, login and session flow, related exceptions and tests, and this
README.

Raj is assigned payment processing, account handling, shared input, related
exceptions and tests, and the UML diagram.

Christy is assigned employee and room behavior, the Payable interface, related
exceptions, and staff and room tests.

Bhoomika is assigned manager and service-order behavior, cash payments, exit and
payment-decline handling, and guest and reservation tests.
