import java.math.BigDecimal;

/** EXPRESS: delivery fee = base fee x 1.5. */
public class ExpressDelivery implements DeliveryService {

    private static final BigDecimal MULTIPLIER = new BigDecimal("1.5");

    @Override
    public String getName() {
        return "Express";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseFee) {
        return baseFee.multiply(MULTIPLIER);
    }
}
