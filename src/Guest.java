public class Guest extends Person
{
	private int guestId;
	private Reservation reservation;
	
	public Guest(int guestIdIn, String nameIn, int ageIn, String genderIn, int phoneIn, String emailIn)
	{
		super(nameIn, ageIn, genderIn, phoneIn, emailIn);
		guestId = guestIdIn;
	}
	
	public Reservation makeReservation(int resId, Room room, int checkInDate, int checkOutDate)
	{
		if(reservation != null)
		{
			throw new IllegalStateException("This guest already has a reservation.");
		}
		reservation = new Reservation(resId, this, room, checkInDate, checkOutDate);
		return reservation;
	}
	
	public void cancelReservation()
	{
		if(reservation == null)
		{
			throw new IllegalStateException("There is no reservation");
		}
		reservation = null;
	}
	
	public Reservation rescheduleReservation(int newCheckInDate, int newCheckOutDate)
	{
		if(reservation == null)
		{
			throw new IllegalStateException("There is no reservation to reschedule");
			
		}
		reservation = new Reservation(reservation.getReservationID(), this, reservation.getRoom(), newCheckInDate, newCheckOutDate);
		return reservation;
		
	}
	
	public Reservation getReservation()
	{
		return reservation;
	}
	
	public int getGuestID()
	{
		return guestId;
	}
	
	public void setGuestID(int guestIDin)
	{
		guestId = guestIDin;
	}
	
	public void displayInfo()
	{
		super.displayInfo();
		System.out.println("Guest Id: " + guestId);
	}
}
