package model;

public class Employee extends Person {

    // Basic information for each hotel employee
    private int employeeID;
    private String position;
    private double salary;
    private String shift;
    private boolean onDuty;

    // Creates an employee with the information provided
    public Employee(String name, int age, String gender, String phoneNumber, String email,
                    int employeeID, String position, double salary, String shift) {
        super(name, age, gender, phoneNumber, email);

        if (employeeID <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive.");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }

        this.employeeID = employeeID;
        this.position = position;
        this.salary = salary;
        this.shift = shift;
        this.onDuty = false;
    }

    // Returns the employee ID
    public int getEmployeeID() {
        return employeeID;
    }

    // Returns the employee's position
    public String getPosition() {
        return position;
    }

    // Returns the employee's salary
    public double getSalary() {
        return salary;
    }

    // Returns the employee's shift
    public String getShift() {
        return shift;
    }

    // Checks if the employee is currently on duty
    public boolean isOnDuty() {
        return onDuty;
    }

    // Updates the position if one was provided
    public void setPosition(String position) {
        if (position != null && !position.trim().isEmpty()) {
            this.position = position;
        }
    }

    // Makes sure the salary cannot be negative
    public void setSalary(double salary) {
        if (salary >= 0) {
            this.salary = salary;
        }
    }

    // Updates the shift if one was provided
    public void setShift(String shift) {
        if (shift != null && !shift.trim().isEmpty()) {
            this.shift = shift;
        }
    }

    // Marks the employee as on duty at the start of a shift
    public void clockIn() {
        onDuty = true;
    }

    // Marks the employee as off duty at the end of a shift
    public void clockOut() {
        onDuty = false;
    }

    // Increases the salary by the given percentage
    public void giveRaise(double percent) {
        if (percent > 0) {
            salary += salary * percent / 100;
        }
    }

    // Checks a guest into a room if the employee is on duty and the room is free
    public boolean checkInGuest(Room room) {
        if (!onDuty || room == null || !room.isAvailable()) {
            return false;
        }

        room.markOccupied();
        return true;
    }

    // Checks a guest out of a room if the employee is on duty and the room is occupied
    public boolean checkOutGuest(Room room) {
        if (!onDuty || room == null || room.isAvailable()) {
            return false;
        }

        room.markAvailable();
        return true;
    }

    // Displays the shared information followed by the employee details
    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Employee ID: " + employeeID);
        System.out.println("Position: " + position);
        System.out.printf("Salary: $%.2f%n", salary);
        System.out.println("Shift: " + shift);
        System.out.println("On Duty: " + onDuty);
    }
}
