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
}