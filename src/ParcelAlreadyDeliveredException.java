/** Thrown when an operation needs a pending parcel but the parcel is already delivered. */
public class ParcelAlreadyDeliveredException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParcelAlreadyDeliveredException(String message) {
        super(message);
    }
}
