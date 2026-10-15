import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BOX: max 50 kg.
 * Volumetric weight = (L x W x H) / 3500.
 * Chargeable weight = the bigger of actual and volumetric weight, rounded UP to a whole kg.
 * Base fee = 40 x chargeable weight.
 */
public class Box extends Parcel {

    public static final BigDecimal MAX_WEIGHT = new BigDecimal("50");

    private static final BigDecimal VOLUMETRIC_DIVISOR = new BigDecimal("3500");
    private static final BigDecimal RATE_PER_KG = new BigDecimal("40");

    private final int length;
    private final int width;
    private final int height;

    public Box(String trackingNumber, String receiverName, BigDecimal weight,
               int length, int width, int height) throws InvalidInputException {
        this(trackingNumber, receiverName, weight, MAX_WEIGHT, length, width, height);
    }

    /** For subclasses that follow the box rules but have a different weight limit (Fragile). */
    protected Box(String trackingNumber, String receiverName, BigDecimal weight, BigDecimal maxWeight,
                  int length, int width, int height) throws InvalidInputException {
        super(trackingNumber, receiverName, weight, maxWeight);
        this.length = validateDimension(length, "Length");
        this.width = validateDimension(width, "Width");
        this.height = validateDimension(height, "Height");
    }

    public static int validateDimension(long centimeters, String label) throws InvalidInputException {
        if (centimeters <= 0) {
            throw new InvalidInputException(label + " must be greater than 0.");
        }
        if (centimeters > Integer.MAX_VALUE) {
            throw new InvalidInputException(label + " is too large.");
        }
        return (int) centimeters;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** Volumetric weight rounded to 2 decimals, for display only (e.g. 8000 / 3500 = 2.29). */
    public BigDecimal getVolumetricWeight() {
        return getVolume().divide(VOLUMETRIC_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    /**
     * Uses exact values, never the rounded display value: rounding each weight up
     * and keeping the bigger one equals rounding the bigger weight up.
     */
    @Override
    public BigDecimal getChargeableWeight() {
        BigDecimal actual = getWeight().setScale(0, RoundingMode.CEILING);
        BigDecimal volumetric = getVolume().divide(VOLUMETRIC_DIVISOR, 0, RoundingMode.CEILING);
        return actual.max(volumetric);
    }

    @Override
    public BigDecimal getBaseFee() {
        return RATE_PER_KG.multiply(getChargeableWeight());
    }

    @Override
    public String getType() {
        return "Box";
    }

    @Override
    public Map<String, String> getDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Size (L x W x H)", length + " x " + width + " x " + height + " cm");
        details.put("Volumetric weight", getVolumetricWeight().toPlainString() + " kg");
        return details;
    }

    // BigDecimal, so even the largest int dimensions cannot overflow.
    private BigDecimal getVolume() {
        return BigDecimal.valueOf(length)
                .multiply(BigDecimal.valueOf(width))
                .multiply(BigDecimal.valueOf(height));
    }
}
