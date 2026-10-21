import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interactive, entirely in-memory event booking and administration demo.
 * No real payment gateway or login/authentication is connected.
 */
public class BookingConsole {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter
        .ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static int nextReservationNumber = 1;

    public static void main(String[] args) {
        Admin admin = new Admin(301, "Event Administrator", "admin@example.com");
        admin.addVenue(new Venue(201, "KL Arena", "Kuala Lumpur", 1000));
        admin.addVenue(new Venue(202, "Bukit Jalil Stadium", "Kuala Lumpur", 1500));
        admin.addVenue(new Venue(203, "MCKL Auditorium", "Kuala Lumpur", 500));

        admin.addEvent(new Event(101, "Jazz Night 2026", "KL Arena", 120.00, 500));
        admin.addEvent(new ConcertEvent(102, "Live Concert", "Bukit Jalil Stadium",
            180.00, 800, "Sample Performer", "Pop"));
        admin.addEvent(new ConferenceEvent(103, "AI Technology Summit", "MCKL Auditorium",
            150.00, 300, "Artificial Intelligence", true));

        // Relative dates keep the demo usable regardless of when it is run.
        admin.updateEventSchedule(admin.findEventById(101),
            LocalDate.now().plusDays(20).atTime(19, 30));
        admin.updateEventSchedule(admin.findEventById(102),
            LocalDate.now().plusDays(35).atTime(20, 0));
        admin.updateEventSchedule(admin.findEventById(103),
            LocalDate.now().plusDays(45).atTime(9, 0));

        Attendee attendee = new Attendee("A001", "Demo Attendee", "demo@example.com");
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
                System.out.println("5. Admin management (demo; no login)");
                System.out.println("0. Exit");
                switch (ask(input, "Choose an option: ")) {
                    case "1": attendee.inspectUpcomingSchedules(admin.getUpcomingEvents()); break;
                    case "2": bookTickets(input, admin, attendee, earlyBirdDeadlines); break;
                    case "3": attendee.reviewBookingHistory(); break;
                    case "4": showTickets(attendee); break;
                    case "5": adminMenu(input, admin, earlyBirdDeadlines); break;
                    case "0": running = false; System.out.println("Goodbye!"); break;
                    default: System.out.println("Invalid menu choice. Please try again.");
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
        if (event == null || !event.isUpcoming()) {
            System.out.println("Event not found or not upcoming.");
            return;
        }
        String type = ask(input, "Ticket type (STANDARD/VIP): ").toUpperCase();
        if (!type.equals("STANDARD") && !type.equals("VIP")) {
            System.out.println("Invalid ticket type.");
            return;
        }
        int quantity = askPositiveInt(input, "Number of tickets: ");
        if (quantity > event.getAvailableSeats()) {
            System.out.println("Not enough seats available. Remaining: " + event.getAvailableSeats());
            return;
        }
        String reservationId = String.format("R%03d", nextReservationNumber++);
        TicketReservation reservation;
        try {
            reservation = new TicketReservation(reservationId, event, quantity, type,
                deadlines.get(eventId));
        } catch (IllegalArgumentException ex) {
            System.out.println("Cannot create reservation: " + ex.getMessage());
            return;
        }

        // The attendee can choose exact seats, or request automatic assignment.
        boolean locked = false;
        while (!locked) {
            TicketInventory inventory = event.getTicketInventory();
            System.out.println("Available seats (first 25): " +
                inventory.previewAvailableSeats(25));
            System.out.println("Total available: " + event.getAvailableSeats());
            String seatText = ask(input,
                "Select " + quantity + " seat(s) (e.g. S1,S2), AUTO, or C to cancel: ");
            if (seatText.equalsIgnoreCase("C")) {
                System.out.println("Booking cancelled before checkout.");
                return;
            }
            if (seatText.equalsIgnoreCase("AUTO")) {
                locked = reservation.lockSeats();
            } else {
                List<String> seatIds = Arrays.asList(seatText.split(",", -1));
                locked = reservation.lockSeats(seatIds);
            }
            if (!locked && !reservation.getStatus().equals("PENDING")) return;
            if (!locked && event.getAvailableSeats() < quantity) {
                System.out.println("Not enough remaining seats to continue.");
                return;
            }
        }

        try {
            System.out.println("\n===== CHECKOUT =====");
            System.out.println("Reservation: " + reservationId);
            System.out.println("Selected seats: " + reservation.getSeatAssignments());
            System.out.printf("Final price (early-bird discount if eligible): RM%.2f%n",
                reservation.getFinalPrice());
            System.out.println("PAYMENT SIMULATION ONLY: no money is charged.");
            while (true) {
                String outcome = ask(input, "Payment outcome (S=success, F=failure, C=cancel): ")
                    .toUpperCase();
                switch (outcome) {
                    case "S":
                        if (attendee.makeReservation(reservation, true)) {
                            reservation.displayInvoice();
                            reservation.displayTickets();
                        }
                        return;
                    case "F": attendee.makeReservation(reservation, false); return;
                    case "C": reservation.cancelReservation(); return;
                    default: System.out.println("Please enter S, F, or C.");
                }
            }
        } finally {
            // Ensure checkout abandonment does not permanently block seats.
            if (reservation.getStatus().equals("SEATS_LOCKED")) reservation.cancelReservation();
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

    private static void adminMenu(Scanner input, Admin admin,
                                  Map<Integer, LocalDate> deadlines) {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Display all events");
            System.out.println("2. Add event");
            System.out.println("3. Update event title");
            System.out.println("4. Update event schedule");
            System.out.println("5. Change event venue");
            System.out.println("6. Update ticket price");
            System.out.println("7. Update ticket allocation/capacity");
            System.out.println("8. Remove event");
            System.out.println("9. Display venues");
            System.out.println("10. Add venue");
            System.out.println("11. Update venue");
            System.out.println("12. Remove venue");
            System.out.println("0. Back to main menu");
            String action = ask(input, "Choose an option: ");
            switch (action) {
                case "1": admin.displayAllEvents(); break;
                case "2": addEvent(input, admin, deadlines); break;
                case "3": updateTitle(input, admin); break;
                case "4": updateSchedule(input, admin); break;
                case "5": updateEventVenue(input, admin); break;
                case "6": updatePrice(input, admin); break;
                case "7": updateCapacity(input, admin); break;
                case "8": removeEvent(input, admin, deadlines); break;
                case "9": admin.displayAllVenues(); break;
                case "10": addVenue(input, admin); break;
                case "11": updateVenue(input, admin); break;
                case "12": admin.removeVenue(askPositiveInt(input, "Venue ID to remove: ")); break;
                case "0": return;
                default: System.out.println("Invalid menu choice.");
            }
        }
    }

    private static void addEvent(Scanner input, Admin admin,
                                 Map<Integer, LocalDate> deadlines) {
        int id = askPositiveInt(input, "New event ID: ");
        if (admin.findEventById(id) != null) {
            System.out.println("An event with that ID already exists.");
            return;
        }
        String title = askNonBlank(input, "Event title: ");
        admin.displayAllVenues();
        Venue venue = admin.findVenueById(askPositiveInt(input, "Venue ID: "));
        if (venue == null) {
            System.out.println("Venue not found. Register it first.");
            return;
        }
        double price = askPrice(input, "Base ticket price (RM): ");
        int allocation = askPositiveInt(input, "Ticket allocation: ");
        if (allocation > venue.getCapacity()) {
            System.out.println("Ticket allocation exceeds the venue capacity.");
            return;
        }
        String kind = ask(input, "Event type (1=General, 2=Concert, 3=Conference): ");
        Event event;
        switch (kind) {
            case "1": event = new Event(id, title, venue.getVenueName(), price, allocation); break;
            case "2":
                String performer = askNonBlank(input, "Performer: ");
                String genre = askNonBlank(input, "Genre: ");
                event = new ConcertEvent(id, title, venue.getVenueName(), price,
                    allocation, performer, genre);
                break;
            case "3":
                String topic = askNonBlank(input, "Conference topic: ");
                boolean vip = askYesNo(input, "VIP seating available? (Y/N): ");
                event = new ConferenceEvent(id, title, venue.getVenueName(), price,
                    allocation, topic, vip);
                break;
            default:
                System.out.println("Invalid event type.");
                return;
        }
        LocalDateTime schedule = askFutureDateTime(input, "Event schedule (yyyy-MM-dd HH:mm): ");
        event.setEventDateTime(schedule);
        if (admin.addEvent(event)) {
            LocalDate deadline = askEarlyBirdDeadline(input, schedule.toLocalDate());
            if (deadline != null) deadlines.put(id, deadline);
        }
    }

    private static void updateTitle(Scanner input, Admin admin) {
        Event event = selectEvent(input, admin);
        if (event != null) admin.updateEvent(event, askNonBlank(input, "New event title: "));
    }

    private static void updateSchedule(Scanner input, Admin admin) {
        Event event = selectEvent(input, admin);
        if (event != null) admin.updateEventSchedule(event,
            askFutureDateTime(input, "New date/time (yyyy-MM-dd HH:mm): "));
    }

    private static void updateEventVenue(Scanner input, Admin admin) {
        Event event = selectEvent(input, admin);
        if (event == null) return;
        admin.displayAllVenues();
        admin.updateEventVenue(event, askPositiveInt(input, "New venue ID: "));
    }

    private static void updatePrice(Scanner input, Admin admin) {
        Event event = selectEvent(input, admin);
        if (event != null) admin.updateEventPrice(event, askPrice(input, "New base price (RM): "));
    }

    private static void updateCapacity(Scanner input, Admin admin) {
        Event event = selectEvent(input, admin);
        if (event == null) return;
        System.out.println("Capacity changes require no booked or locked seats.");
        admin.updateEventCapacity(event, askPositiveInt(input, "New ticket allocation: "));
    }

    private static void removeEvent(Scanner input, Admin admin,
                                    Map<Integer, LocalDate> deadlines) {
        int id = askPositiveInt(input, "Event ID to remove: ");
        if (admin.removeEvent(id)) deadlines.remove(id);
    }

    private static void addVenue(Scanner input, Admin admin) {
        int id = askPositiveInt(input, "New venue ID: ");
        String name = askNonBlank(input, "Venue name: ");
        String location = askNonBlank(input, "Location: ");
        int capacity = askPositiveInt(input, "Venue capacity: ");
        admin.addVenue(new Venue(id, name, location, capacity));
    }

    private static void updateVenue(Scanner input, Admin admin) {
        admin.displayAllVenues();
        int id = askPositiveInt(input, "Venue ID to update: ");
        if (admin.findVenueById(id) == null) {
            System.out.println("Venue not found.");
            return;
        }
        String name = askNonBlank(input, "Updated venue name: ");
        String location = askNonBlank(input, "Updated location: ");
        int capacity = askPositiveInt(input, "Updated venue capacity: ");
        admin.updateVenue(id, name, location, capacity);
    }

    private static Event selectEvent(Scanner input, Admin admin) {
        int eventId = askPositiveInt(input, "Event ID: ");
        Event event = admin.findEventById(eventId);
        if (event == null) System.out.println("Event not found.");
        return event;
    }

    private static LocalDateTime askFutureDateTime(Scanner input, String prompt) {
        while (true) {
            try {
                LocalDateTime date = LocalDateTime.parse(ask(input, prompt), DATE_TIME_FORMAT);
                if (date.isAfter(LocalDateTime.now())) return date;
            } catch (DateTimeParseException ex) {
                // Show the same message for invalid dates and past dates.
            }
            System.out.println("Enter a valid FUTURE date/time, e.g. 2027-01-30 19:30.");
        }
    }

    private static LocalDate askEarlyBirdDeadline(Scanner input, LocalDate eventDate) {
        while (true) {
            String text = ask(input, "Early-bird deadline (yyyy-MM-dd or Enter for none): ");
            if (text.isEmpty()) return null;
            try {
                LocalDate date = LocalDate.parse(text, DATE_FORMAT);
                if (!date.isBefore(LocalDate.now()) && !date.isAfter(eventDate)) return date;
            } catch (DateTimeParseException ex) {
                // Ask again.
            }
            System.out.println("Deadline must be a valid date from today through event day.");
        }
    }

    private static boolean askYesNo(Scanner input, String prompt) {
        while (true) {
            String value = ask(input, prompt);
            if (value.equalsIgnoreCase("Y")) return true;
            if (value.equalsIgnoreCase("N")) return false;
            System.out.println("Please enter Y or N.");
        }
    }

    private static String askNonBlank(Scanner input, String prompt) {
        while (true) {
            String value = ask(input, prompt);
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be blank.");
        }
    }

    private static double askPrice(Scanner input, String prompt) {
        while (true) {
            try {
                double price = Double.parseDouble(ask(input, prompt));
                if (Double.isFinite(price) && price >= 0) return price;
            } catch (NumberFormatException ex) {
                // Ask again.
            }
            System.out.println("Enter a valid non-negative price.");
        }
    }

    private static String ask(Scanner input, String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) throw new NoSuchElementException("Input ended");
        return input.nextLine().trim();
    }

    private static int askPositiveInt(Scanner input, String prompt) {
        while (true) {
            try {
                int value = Integer.parseInt(ask(input, prompt));
                if (value > 0) return value;
            } catch (NumberFormatException ex) {
                // Ask again.
            }
            System.out.println("Please enter a positive whole number.");
        }
    }
}
