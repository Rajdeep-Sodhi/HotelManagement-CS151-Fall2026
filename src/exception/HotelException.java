package exception;

/**
 * Base checked exception for hotel validation, duplicate, state, and payment
 * failures.
 */
public class HotelException extends Exception {

    public HotelException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        //This returns the Class name along with whatever message was implemented in the constructor
            //getSimpleName ensures only the class name is returned like Hotel instead of src.model.Hotel
        return getClass().getSimpleName() + ": " + getMessage();
    }
}
