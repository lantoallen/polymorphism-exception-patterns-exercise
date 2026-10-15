import java.math.BigDecimal;

/** STANDARD: delivery fee = base fee. */
public class StandardDelivery implements DeliveryService {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseFee) {
        return baseFee;
    }
}
