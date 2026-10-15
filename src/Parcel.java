import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * POLYMORPHISM - base class of every parcel type.
 *
 * Holds the details all parcels share. Each subclass overrides getType(),
 * getChargeableWeight(), getBaseFee() and getDetails() with its own rules, so the
 * rest of the program can work with any Parcel without checking its actual type.
 */
public abstract class Parcel {

    /** Delivery status of a parcel. */
    public enum Status {
        PENDING("Pending"),
        DELIVERED("Delivered");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_WEIGHT_DECIMALS = 3;

    private static final Pattern TRACKING_NUMBER_FORMAT = Pattern.compile("ABC[0-9]{4}");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\p{Nd}");
    // Words made of letters (including accented ones like n with tilde), joined by ONE space,
    // hyphen, apostrophe or period; may end with a period.
    // Examples: "Ma. Clara", "O'Neil", "Dela Cruz-Reyes", "Juan Dela Cruz Jr.".
    // Numbers are written as Roman numerals, which are letters: "Juan Dela Cruz III".
    private static final Pattern NAME_FORMAT =
            Pattern.compile("\\p{L}[\\p{L}\\p{M}]*(?:(?:\\. ?|[ '\\u2019-])\\p{L}[\\p{L}\\p{M}]*)*\\.?");

    private final String trackingNumber;
    private final String receiverName;
    private final BigDecimal weight;
    private DeliveryService deliveryService;
    private Status status;

    /** maxWeight is the limit of the parcel type, e.g. 0.5 kg for a Document. */
    protected Parcel(String trackingNumber, String receiverName, BigDecimal weight, BigDecimal maxWeight)
            throws InvalidInputException {
        Objects.requireNonNull(maxWeight, "maxWeight");
        this.trackingNumber = validateTrackingNumber(trackingNumber);
        this.receiverName = validateReceiverName(receiverName);
        this.weight = validateWeight(weight, maxWeight);
        // Standard has no restrictions, so a new parcel is always valid.
        // The menu replaces it with the service the user picks.
        this.deliveryService = new StandardDelivery();
        this.status = Status.PENDING;
    }

    // ---------------------------------------------------------------- overridden by each type

    /** "Document", "Box" or "Fragile". */
    public abstract String getType();

    /** Weight (kg) the parcel is charged for. */
    public abstract BigDecimal getChargeableWeight();

    /** Fee before the delivery service is applied. */
    public abstract BigDecimal getBaseFee();

    /** Type-specific details as label -> value, e.g. "Number of pages" -> "12". */
    public abstract Map<String, String> getDetails();

    // ---------------------------------------------------------------- shared behaviour

    /** Delivery fee = the current delivery service (strategy) applied to the base fee. */
    public BigDecimal getDeliveryFee() {
        return deliveryService.calculateFee(getBaseFee());
    }

    /**
     * Switches to another delivery service. If the change is rejected,
     * the parcel keeps its old service.
     */
    public void setDeliveryService(DeliveryService newService)
            throws ParcelAlreadyDeliveredException, InvalidInputException {
        Objects.requireNonNull(newService, "newService");
        ensurePending();
        if (!newService.isAvailableFor(this)) {
            throw new InvalidInputException(newService.getUnavailableMessage());
        }
        deliveryService = newService;
    }

    /** Throws if the parcel is already delivered (used before changing its service). */
    public void ensurePending() throws ParcelAlreadyDeliveredException {
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel already delivered!");
        }
    }

    public void markAsDelivered() throws ParcelAlreadyDeliveredException {
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel was already delivered.");
        }
        status = Status.DELIVERED;
    }

    public boolean isDelivered() {
        return status == Status.DELIVERED;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public DeliveryService getDeliveryService() {
        return deliveryService;
    }

    public Status getStatus() {
        return status;
    }

    // ---------------------------------------------------------------- validation
    // Used by the constructor AND by the menu, which checks each field as soon as it is typed.

    /** Trims spaces and upper-cases, so "abc0001" and "ABC0001" are the same parcel. */
    public static String normalizeTrackingNumber(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    public static String validateTrackingNumber(String text) throws InvalidInputException {
        String trackingNumber = normalizeTrackingNumber(text);
        if (trackingNumber.isEmpty()) {
            throw new InvalidInputException("Tracking number cannot be empty.");
        }
        if (!TRACKING_NUMBER_FORMAT.matcher(trackingNumber).matches()) {
            throw new InvalidInputException(
                    "Invalid tracking number. It must be ABC followed by 4 digits (e.g. ABC0001).");
        }
        return trackingNumber;
    }

    public static String validateReceiverName(String text) throws InvalidInputException {
        String name = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new InvalidInputException("Receiver name cannot be empty.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(
                    "Receiver name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        if (HAS_DIGIT.matcher(name).find()) {
            throw new InvalidInputException("Receiver name cannot contain numbers. "
                    + "Use Roman numerals instead (e.g. Juan Dela Cruz III).");
        }
        if (!NAME_FORMAT.matcher(name).matches()) {
            throw new InvalidInputException("Invalid receiver name. Use letters, with only one space, "
                    + "period, apostrophe or hyphen between words (e.g. Ma. Clara O'Neil-Reyes).");
        }
        return name;
    }

    public static BigDecimal validateWeight(BigDecimal weight, BigDecimal maxWeight)
            throws InvalidInputException {
        if (weight == null) {
            throw new InvalidInputException("Weight cannot be empty.");
        }
        if (weight.signum() <= 0) {
            throw new InvalidInputException("Weight must be greater than 0.");
        }
        if (weight.compareTo(maxWeight) > 0) {
            throw new InvalidInputException("Weight must not exceed "
                    + maxWeight.stripTrailingZeros().toPlainString() + " kg for this parcel type.");
        }
        if (weight.stripTrailingZeros().scale() > MAX_WEIGHT_DECIMALS) {
            throw new InvalidInputException(
                    "Weight can have at most " + MAX_WEIGHT_DECIMALS + " decimal places (e.g. 0.125).");
        }
        return weight;
    }
}
