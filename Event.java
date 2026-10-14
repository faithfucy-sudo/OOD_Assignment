class Event {

    // 1. Attributes
    private int eventId;
    private String title;
    private String venueName;
    private double baseTicketPrice;
    private int maxCapacity;
    private int availableSeats;

    // 2. Constructor
    public Event(int eventId, String title, String venueName,
                 double baseTicketPrice, int maxCapacity) {

        this.eventId = eventId;
        this.title = title;
        this.venueName = venueName;
        this.baseTicketPrice = baseTicketPrice;
        this.maxCapacity = maxCapacity;
        this.availableSeats = maxCapacity;
    }

    // 3. Getters
    public int getEventId() {
        return eventId;
    }
    public String getTitle() {
        return title;
    }
    public String getVenueName() {
        return venueName;
    }
    public double getBaseTicketPrice() {
        return baseTicketPrice;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public int getAvailableSeats() {
        return availableSeats;
    }

    // 4. Setters
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    public void setBaseTicketPrice(double baseTicketPrice) {
        this.baseTicketPrice = baseTicketPrice;
    }
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    // 5. Reserve seats
    public void reserveSeats(int numberOfSeats) {
        if (numberOfSeats > 0 && numberOfSeats <= availableSeats) {
            availableSeats = availableSeats - numberOfSeats;
        } else {
            System.out.println(
                "Invalid reservation or not enough seats."
            );
        }
    }

    // 6. Display event details
    public void displayEventDetails() {
        System.out.println("Event ID: " + eventId);
        System.out.println("Event Title: " + title);
        System.out.println("Venue: " + venueName);
        System.out.println("Ticket Price: RM" + baseTicketPrice);
        System.out.println("Maximum Capacity: " + maxCapacity);
        System.out.println("Available Seats: " + availableSeats);
    }
}


