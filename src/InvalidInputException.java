/**
 * Thrown when a value entered by the user is not accepted: empty, badly formatted,
 * out of range, a tracking number that already exists, or Same Day for a parcel over 10 kg.
 */
public class InvalidInputException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
