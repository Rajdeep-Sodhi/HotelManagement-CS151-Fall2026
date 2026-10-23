package model;

import exception.HotelException;

public class Account
{

    // Stores login information and links the account to one person.
    private final String username;
    private String password;
    private final Guest guest;
    private final Employee employee;
    private final String role;

    /* Validate the username and password, make sure the account belongs to
       exactly one person, and determine their role. */
    Account(String username, String password, Guest guest, Employee employee)
            throws HotelException
    {
        if (username == null || username.trim().isEmpty())
        {
            throw new HotelException("Username cannot be blank.");
        }

        if (password == null || password.length() < 6)
        {
            throw new HotelException(
                    "Password must be at least 6 characters.");
        }

        if ((guest == null) == (employee == null))
        {
            throw new HotelException(
                    "Account must belong to one guest or employee.");
        }

        this.username = username.trim().toLowerCase();
        this.password = password;
        this.guest = guest;
        this.employee = employee;

        // The role comes from the linked person, not from user input.
        if (guest != null)
        {
            role = "GUEST";
        }
        else if (employee.isManager())
        {
            role = "MANAGER";
        }
        else
        {
            role = "EMPLOYEE";
        }
    }

    // Checks the password without exposing the stored password.
    boolean authenticate(String password)
    {
        return password != null && this.password.equals(password);
    }
}
