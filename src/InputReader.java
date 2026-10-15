import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads what the user types. Every prompt repeats until the value is valid,
 * so a typo never sends the user back to the main menu.
 */
public class InputReader {

    /** Turns one line of input into a value, or throws an exception that explains why it is not accepted. */
    public interface Parser<T> {
        T parse(String input)
                throws InvalidInputException, ParcelNotFoundException, ParcelAlreadyDeliveredException;
    }

    private static final Pattern WHOLE_NUMBER = Pattern.compile("-?[0-9]+");
    private static final Pattern DECIMAL_NUMBER = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    private static final int MAX_WHOLE_NUMBER_DIGITS = 18; // always fits in a long

    private final Scanner scanner;

    public InputReader() {
        // Read with the console's own encoding so letters like the Filipino n-tilde arrive intact.
        this.scanner = new Scanner(System.in, inputCharset().name());
    }

    /** Prints the prompt and returns what the user typed, without leading/trailing spaces. */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Asks until the parser accepts the input, showing the reason each time it does not. */
    public <T> T prompt(String prompt, Parser<T> parser) {
        T value = null;
        boolean hasValidInput = false;
        while (!hasValidInput) {
            String input = readLine(prompt);
            try {
                value = parser.parse(input);
                hasValidInput = true;
            } catch (InvalidInputException | ParcelNotFoundException | ParcelAlreadyDeliveredException e) {
                System.out.println(e.getMessage());
            }
        }
        return value;
    }

    /** Asks until the user types exactly one of the choices shown, e.g. "1" (not "01" or "1.0"). */
    public int readChoice(String prompt, String... choices) {
        return prompt(prompt, input -> parseChoice(input, choices));
    }

    public static int parseChoice(String input, String... choices) throws InvalidInputException {
        String text = input == null ? "" : input.trim();
        if (!Arrays.asList(choices).contains(text)) {
            throw new InvalidInputException("Invalid choice. Please enter " + listChoices(choices) + ".");
        }
        return Integer.parseInt(text);
    }

    /** Accepts whole numbers like 12 or -3; rejects 12.5, 012, 1e3, +5 and text. */
    public static long parseWholeNumber(String input, String fieldName) throws InvalidInputException {
        String text = input == null ? "" : input.trim();
        if (text.isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        if (!WHOLE_NUMBER.matcher(text).matches()) {
            throw new InvalidInputException(fieldName + " must be a whole number (e.g. 12).");
        }
        String digits = text.startsWith("-") ? text.substring(1) : text;
        rejectLeadingZeros(digits, fieldName);
        if (digits.length() > MAX_WHOLE_NUMBER_DIGITS) {
            throw new InvalidInputException(fieldName + " is out of range.");
        }
        return Long.parseLong(text);
    }

    /** Accepts numbers like 2, 1.5 or 0.25; rejects .5, 5., 05, 1,5, 1e3 and text. */
    public static BigDecimal parseDecimal(String input, String fieldName) throws InvalidInputException {
        String text = input == null ? "" : input.trim();
        if (text.isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        if (!DECIMAL_NUMBER.matcher(text).matches()) {
            throw new InvalidInputException(fieldName + " must be a number (e.g. 1.5 or 0.25).");
        }
        String digits = text.startsWith("-") ? text.substring(1) : text;
        int dot = digits.indexOf('.');
        rejectLeadingZeros(dot < 0 ? digits : digits.substring(0, dot), fieldName);
        return new BigDecimal(text);
    }

    /**
     * Encoding of the console window. Used to read input and to decide
     * whether the peso sign can be printed.
     */
    public static Charset consoleCharset() {
        String encoding = System.getProperty("stdout.encoding");          // Java 19 and newer
        if (encoding == null) {
            encoding = System.getProperty("sun.stdout.encoding");         // older Java on a Windows console
        }
        try {
            return encoding == null ? Charset.defaultCharset() : Charset.forName(encoding);
        } catch (IllegalArgumentException e) {                            // unknown or unsupported name
            return Charset.defaultCharset();
        }
    }

    /** Encoding of what the user types. Newer Java reports it directly; otherwise use the console's. */
    private static Charset inputCharset() {
        String encoding = System.getProperty("stdin.encoding");
        try {
            return encoding == null ? consoleCharset() : Charset.forName(encoding);
        } catch (IllegalArgumentException e) {                            // unknown or unsupported name
            return consoleCharset();
        }
    }

    /** "7" and "0.5" are fine; "07", "007" and "00.5" are not. */
    private static void rejectLeadingZeros(String integerPart, String fieldName) throws InvalidInputException {
        if (integerPart.length() > 1 && integerPart.charAt(0) == '0') {
            throw new InvalidInputException(fieldName + " must not have leading zeros (e.g. 5, not 05).");
        }
    }

    /** {"1", "2", "5"} becomes "1, 2 or 5". */
    private static String listChoices(String[] choices) {
        if (choices.length == 1) {
            return choices[0];
        }
        String allButLast = String.join(", ", Arrays.copyOf(choices, choices.length - 1));
        return allButLast + " or " + choices[choices.length - 1];
    }
}
