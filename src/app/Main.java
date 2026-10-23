package app;

import exception.HotelException;
import java.time.LocalDate;
import java.util.Scanner;
import model.Hotel;
import ui.ExitRequestedException;
import ui.InputHelper;
import ui.Login;

/** Starts the console application. */
public final class Main
{
	private Main()
	{
	}

	public static void main(String[] args)
	{
		try
		{
			Scanner sc = new Scanner(System.in);

			try
			{
				Hotel hotel = new Hotel(LocalDate.now());

				//fills hotel with demo data to startoff
				hotel.seedDemoData();

				System.out.println("Hotel Management System");
				System.out.println("Demo accounts: manager / manager123, "
						+ "employee / employee123, guest / guest123, guest2 / guest234");
				System.out.println("Dates use YYYY-MM-DD. Type EXIT at any prompt to quit.");

				Login menu = new Login(hotel, new InputHelper(sc));
				menu.run();
			}
			finally
			{
				sc.close();
			}
		}
		catch (ExitRequestedException ex)
		{
			System.out.println("Goodbye.");
		}
		catch (HotelException ex)
		{
			System.err.println(
					"Unable to start the hotel app: " + ex.getMessage());
		}
	}

	@Override
	public String toString()
	{
		return "Hotel Management app starter";
	}
}
