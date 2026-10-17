import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Event {

    // 1. Attributes
    private int eventId;
    private String title;
    private String venueName;
    private double baseTicketPrice;
    private int maxCapacity;
    // Optional date/time so earlier Member 1 tests and constructors still work.
    private LocalDateTime eventDateTime;
    // TicketInventory is the single source of truth for available seats.
    private TicketInventory ticketInventory;

    // 2. Constructor
    public Event(int eventId, String title, String venueName,
                 double baseTicketPrice, int maxCapacity) {
        this.eventId = eventId;
        this.title = title;
        this.venueName = venueName;
        validatePrice(baseTicketPrice);
        this.baseTicketPrice = baseTicketPrice;
        this.maxCapacity = maxCapacity;
        this.ticketInventory = new TicketInventory(maxCapacity);
    }

    // 3. Getters
    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getVenueName() { return venueName; }
    public double getBaseTicketPrice() { return baseTicketPrice; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getAvailableSeats() { return ticketInventory.getAvailableTickets(); }
    public TicketInventory getTicketInventory() { return ticketInventory; }
    public LocalDateTime getEventDateTime() { return eventDateTime; }

    public boolean isUpcoming() {
        return eventDateTime != null && eventDateTime.isAfter(LocalDateTime.now());
    }

    // 4. Setters
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setTitle(String title) { this.title = title; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public void setBaseTicketPrice(double baseTicketPrice) {
        validatePrice(baseTicketPrice);
        this.baseTicketPrice = baseTicketPrice;
    }

    private static void validatePrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException(
                "Ticket price must be zero or a positive amount.");
        }
    }

    // A recorded event may have a past date; admin updates are validated separately.
    public void setEventDateTime(LocalDateTime eventDateTime) {
        if (eventDateTime == null) {
            throw new IllegalArgumentException("Event date and time cannot be empty.");
        }
        this.eventDateTime = eventDateTime;
    }

    public void setMaxCapacity(int maxCapacity) {
        // Never reset inventory while seats are reserved or locked.
        if (ticketInventory.getReservedTickets() > 0 ||
            ticketInventory.getLockedTickets() > 0) {
            throw new IllegalStateException(
                "Cannot change capacity while seats are reserved or locked.");
        }
        this.ticketInventory = new TicketInventory(maxCapacity);
        this.maxCapacity = maxCapacity;
    }

    // 5. Reserve seats (keeps Member 1's original method signature)
    public void reserveSeats(int numberOfSeats) {
        if (!ticketInventory.reserveTickets(numberOfSeats)) {
            System.out.println("Invalid reservation or not enough seats.");
        }
    }

    // 6. Display event details
    public void displayEventDetails() {
        System.out.println("Event ID: " + eventId);
        System.out.println("Event Title: " + title);
        System.out.println("Venue: " + venueName);
        System.out.println("Schedule: " + (eventDateTime == null
                ? "Not scheduled"
                : eventDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))));
        System.out.println("Ticket Price: RM" + baseTicketPrice);
        System.out.println("Maximum Capacity: " + maxCapacity);
        System.out.println("Available Seats: " + getAvailableSeats());
    }
}
