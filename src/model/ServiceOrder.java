package model;

import exception.HotelException;
import java.util.HashMap;
import java.util.Map;

public class ServiceOrder
{

    // Stores the services ordered for one checked-in reservation.
    private final int orderId;
    private final Reservation reservation;
    private final Map<String, Integer> items;
    private String status;
    private boolean paid;

    private static final double LAUNDRY_PRICE = 10.00;
    private static final double ROOM_SERVICE_PRICE = 20.00;

    /* Create a new service order with one valid item and start it
       as open and unpaid. */
    ServiceOrder(int id, Reservation reservation, String item, int quantity)
            throws HotelException
    {
        if (id <= 0)
        {
            throw new HotelException("Service order ID must be positive.");
        }

        if (reservation == null)
        {
            throw new HotelException(
                    "A service order must belong to a reservation.");
        }

        if (!"CHECKED_IN".equals(reservation.status()))
        {
            throw new HotelException(
                    "Services can only be ordered during a checked-in stay.");
        }

        if (quantity <= 0)
        {
            throw new HotelException("Service quantity must be positive.");
        }

        String cleanItem = cleanItem(item);

        orderId = id;
        this.reservation = reservation;
        items = new HashMap<>();
        items.put(cleanItem, quantity);
        status = "OPEN";
        paid = false;
    }

    // Cleans the service name and makes sure the hotel offers it.
    private static String cleanItem(String item) throws HotelException
    {
        if (item == null || item.trim().isEmpty())
        {
            throw new HotelException("Service name cannot be blank.");
        }

        String cleanItem = item.trim().toUpperCase();

        if (!cleanItem.equals("LAUNDRY")
                && !cleanItem.equals("ROOM_SERVICE"))
        {
            throw new HotelException("That service is not available.");
        }

        return cleanItem;
    }
}
