import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * FRAGILE: a Box with a max weight of 30 kg and a handling note.
 * Chargeable weight follows the Box rule; base fee = Box base fee + 100 handling fee.
 */
public class Fragile extends Box {

    public static final BigDecimal MAX_WEIGHT = new BigDecimal("30");
    public static final int MIN_NOTE_LENGTH = 3;
    public static final int MAX_NOTE_LENGTH = 100;

    private static final BigDecimal HANDLING_FEE = new BigDecimal("100");
    private static final Pattern HAS_LETTER = Pattern.compile("\\p{L}");

    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, BigDecimal weight,
                   int length, int width, int height, String handlingNote) throws InvalidInputException {
        super(trackingNumber, receiverName, weight, MAX_WEIGHT, length, width, height);
        this.handlingNote = validateHandlingNote(handlingNote);
    }

    public static String validateHandlingNote(String text) throws InvalidInputException {
        String note = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (note.isEmpty()) {
            throw new InvalidInputException("Handling note cannot be empty.");
        }
        if (note.length() < MIN_NOTE_LENGTH) {
            throw new InvalidInputException(
                    "Handling note must be at least " + MIN_NOTE_LENGTH + " characters (e.g. Glass).");
        }
        if (note.length() > MAX_NOTE_LENGTH) {
            throw new InvalidInputException(
                    "Handling note must be at most " + MAX_NOTE_LENGTH + " characters.");
        }
        if (!HAS_LETTER.matcher(note).find()) {
            throw new InvalidInputException("Handling note must contain words (e.g. Handle with care).");
        }
        return note;
    }

    public String getHandlingNote() {
        return handlingNote;
    }

    @Override
    public BigDecimal getBaseFee() {
        return super.getBaseFee().add(HANDLING_FEE);
    }

    @Override
    public String getType() {
        return "Fragile";
    }

    @Override
    public Map<String, String> getDetails() {
        Map<String, String> details = super.getDetails(); // the Box details, plus the note
        details.put("Handling note", handlingNote);
        return details;
    }
}
