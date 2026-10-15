import java.math.BigDecimal;

/**
 * STRATEGY PATTERN - one class per delivery service.
 *
 * A parcel keeps its current DeliveryService and asks it for the fee, so changing
 * the service only swaps this object. No if/else on service names is needed, and a
 * new service can be added by writing one new class.
 */
public interface DeliveryService {

    /** Name shown to the user, e.g. "Express". */
    String getName();

    /** Turns the parcel's base fee into the delivery fee. */
    BigDecimal calculateFee(BigDecimal baseFee);

    /** Whether this service can carry the parcel. Services without restrictions keep this default. */
    default boolean isAvailableFor(Parcel parcel) {
        return true;
    }

    /** Short rule shown next to the service in the menu, e.g. "parcels up to 10 kg only". Empty if none. */
    default String getRestriction() {
        return "";
    }

    /** Message shown when isAvailableFor(parcel) is false. */
    default String getUnavailableMessage() {
        return getName() + " delivery is not available for this parcel!";
    }
}
