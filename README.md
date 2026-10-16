# Parcel Delivery System

A simple Java console program for adding and tracking parcel deliveries.
It shows **polymorphism**, **exception handling**, and the **Singleton** and **Strategy** patterns.

## How to run

**In VS Code**
1. Open this folder in VS Code.
2. Open `src/Main.java`.
3. Click **Run** above `main`.
4. Type your choices in the terminal at the bottom.

**In Terminal**
```bash
javac -d out src/*.java
java -cp out Main
```

## Menu

| Option | What it does |
|---|---|
| 1. Add Parcel | Add a Document, Box or Fragile parcel and choose a delivery service |
| 2. View All Parcels | Show all parcels in a table |
| 3. Change Delivery Service | Change the service of a parcel that is not yet delivered |
| 4. Mark as Delivered | Mark a parcel as delivered |
| 5. Exit | Close the program |

If there are no parcels yet, options 2 to 4 say the list is empty and return to the menu.

## Fees

| Parcel | Max weight | Charged weight | Base fee |
|---|---|---|---|
| Document | 0.5 kg | actual weight | ₱50 |
| Box | 50 kg | the bigger of actual weight and L × W × H ÷ 3500, rounded up | ₱40 per kg |
| Fragile | 30 kg | same as Box | Box fee + ₱100 |

| Service | Delivery fee |
|---|---|
| Standard | base fee |
| Express | base fee × 1.5 |
| Same Day | base fee × 2 + ₱100 (only for parcels up to 10 kg) |

Example: Fragile, 40 × 20 × 10 cm, 1.8 kg, Express. 8000 ÷ 3500 = 2.29, rounded up to 3 kg.
Base fee = ₱40 × 3 + ₱100 = ₱220. Delivery fee = ₱220 × 1.5 = **₱330.00**.

## Where each requirement is

| Requirement | Where to look |
|---|---|
| Polymorphism | `Parcel` is the parent class of `Document`, `Box` and `Fragile` (`Fragile` extends `Box`). Each type calculates its own charged weight and base fee. |
| Exception handling | `InvalidInputException`, `ParcelNotFoundException` and `ParcelAlreadyDeliveredException`. A wrong input shows the reason and asks again. |
| Singleton | `ParcelManager`: there is only one list of parcels in the whole program. |
| Strategy | `DeliveryService` with `StandardDelivery`, `ExpressDelivery` and `SameDayDelivery`. Each service calculates its own fee. |

The other files: `Main` starts the program, `ParcelMenu` shows the screens, and `InputReader`
reads what the user types.

## Input rules

- **Tracking number:** `ABC` + 4 digits (e.g. `ABC0001`), and no two parcels can share one.
  Small letters are changed to capitals (`abc0001` becomes `ABC0001`).
- **Receiver name:** letters only, 2 to 50 characters. Write numbers as Roman numerals (e.g. `Juan Dela Cruz III`).
- **Weight:** more than 0 and not over the parcel's max, up to 3 decimal places.
- **Pages:** 1 to 100. **Length, width, height:** whole numbers from 1 to 300 cm.
- **Handling note:** required, 3 to 100 characters.
- **Numbers:** no leading zeros (type `5`, not `05`).
- If an input is wrong, the program says why and asks again. Where shown, type `0` to go back.
