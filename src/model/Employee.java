package model;

import exception.AuthorizationException;
import exception.HotelException;

public class Employee 
{

    // Represents staff duty and arrival/departure responsibilities.
    private final int employeeId; // Immutable employee identifier.
    private final String name; // Validated, trimmed employee name.
    private final String email; // Validated, normalized email; unique among registered employees.
    private final boolean manager; // Immutable manager permission flag; no separate Manager or Person class.
    private boolean onDuty; // Whether this employee has clocked in.


    /* Validate positive ID, trimmed name, and normalized email; capture the manager flag and
       begin off duty. */
    Employee(int id, String name, String email, boolean manager) throws HotelException
    {
        if (id <= 0)
        {
            throw new HotelException("Employee ID must be positive.");
        }
        if (name == null || name.trim().isEmpty())
        {
            throw new HotelException("Employee name cannot be blank.");
        }
        if (email == null)
        {
            throw new HotelException("Employee email is required.");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!cleanEmail.matches("[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}"))
        {
            throw new HotelException("Enter a valid employee email address.");
        }
        this.employeeId = id;
        this.name = name.trim();
        this.email = cleanEmail;
        this.manager = manager;
        this.onDuty = false;
    }

    /* Require this employee's own active session and reject an already on-duty employee
       before setting onDuty to true. */
    public void clockIn(Session actor) throws HotelException
    {
        if (actor == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        if (actor.getRole().equals("GUEST") || actor.getPersonId() != employeeId)
        {
            throw new AuthorizationException("Employees can only clock themselves in.");
        }
        if (onDuty)
        {
            throw new HotelException("Employee " + employeeId + " is already clocked in.");
        }
        onDuty = true;
    }

    /* Require this employee's own active session and reject an already off-duty employee
       before setting onDuty to false. */
    public void clockOut(Session actor) throws HotelException
    {
        if (actor == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        if (actor.getRole().equals("GUEST") || actor.getPersonId() != employeeId)
        {
            throw new AuthorizationException("Employees can only clock themselves out.");
        }
        if (!onDuty)
        {
            throw new HotelException("Employee " + employeeId + " is already clocked out.");
        }
        onDuty = false;
    }

    /* Require this employee's own active session and on-duty status, then call the
       reservation's validated check-in transition. */
    public void checkInGuest(Reservation reservation, Session actor) throws HotelException
    {
        if (actor == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        if (actor.getRole().equals("GUEST") || actor.getPersonId() != employeeId)
        {
            throw new AuthorizationException("Employees can only check in guests from their own account.");
        }
        if (!onDuty)
        {
            throw new HotelException("Employee " + employeeId + " must clock in before checking in a guest.");
        }
        if (reservation == null)
        {
            throw new HotelException("A reservation is required.");
        }
        reservation.checkIn(actor);
    }

    /* Require this employee's own active session and on-duty status, then call the
       reservation's validated checkout transition. */
    public void checkOutGuest(Reservation reservation, Session actor) throws HotelException
    {
        if (actor == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        if (actor.getRole().equals("GUEST") || actor.getPersonId() != employeeId)
        {
            throw new AuthorizationException("Employees can only check out guests from their own account.");
        }
        if (!onDuty)
        {
            throw new HotelException("Employee " + employeeId + " must clock in before checking out a guest.");
        }
        if (reservation == null)
        {
            throw new HotelException("A reservation is required.");
        }
        reservation.checkOut(actor);
    }

    /* Return the name only to this employee or a manager. */
    public String getName(Session viewer) throws HotelException
    {
        if (viewer == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        viewer.requireEmployeeAccess(this);
        return name;
    }

    /* Return the email only to this employee or a manager. */
    public String getEmail(Session viewer) throws HotelException
    {
        if (viewer == null)
        {
            throw new AuthorizationException("You must be logged in.");
        }
        viewer.requireEmployeeAccess(this);
        return email;
    }

    /* Return the immutable, non-sensitive lookup ID. */
    public int getEmployeeId()
    {
        return employeeId;
    }

    /* Package-private role helper for Account and Session; no public role setter. */
    boolean isManager()
    {
        return manager;
    }

    /* Package-private duty helper for authorized staff operations. */
    boolean isOnDuty()
    {
        return onDuty;
    }

    /* Package-private normalized email used by Hotel's duplicate checks. */
    String identityEmail()
    {
        return email;
    }

    /* Display employee ID, manager flag, and duty state; omit private contact details. */
    @Override
    public String toString()
    {
        return "Employee " + employeeId + " | Manager: " + manager + " | On Duty: " + onDuty;
    }
}
