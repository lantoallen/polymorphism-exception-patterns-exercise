import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SINGLETON PATTERN - the one and only store of parcels.
 *
 * The constructor is private, so no other class can create a second store.
 * Everyone gets the same object from getInstance(). The instance is created
 * when the class loads, which is simple and thread-safe.
 */
public final class ParcelManager {

    private static final ParcelManager INSTANCE = new ParcelManager();

    // Key = tracking number (unique). LinkedHashMap keeps the order parcels were added.
    private final Map<String, Parcel> parcels = new LinkedHashMap<>();

    private ParcelManager() {
        // Blocks a second instance, even one created through reflection.
        if (INSTANCE != null) {
            throw new IllegalStateException("ParcelManager is a singleton. Use ParcelManager.getInstance().");
        }
    }

    public static ParcelManager getInstance() {
        return INSTANCE;
    }

    public void addParcel(Parcel parcel) throws InvalidInputException {
        Objects.requireNonNull(parcel, "parcel");
        checkNotUsed(parcel.getTrackingNumber());
        parcels.put(parcel.getTrackingNumber(), parcel);
    }

    /** Tracking numbers must be unique. */
    public void checkNotUsed(String trackingNumber) throws InvalidInputException {
        if (contains(trackingNumber)) {
            throw new InvalidInputException("Tracking number " + Parcel.normalizeTrackingNumber(trackingNumber)
                    + " already exists!");
        }
    }

    public boolean contains(String trackingNumber) {
        return parcels.containsKey(Parcel.normalizeTrackingNumber(trackingNumber));
    }

    public Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        Parcel parcel = parcels.get(Parcel.normalizeTrackingNumber(trackingNumber));
        if (parcel == null) {
            throw new ParcelNotFoundException();
        }
        return parcel;
    }

    /** Finds a parcel whose service can still be changed. */
    public Parcel findPendingParcel(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.ensurePending();
        return parcel;
    }

    public Parcel markAsDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.markAsDelivered();
        return parcel;
    }

    /** Read-only copy, in the order the parcels were added. */
    public List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(new ArrayList<>(parcels.values()));
    }

    public boolean isEmpty() {
        return parcels.isEmpty();
    }
}
