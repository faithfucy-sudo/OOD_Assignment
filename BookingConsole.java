import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interactive command-line demonstration for Member 2's booking features.
 * Runs entirely in memory and leaves Member 1's Main.java untouched.
 */
public class BookingConsole {

    private static int nextReservationNumber = 1;

    public static void main(String[] args) {
        // Member 1's existing classes supply the events and admin management.
        Admin admin = new Admin(301, "Event Administrator", "admin@example.com");
        admin.addEvent(new Event(101, "Jazz Night 2026", "KL Arena", 120.00, 500));
        admin.addEvent(new ConcertEvent(102, "Live Concert", "Bukit Jalil Stadium",
                180.00, 800, "Sample Performer", "Pop"));
        admin.addEvent(new ConferenceEvent(103, "AI Technology Summit", "MCKL Auditorium",
                150.00, 300, "Artificial Intelligence", true));

        // Demo schedules are relative to today, so the sample is always bookable.
        admin.updateEventSchedule(admin.findEventById(101),
                LocalDate.now().plusDays(20).atTime(19, 30));
        admin.updateEventSchedule(admin.findEventById(102),
                LocalDate.now().plusDays(35).atTime(20, 0));
        admin.updateEventSchedule(admin.findEventById(103),
                LocalDate.now().plusDays(45).atTime(9, 0));

        Attendee attendee = new Attendee("A001", "Demo Attendee", "demo@example.com");

        // Example promotional periods: event 102's early-bird offer is already over.
        // These are demo booking deadlines, NOT event schedule dates.
        Map<Integer, LocalDate> earlyBirdDeadlines = new HashMap<>();
        earlyBirdDeadlines.put(101, LocalDate.now().plusDays(7));
        earlyBirdDeadlines.put(102, LocalDate.now().minusDays(1));
        earlyBirdDeadlines.put(103, LocalDate.now().plusDays(14));

        Scanner input = new Scanner(System.in);
        System.out.println("\nWelcome to the Event Ticketing and Reservation System!");
        System.out.println("Demo attendee: " + attendee.getFullName());

        try {
            boolean running = true;
            while (running) {
                System.out.println("\n===== MAIN MENU =====");
                System.out.println("1. Browse upcoming event schedules");
                System.out.println("2. Reserve tickets and pay");
                System.out.println("3. View booking history / invoices");
                System.out.println("4. View my digital tickets");
                System.out.println("5. Admin: update event title (demo)");
                System.out.println("6. Admin: change event schedule (demo)");
                System.out.println("0. Exit");

                String choice = ask(input, "Choose an option: ");
                switch (choice) {
                    case "1":
                        attendee.inspectUpcomingSchedules(admin.getUpcomingEvents());
                        break;
                    case "2":
                        bookTickets(input, admin, attendee, earlyBirdDeadlines);
                        break;
                    case "3":
                        attendee.reviewBookingHistory();
                        break;
                    case "4":
                        showTickets(attendee);
                        break;
                    case "5":
                        updateTitle(input, admin);
                        break;
                    case "6":
                        updateSchedule(input, admin);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid menu choice. Please try again.");
                }
            }
        } catch (NoSuchElementException ex) {
            System.out.println("\nInput ended. Exiting safely.");
        }
    }

    private static void bookTickets(Scanner input, Admin admin, Attendee attendee,
                                    Map<Integer, LocalDate> deadlines) {
        attendee.inspectUpcomingSchedules(admin.getUpcomingEvents());
        int eventId = askPositiveInt(input, "Enter event ID: ");
        Event event = admin.findEventById(eventId);
        if (event == null) {
            System.out.println("Event not found.");
            return;
        }
        if (!event.isUpcoming()) {
            System.out.println("This event is not scheduled for the future.");
            return;
        }

        String type = ask(input, "Ticket type (STANDARD/VIP): ").toUpperCase();
        if (!type.equals("STANDARD") && !type.equals("VIP")) {
            System.out.println("Invalid ticket type.");
            return;
        }

        int quantity = askPositiveInt(input, "Number of tickets: ");
        String reservationId = String.format("R%03d", nextReservationNumber++);
        TicketReservation reservation;
        try {
            reservation = new TicketReservation(reservationId, event, quantity,
                                                type, deadlines.get(eventId));
        } catch (IllegalArgumentException ex) {
            System.out.println("Cannot create reservation: " + ex.getMessage());
            return;
        }

        // Seats must be locked before simulated payment takes place.
        if (!reservation.lockSeats()) {
            return;
        }

        try {
            System.out.println("Reservation: " + reservationId);
            System.out.println("Locked seats: " + reservation.getSeatAssignments());
            System.out.printf("Total price (discount included if eligible): RM%.2f%n",
                              reservation.getFinalPrice());
            System.out.println("This is a PAYMENT SIMULATION; no real money is charged.");

            while (true) {
                String outcome = ask(input,
                    "Payment outcome (S=success, F=failure, C=cancel): ").toUpperCase();
                if (outcome.equals("S")) {
                    if (attendee.makeReservation(reservation, true)) {
                        reservation.displayInvoice();
                        reservation.displayTickets();
                    }
                    return;
                }
                if (outcome.equals("F")) {
                    attendee.makeReservation(reservation, false);
                    return;
                }
                if (outcome.equals("C")) {
                    reservation.cancelReservation();
                    return;
                }
                System.out.println("Please enter S, F, or C.");
            }
        } finally {
            // Also release the hold if the user closes the input mid-checkout.
            if (reservation.getStatus().equals("SEATS_LOCKED")) {
                reservation.cancelReservation();
            }
        }
    }

    private static void showTickets(Attendee attendee) {
        if (attendee.getReservationHistory().isEmpty()) {
            System.out.println("No confirmed digital tickets yet.");
            return;
        }
        for (TicketReservation reservation : attendee.getReservationHistory()) {
            reservation.displayTickets();
        }
    }

    private static void updateTitle(Scanner input, Admin admin) {
        int eventId = askPositiveInt(input, "Enter event ID to update: ");
        Event event = admin.findEventById(eventId);
        if (event == null) {
            System.out.println("Event not found.");
            return;
        }
        String title = ask(input, "Enter new event title: ");
        admin.updateEvent(event, title);
    }

    // Admin's schedule workflow for the Activity and Sequence Diagrams.
    private static void updateSchedule(Scanner input, Admin admin) {
        int eventId = askPositiveInt(input, "Enter event ID to reschedule: ");
        Event event = admin.findEventById(eventId);
        if (event == null) {
            System.out.println("Event not found.");
            return;
        }
        String rawDate = ask(input, "New date/time (yyyy-MM-dd HH:mm): ");
        try {
            DateTimeFormatter pattern = DateTimeFormatter
                    .ofPattern("uuuu-MM-dd HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalDateTime newDate = LocalDateTime.parse(rawDate, pattern);
            admin.updateEventSchedule(event, newDate);
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date. Example: 2027-01-30 19:30");
        }
    }

    private static String ask(Scanner input, String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {
            throw new NoSuchElementException("No more input");
        }
        return input.nextLine().trim();
    }

    private static int askPositiveInt(Scanner input, String prompt) {
        while (true) {
            String text = ask(input, prompt);
            try {
                int number = Integer.parseInt(text);
                if (number > 0) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // Show a friendly prompt rather than crashing on non-numeric input.
            }
            System.out.println("Please enter a positive whole number.");
        }
    }
}
