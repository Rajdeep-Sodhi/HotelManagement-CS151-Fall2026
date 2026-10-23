package exception;

// Used when a user tries to do something they do not have permission for
public class AuthorizationException extends HotelException {

    public AuthorizationException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "AuthorizationException: " + getMessage();
    }
}
