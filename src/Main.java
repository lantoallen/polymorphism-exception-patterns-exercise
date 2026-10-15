import java.util.NoSuchElementException;

/** Starts the Parcel Delivery System. */
public class Main {

    public static void main(String[] args) {
        ParcelMenu menu = new ParcelMenu(new InputReader());
        try {
            menu.start();
        } catch (NoSuchElementException e) {
            // Only happens when the input stream ends (Ctrl+D / Ctrl+Z, or piped input runs out).
            System.out.println();
            System.out.println("Input ended. Exiting the Parcel Delivery System.");
        }
    }
}
