import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** DOCUMENT: max 0.5 kg, chargeable weight = actual weight, base fee = flat 50. */
public class Document extends Parcel {

    public static final BigDecimal MAX_WEIGHT = new BigDecimal("0.5");
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;

    private static final BigDecimal BASE_FEE = new BigDecimal("50");

    private final int pages;

    public Document(String trackingNumber, String receiverName, BigDecimal weight, int pages)
            throws InvalidInputException {
        super(trackingNumber, receiverName, weight, MAX_WEIGHT);
        this.pages = validatePages(pages);
    }

    public static int validatePages(long pages) throws InvalidInputException {
        if (pages < MIN_PAGES || pages > MAX_PAGES) {
            throw new InvalidInputException(
                    "Number of pages must be from " + MIN_PAGES + " to " + MAX_PAGES + ".");
        }
        return (int) pages;
    }

    public int getPages() {
        return pages;
    }

    @Override
    public String getType() {
        return "Document";
    }

    @Override
    public BigDecimal getChargeableWeight() {
        return getWeight();
    }

    @Override
    public BigDecimal getBaseFee() {
        return BASE_FEE;
    }

    @Override
    public Map<String, String> getDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Number of pages", String.valueOf(pages));
        return details;
    }
}
