package model;

import exception.HotelException;
import java.util.HashMap;
import java.util.Map;

public class ServiceOrder implements Payable
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
        // Adds another service or increases the quantity of an existing one.
    public void addItem(String item, int quantity, Session actor)
            throws HotelException
    {
        reservation.requireAccess(actor);

        if (!status.equals("OPEN"))
        {
            throw new HotelException(
                    "Items can only be added to an open service order.");
        }

        if (paid)
        {
            throw new HotelException(
                    "A paid service order cannot be changed.");
        }

        if (quantity <= 0)
        {
            throw new HotelException("Service quantity must be positive.");
        }

        String cleanItem = cleanItem(item);
        int currentQuantity = items.getOrDefault(cleanItem, 0);

        // Prevent the quantity from overflowing an int.
        if (currentQuantity > Integer.MAX_VALUE - quantity)
        {
            throw new HotelException("Service quantity is too large.");
        }

        items.put(cleanItem, currentQuantity + quantity);
    }

    // Removes a quantity of an item without leaving the order empty.
    public void removeItem(String item, int quantity, Session actor)
            throws HotelException
    {
        reservation.requireAccess(actor);

        if (!status.equals("OPEN"))
        {
            throw new HotelException(
                    "Items can only be removed from an open service order.");
        }

        if (paid)
        {
            throw new HotelException(
                    "A paid service order cannot be changed.");
        }

        if (quantity <= 0)
        {
            throw new HotelException("Service quantity must be positive.");
        }

        String cleanItem = cleanItem(item);
        Integer currentQuantity = items.get(cleanItem);

        if (currentQuantity == null || quantity > currentQuantity)
        {
            throw new HotelException(
                    "Cannot remove more items than were ordered.");
        }

        if (quantity == currentQuantity && items.size() == 1)
        {
            throw new HotelException(
                    "A service order must contain at least one item.");
        }

        if (quantity == currentQuantity)
        {
            items.remove(cleanItem);
        }
        else
        {
            items.put(cleanItem, currentQuantity - quantity);
        }
    }
        // Marks an open service order as completed.
    public void complete(Session actor) throws HotelException
    {
        reservation.requireAccess(actor);

        if (!status.equals("OPEN"))
        {
            throw new HotelException(
                    "Only an open service order can be completed.");
        }

        status = "COMPLETED";
    }

    // Cancels an open service order before it has been paid.
    public void cancel(Session actor) throws HotelException
    {
        reservation.requireAccess(actor);

        if (!status.equals("OPEN"))
        {
            throw new HotelException(
                    "Only an open service order can be cancelled.");
        }

        if (paid)
        {
            throw new HotelException(
                    "A paid service order cannot be cancelled.");
        }

        status = "CANCELLED";
    }
        // Calculates the unpaid total for all services in this order.
    @Override
    public double getAmountDue(Session viewer) throws HotelException
    {
        reservation.requireAccess(viewer);

        if (paid || status.equals("CANCELLED"))
        {
            return 0.0;
        }

        double total = 0.0;

        for (Map.Entry<String, Integer> entry : items.entrySet())
        {
            if (entry.getKey().equals("LAUNDRY"))
            {
                total += LAUNDRY_PRICE * entry.getValue();
            }
            else if (entry.getKey().equals("ROOM_SERVICE"))
            {
                total += ROOM_SERVICE_PRICE * entry.getValue();
            }
        }

        return Math.round(total * 100.0) / 100.0;
    }

    // Processes a full payment for a completed service order.
    @Override
    public void pay(Payment payment, Session actor) throws HotelException
    {
        reservation.requireAccess(actor);

        if (!status.equals("COMPLETED"))
        {
            throw new HotelException(
                    "Service order must be completed before payment.");
        }

        if (paid)
        {
            throw new HotelException(
                    "Service order has already been paid.");
        }

        if (payment == null)
        {
            throw new HotelException("A payment is required.");
        }

        payment.process(this, getAmountDue(actor));
        paid = true;
    }

}
