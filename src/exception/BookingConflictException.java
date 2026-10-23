package exception;

/** Thrown when active room bookings overlap. */
public class BookingConflictException extends HotelException
{
	//Call's Hotel Exception's constructor
	public BookingConflictException(String message)
	{
		super(message);
	}

	@Override //overrident toString method that prints exception name and message
	public String toString()
	{
		return getClass().getSimpleName() + ": " + getMessage();
	}
}
