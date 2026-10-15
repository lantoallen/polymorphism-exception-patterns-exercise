import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The console screens: the main menu and its four operations.
 * The rules live in the parcel classes; this class only asks, shows and reacts.
 */
public class ParcelMenu {

    private static final int LINE_WIDTH = 50;

    private static final int DOCUMENT = 1;
    private static final int BOX = 2;
    private static final int BACK = 0;

    private static final String PESO = pesoSign();

    private final InputReader input;
    private final ParcelManager manager = ParcelManager.getInstance();
    private final DeliveryService[] services = {
            new StandardDelivery(), new ExpressDelivery(), new SameDayDelivery()
    };
    private final String[] serviceChoices; // "1".."n" for the services, then "0" to cancel

    public ParcelMenu(InputReader input) {
        this.input = input;
        serviceChoices = new String[services.length + 1];
        for (int i = 0; i < services.length; i++) {
            serviceChoices[i] = String.valueOf(i + 1);
        }
        serviceChoices[services.length] = String.valueOf(BACK);
    }

    /** Shows the main menu until the user chooses Exit. */
    public void start() {
        boolean isRunning = true;
        while (isRunning) {
            int choice = showMainMenu();
            switch (choice) {
                case 1:
                    addParcel();
                    break;
                case 2:
                    viewAllParcels();
                    break;
                case 3:
                    changeDeliveryService();
                    break;
                case 4:
                    markAsDelivered();
                    break;
                case 5:
                    isRunning = false;
                    break;
                default:
                    break; // readChoice only returns choices that are shown on screen
            }
        }
        System.out.println();
        System.out.println("Thank you for using the Parcel Delivery System. Goodbye!");
    }

    private int showMainMenu() {
        printTitle("PARCEL DELIVERY SYSTEM");
        if (manager.isEmpty()) {
            // Nothing to view, change or deliver yet, so only Add and Exit are shown.
            System.out.println("  [1] Add Parcel");
            System.out.println("  [5] Exit");
            printLine('-');
            System.out.println("  No parcels yet. Add a parcel to see more options.");
            return input.readChoice("Enter your choice: ", "1", "5");
        }
        System.out.println("  [1] Add Parcel");
        System.out.println("  [2] View All Parcels");
        System.out.println("  [3] Change Delivery Service");
        System.out.println("  [4] Mark as Delivered");
        System.out.println("  [5] Exit");
        printLine('-');
        return input.readChoice("Enter your choice: ", "1", "2", "3", "4", "5");
    }

    // ================================================================ 1. Add Parcel

    private void addParcel() {
        printSection("ADD PARCEL");
        System.out.println("Parcel type:");
        System.out.println("  [1] Document  (max " + kg(Document.MAX_WEIGHT) + ")");
        System.out.println("  [2] Box       (max " + kg(Box.MAX_WEIGHT) + ")");
        System.out.println("  [3] Fragile   (max " + kg(Fragile.MAX_WEIGHT) + ")");
        System.out.println("  [0] Back to main menu");
        int type = input.readChoice("Enter your choice: ", "1", "2", "3", "0");
        if (type == BACK) {
            return;
        }

        Parcel parcel = readNewParcel(type);
        if (parcel == null) {
            return;
        }

        printParcelDetails(parcel);
        System.out.println();
        printServiceOptions(parcel, false);
        System.out.println("  [0] Cancel (do not add this parcel)");
        DeliveryService service = input.prompt("Enter your choice: ", text -> chooseFirstService(parcel, text));
        if (service == null) {
            System.out.println("Parcel was not added.");
            return;
        }

        try {
            manager.addParcel(parcel);
            System.out.println("Parcel added successfully! Delivery fee: " + peso(parcel.getDeliveryFee()));
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Asks for every detail of the chosen parcel type; each field is re-asked until it is valid. */
    private Parcel readNewParcel(int type) {
        System.out.println();
        System.out.println("Enter the details of the new " + typeNameOf(type) + ":");
        String trackingNumber = input.prompt("Tracking number (e.g. ABC0001): ", this::parseNewTrackingNumber);
        String receiverName = input.prompt("Receiver name: ", Parcel::validateReceiverName);
        BigDecimal weight = readWeight(maxWeightOf(type));
        try {
            if (type == DOCUMENT) {
                int pages = input.prompt("Number of pages (" + Document.MIN_PAGES + "-" + Document.MAX_PAGES + "): ",
                        text -> Document.validatePages(InputReader.parseWholeNumber(text, "Number of pages")));
                return new Document(trackingNumber, receiverName, weight, pages);
            }
            int length = readDimension("Length");
            int width = readDimension("Width");
            int height = readDimension("Height");
            if (type == BOX) {
                return new Box(trackingNumber, receiverName, weight, length, width, height);
            }
            String note = input.prompt("Handling note (e.g. Handle with care): ", Fragile::validateHandlingNote);
            return new Fragile(trackingNumber, receiverName, weight, length, width, height, note);
        } catch (InvalidInputException e) {
            // Every field was already checked as it was typed, so this is only a safety net.
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String parseNewTrackingNumber(String text) throws InvalidInputException {
        String trackingNumber = Parcel.validateTrackingNumber(text);
        manager.checkNotUsed(trackingNumber);
        return trackingNumber;
    }

    private BigDecimal readWeight(BigDecimal maxWeight) {
        return input.prompt("Weight in kg (max " + plain(maxWeight) + "): ",
                text -> Parcel.validateWeight(InputReader.parseDecimal(text, "Weight"), maxWeight));
    }

    private int readDimension(String label) {
        return input.prompt(label + " in cm: ",
                text -> Box.validateDimension(InputReader.parseWholeNumber(text, label), label));
    }

    private static String typeNameOf(int type) {
        if (type == DOCUMENT) {
            return "Document";
        }
        if (type == BOX) {
            return "Box";
        }
        return "Fragile";
    }

    private static BigDecimal maxWeightOf(int type) {
        if (type == DOCUMENT) {
            return Document.MAX_WEIGHT;
        }
        if (type == BOX) {
            return Box.MAX_WEIGHT;
        }
        return Fragile.MAX_WEIGHT;
    }

    /** Returns the chosen service, or null for 0 (cancel). Same Day is refused for heavy parcels. */
    private DeliveryService chooseFirstService(Parcel parcel, String text)
            throws InvalidInputException, ParcelAlreadyDeliveredException {
        int choice = InputReader.parseChoice(text, serviceChoices);
        if (choice == BACK) {
            return null;
        }
        DeliveryService service = services[choice - 1];
        parcel.setDeliveryService(service); // not allowed -> message is shown, then the user selects again
        return service;
    }

    // ================================================================ 2. View All Parcels

    private void viewAllParcels() {
        printSection("ALL PARCELS");
        List<Parcel> parcels = manager.getAllParcels();
        if (parcels.isEmpty()) {
            System.out.println("No parcels to display."); // safety net: the option is hidden when empty
            return;
        }

        // These three columns grow with the data; the others have a known maximum width.
        int receiverWidth = "Receiver".length();
        int weightWidth = "Chargeable Weight".length();
        int feeWidth = "Delivery Fee".length();
        int pendingCount = 0;
        for (Parcel parcel : parcels) {
            receiverWidth = Math.max(receiverWidth, parcel.getReceiverName().length());
            weightWidth = Math.max(weightWidth, kg(parcel.getChargeableWeight()).length());
            feeWidth = Math.max(feeWidth, peso(parcel.getDeliveryFee()).length());
            if (!parcel.isDelivered()) {
                pendingCount++;
            }
        }

        String rowFormat = "| %-12s | %-8s | %-" + receiverWidth + "s | %" + weightWidth + "s | %-8s | %"
                + feeWidth + "s | %-9s |%n";
        String border = "+" + repeat('-', 14) + "+" + repeat('-', 10) + "+" + repeat('-', receiverWidth + 2)
                + "+" + repeat('-', weightWidth + 2) + "+" + repeat('-', 10) + "+" + repeat('-', feeWidth + 2)
                + "+" + repeat('-', 11) + "+";

        System.out.println(border);
        System.out.printf(Locale.ROOT, rowFormat, "Tracking No.", "Type", "Receiver", "Chargeable Weight",
                "Service", "Delivery Fee", "Status");
        System.out.println(border);
        for (Parcel parcel : parcels) {
            System.out.printf(Locale.ROOT, rowFormat, parcel.getTrackingNumber(), parcel.getType(),
                    parcel.getReceiverName(), kg(parcel.getChargeableWeight()),
                    parcel.getDeliveryService().getName(), peso(parcel.getDeliveryFee()),
                    parcel.getStatus().getLabel());
        }
        System.out.println(border);
        System.out.println("Total: " + parcels.size() + (parcels.size() == 1 ? " parcel" : " parcels")
                + "  |  Pending: " + pendingCount + "  |  Delivered: " + (parcels.size() - pendingCount));
    }

    // ================================================================ 3. Change Delivery Service

    private void changeDeliveryService() {
        printSection("CHANGE DELIVERY SERVICE");
        Parcel parcel = input.prompt("Enter tracking number (0 to go back): ", this::findPendingParcel);
        if (parcel == null) {
            return;
        }

        printParcelDetails(parcel);
        printField("Current service", parcel.getDeliveryService().getName() + " - " + peso(parcel.getDeliveryFee()));
        System.out.println();
        printServiceOptions(parcel, true);
        System.out.println("  [0] Cancel (keep current service)");
        DeliveryService service = input.prompt("Enter your choice: ", text -> chooseNewService(parcel, text));
        if (service == null) {
            System.out.println("No changes made. The parcel keeps its "
                    + parcel.getDeliveryService().getName() + " service.");
            return;
        }
        System.out.println("Delivery service changed! New fee: " + peso(parcel.getDeliveryFee()));
    }

    /** Returns the pending parcel, or null for 0 (back). Not found / delivered -> message, then ask again. */
    private Parcel findPendingParcel(String text)
            throws InvalidInputException, ParcelNotFoundException, ParcelAlreadyDeliveredException {
        if (text.equals("0")) {
            return null;
        }
        return manager.findPendingParcel(Parcel.validateTrackingNumber(text));
    }

    /** Returns the new service, or null for 0 (cancel). A refused change keeps the old service. */
    private DeliveryService chooseNewService(Parcel parcel, String text)
            throws InvalidInputException, ParcelAlreadyDeliveredException {
        int choice = InputReader.parseChoice(text, serviceChoices);
        if (choice == BACK) {
            return null;
        }
        DeliveryService service = services[choice - 1];
        if (isCurrentService(parcel, service)) {
            throw new InvalidInputException("This parcel already uses " + service.getName()
                    + " delivery. Choose a different service, or 0 to cancel.");
        }
        try {
            parcel.setDeliveryService(service);
        } catch (InvalidInputException e) { // Same Day refused
            throw new InvalidInputException(e.getMessage() + System.lineSeparator()
                    + "The parcel keeps its " + parcel.getDeliveryService().getName() + " service.");
        }
        return service;
    }

    // ================================================================ 4. Mark as Delivered

    private void markAsDelivered() {
        printSection("MARK AS DELIVERED");
        Parcel parcel = input.prompt("Enter tracking number (0 to go back): ", this::deliverParcel);
        if (parcel != null) {
            System.out.println("Parcel delivered successfully!");
            System.out.println(parcel.getTrackingNumber() + " (" + parcel.getType() + " for "
                    + parcel.getReceiverName() + ") is now marked as Delivered.");
        }
    }

    /** Returns the delivered parcel, or null for 0 (back). Not found / already delivered -> message, ask again. */
    private Parcel deliverParcel(String text)
            throws InvalidInputException, ParcelNotFoundException, ParcelAlreadyDeliveredException {
        if (text.equals("0")) {
            return null;
        }
        return manager.markAsDelivered(Parcel.validateTrackingNumber(text));
    }

    // ================================================================ shared screen parts

    private void printParcelDetails(Parcel parcel) {
        System.out.println();
        printField("Tracking number", parcel.getTrackingNumber());
        printField("Type", parcel.getType());
        printField("Receiver", parcel.getReceiverName());
        printField("Actual weight", kg(parcel.getWeight()));
        for (Map.Entry<String, String> detail : parcel.getDetails().entrySet()) {
            printField(detail.getKey(), detail.getValue()); // pages, size, note... depending on the type
        }
        printField("Chargeable weight", kg(parcel.getChargeableWeight()));
        printField("Base fee", peso(parcel.getBaseFee()));
    }

    /** Lists every service with what it would cost for this parcel. */
    private void printServiceOptions(Parcel parcel, boolean markCurrent) {
        System.out.println("Delivery service:");
        for (int i = 0; i < services.length; i++) {
            DeliveryService service = services[i];
            String rule = service.getRestriction().isEmpty() ? "" : "  (" + service.getRestriction() + ")";
            String info;
            if (!service.isAvailableFor(parcel)) {
                info = "not available" + rule;
            } else if (markCurrent && isCurrentService(parcel, service)) {
                info = peso(service.calculateFee(parcel.getBaseFee())) + "  (current)";
            } else {
                info = peso(service.calculateFee(parcel.getBaseFee())) + rule;
            }
            System.out.printf(Locale.ROOT, "  [%d] %-9s %s%n", i + 1, service.getName(), info);
        }
    }

    private static boolean isCurrentService(Parcel parcel, DeliveryService service) {
        return parcel.getDeliveryService().getName().equals(service.getName());
    }

    private static void printField(String label, String value) {
        System.out.printf(Locale.ROOT, "  %-18s: %s%n", label, value);
    }

    private static void printTitle(String title) {
        System.out.println();
        printLine('=');
        System.out.println(repeat(' ', (LINE_WIDTH - title.length()) / 2) + title);
        printLine('=');
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("---- " + title + " " + repeat('-', LINE_WIDTH - title.length() - 6));
    }

    private static void printLine(char character) {
        System.out.println(repeat(character, LINE_WIDTH));
    }

    // ================================================================ formatting

    /** e.g. 2880 -> peso sign + "2,880.00" (or "Php 2,880.00" on a console that cannot show the sign). */
    private static String peso(BigDecimal amount) {
        return PESO + String.format(Locale.US, "%,.2f", amount);
    }

    /** e.g. 3 -> "3 kg", 0.30 -> "0.3 kg". */
    private static String kg(BigDecimal weight) {
        return plain(weight) + " kg";
    }

    private static String plain(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }

    private static String repeat(char character, int count) {
        return String.join("", Collections.nCopies(Math.max(0, count), String.valueOf(character)));
    }

    /** The peso sign if this console can print it, otherwise "Php " so it never shows up as "?". */
    private static String pesoSign() {
        char pesoSign = (char) 0x20B1; // Unicode code of the Philippine peso sign
        try {
            return InputReader.consoleCharset().newEncoder().canEncode(pesoSign) ? String.valueOf(pesoSign) : "Php ";
        } catch (UnsupportedOperationException e) { // a charset that cannot encode at all
            return "Php ";
        }
    }
}
