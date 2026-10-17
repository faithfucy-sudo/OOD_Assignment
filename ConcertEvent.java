public class ConcertEvent extends Event {

    // 1. Attributes
    private String performerName;
    private String genre;

    // 2. Constructor
    public ConcertEvent(int eventId, String title, String venueName,
                        double baseTicketPrice, int maxCapacity,
                        String performerName, String genre) {

        super(eventId, title, venueName, baseTicketPrice, maxCapacity);
        this.performerName = performerName;
        this.genre = genre;
    }

    // 3. Getters
    public String getPerformerName() {
        return performerName;
    }
    public String getGenre() {
        return genre;
    }

    // 4. Setters
    public void setPerformerName(String performerName) {
        this.performerName = performerName;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Polymorphism: an Event reference displays concert-specific information.
    @Override
    public void displayEventDetails() {
        super.displayEventDetails();
        displayConcertDetails();
    }

    // 5. Display concert details
    public void displayConcertDetails() {
        System.out.println("Performer Name: " + performerName);
        System.out.println("Genre: " + genre);
    }    
}
