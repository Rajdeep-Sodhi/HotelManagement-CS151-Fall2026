package exception;

public class CapacityExceededException extends HotelException{
                // Signals an operation that would exceed the shared 100-object registry limit.

                /* Forward the specific error message to HotelException. */
                public CapacityExceededException(String message)
                {
                    super(message);
                }

                /* Display the exception type and capacity error message. */
                @Override
                public String toString()
                {
                    return getClass().getSimpleName() + ": " + getMessage();
                }
            }
            
