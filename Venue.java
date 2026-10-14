
public class Venue {

    // 1. Attributes
    private int venueId;
    private String venueName;
    private String location;
    private int capacity;

    // 2. Constructor
    public Venue(int venueId, String venueName,
                 String location, int capacity) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.location = location;
        this.capacity = capacity;
    }

    // 3. Getters
    public int getVenueId() {
        return venueId;
    }
    public String getVenueName() {
        return venueName;
    }
    public String getLocation() {
        return location;
    }
    public int getCapacity() {
        return capacity;
    }

    // 4. Setters
    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // 5. Display venue details
    public void displayVenueDetails() {
        System.out.println("Venue ID: " + venueId);
        System.out.println("Venue Name: " + venueName);
        System.out.println("Location: " + location);
        System.out.println("Capacity: " + capacity);
    }
}
