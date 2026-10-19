package model;

import java.util.ArrayList;
import java.util.List;

public class Manager extends Employee {

    // Basic information for each hotel manager
    private String department;
    private List<Employee> employees;

    // Creates a manager with the information provided
    public Manager(String name, int age, String gender, String phoneNumber, String email,
                   int employeeID, double salary, String shift, String department) {
        super(name, age, gender, phoneNumber, email, employeeID, "Manager", salary, shift);
        this.department = department;
        this.employees = new ArrayList<>();
    }

    // Returns the department the manager is in charge of
    public String getDepartment() {
        return department;
    }

    // Updates the department if one was provided
    public void setDepartment(String department) {
        if (department != null && !department.trim().isEmpty()) {
            this.department = department;
        }
    }

    // Returns the number of employees the manager supervises
    public int getEmployeeCount() {
        return employees.size();
    }

    // Finds a supervised employee by ID, or returns null if there is none
    public Employee findEmployee(int employeeID) {
        for (Employee employee : employees) {
            if (employee.getEmployeeID() == employeeID) {
                return employee;
            }
        }

        return null;
    }

    // Adds an employee to the manager's team if they are not already on it
    public boolean addEmployee(Employee employee) {
        if (employee == null || employee == this
                || findEmployee(employee.getEmployeeID()) != null) {
            return false;
        }

        employees.add(employee);
        return true;
    }

    // Removes the employee with the given ID from the manager's team
    public boolean removeEmployee(int employeeID) {
        Employee employee = findEmployee(employeeID);
        if (employee == null) {
            return false;
        }

        employees.remove(employee);
        return true;
    }

    // Gives a raise to an employee on the manager's team
    public boolean approveRaise(int employeeID, double percent) {
        Employee employee = findEmployee(employeeID);
        if (employee == null || percent <= 0) {
            return false;
        }

        employee.giveRaise(percent);
        return true;
    }

    // Adds up the amounts of the payments to get the total revenue
    public double checkRevenue(List<Payment> payments) {
        double revenue = 0;
        if (payments == null) {
            return revenue;
        }

        for (Payment payment : payments) {
            revenue += payment.getAmount();
        }

        return revenue;
    }

    // Displays the employee information followed by the manager details
    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Department: " + department);
        System.out.println("Employees Supervised: " + employees.size());
    }
}
