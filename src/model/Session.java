package model;

import exception.AuthorizationException;
import exception.HotelException;

public class Session
{

    // Stores the account that is logged in and whether the session is active.
    private final Account account;
    private boolean active;

    // Creates an active session after a successful login.
    Session(Account account)
    {
        if (account == null)
        {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        this.account = account;
        active = true;
    }

    // Returns the role of the logged in user.
    public String getRole() throws AuthorizationException
    {
        requireAuthenticated();
        return account.role();
    }

    // Returns the ID of the guest or employee using this session.
    public int getPersonId() throws AuthorizationException
    {
        requireAuthenticated();

        if (account.guest() != null)
        {
            return account.guest().getGuestId();
        }

        return account.employee().getEmployeeId();
    }

    // Allows the logged in user to change their password.
    public void changePassword(String oldPassword, String newPassword)
            throws HotelException
    {
        requireAuthenticated();
        account.changePassword(oldPassword, newPassword);
    }

    // Ends the session so it cannot be used again.
    public void logout()
    {
        active = false;
    }

    // Makes sure a logged out session cannot authorize another action.
    void requireAuthenticated() throws AuthorizationException
    {
        if (!active)
        {
            throw new AuthorizationException(
                    "This session is no longer active.");
        }
    }

        // The ... lets us pass one or more allowed roles.
    void requireRole(String... roles) throws AuthorizationException
    {
        requireAuthenticated();

        if (roles == null || roles.length == 0)
        {
            throw new AuthorizationException(
                    "No roles were provided for this action.");
        }

        String currentRole = account.role();

        for (String role : roles)
        {
            if (currentRole.equals(role))
            {
                return;
            }
        }

        throw new AuthorizationException(
                "You do not have permission to perform this action.");
    }

    // Guests can access their own information, while staff can help guests.
    void requireGuestAccess(Guest guest) throws AuthorizationException
    {
        requireAuthenticated();

        if (guest == null)
        {
            throw new AuthorizationException("Guest access is invalid.");
        }

        if (account.guest() == guest
                || "EMPLOYEE".equals(account.role())
                || "MANAGER".equals(account.role()))
        {
            return;
        }

        throw new AuthorizationException(
                "You do not have permission to access this guest.");
    }

    // Employees can access themselves, while managers can access employees.
    void requireEmployeeAccess(Employee employee)
            throws AuthorizationException
    {
        requireAuthenticated();

        if (employee == null)
        {
            throw new AuthorizationException("Employee access is invalid.");
        }

        if (account.employee() == employee
                || "MANAGER".equals(account.role()))
        {
            return;
        }

        throw new AuthorizationException(
                "You do not have permission to access this employee.");
    }

    // Staff must be logged in and on duty for staff-only actions.
    Employee requireOnDutyStaff() throws HotelException
    {
        requireAuthenticated();

        Employee employee = account.employee();

        if (employee == null)
        {
            throw new AuthorizationException(
                    "Only hotel staff can perform this action.");
        }

        if (!employee.isOnDuty())
        {
            throw new AuthorizationException(
                    "You must be on duty to perform this action.");
        }

        return employee;
    }

    @Override
    public String toString()
    {
        if (!active)
        {
            return "Session{active=false}";
        }

        return "Session{active=true, role='" + account.role() + "'}";
    }
}