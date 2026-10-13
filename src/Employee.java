public class Employee extends Person {

    // Basic information for each hotel employee
    private int employeeID;
    private String position;
    private double salary;
    private String shift;
    private boolean onDuty;

    // Creates an employee with the information provided
    public Employee(String name, int age, String gender, int phoneNumber, String email,
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
