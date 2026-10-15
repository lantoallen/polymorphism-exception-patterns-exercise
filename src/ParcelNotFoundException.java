/** Thrown when no parcel has the given tracking number. */
public class ParcelNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParcelNotFoundException() {
        super("Parcel not found!");
    }
}
