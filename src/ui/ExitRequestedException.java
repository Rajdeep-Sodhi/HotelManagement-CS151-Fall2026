package ui;

// Used when the user types EXIT and wants to close the program
public class ExitRequestedException extends RuntimeException {

    public ExitRequestedException() {
        super("Exit requested.");
    }

    @Override
    public String toString() {
        return "ExitRequestedException: " + getMessage();
    }
}
