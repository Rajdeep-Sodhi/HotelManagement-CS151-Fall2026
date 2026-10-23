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

        // Changes the password after checking that the old password is correct.
    void changePassword(String oldPassword, String newPassword)
            throws HotelException
    {
        if (!authenticate(oldPassword))
        {
            throw new HotelException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 6)
        {
            throw new HotelException(
                    "New password must be at least 6 characters.");
        }

        password = newPassword;
    }

    // Used by Hotel to store and look up accounts.
    String username()
    {
        return username;
    }

    // Returns the linked guest for guest accounts.
    Guest guest()
    {
        return guest;
    }

    // Returns the linked employee for employee and manager accounts.
    Employee employee()
    {
        return employee;
    }

    // Returns the role that was determined when the account was created.
    String role()
    {
        return role;
    }

    // Displays basic account information without showing the password.
    @Override
    public String toString()
    {
        return "Account " + username + " | Role: " + role;
    }


}
