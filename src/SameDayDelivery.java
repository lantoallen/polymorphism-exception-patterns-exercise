import java.math.BigDecimal;

/**
 * SAME DAY: delivery fee = (base fee x 2) + 100.
 * Only for parcels with a chargeable weight of 10 kg or less.
 */
public class SameDayDelivery implements DeliveryService {

    public static final BigDecimal MAX_CHARGEABLE_WEIGHT = new BigDecimal("10");

    private static final BigDecimal MULTIPLIER = new BigDecimal("2");
    private static final BigDecimal SURCHARGE = new BigDecimal("100");

    @Override
    public String getName() {
        return "Same Day";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseFee) {
        return baseFee.multiply(MULTIPLIER).add(SURCHARGE);
    }

    @Override
    public boolean isAvailableFor(Parcel parcel) {
        return parcel.getChargeableWeight().compareTo(MAX_CHARGEABLE_WEIGHT) <= 0;
    }

    @Override
    public String getRestriction() {
        return "parcels up to " + MAX_CHARGEABLE_WEIGHT.toPlainString() + " kg only";
    }

    @Override
    public String getUnavailableMessage() {
        return "Same Day delivery is only for parcels up to "
                + MAX_CHARGEABLE_WEIGHT.toPlainString() + " kg!";
    }
}
