public class Venue {

    // 1. Attributes
    private int venueId;
    private String venueName;
    private String location;
    private int capacity;

    // 2. Constructor
    public Venue(int venueId, String venueName,
                 String location, int capacity) {
        setVenueId(venueId);
        setVenueName(venueName);
        setLocation(location);
        setCapacity(capacity);
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
        if (venueId <= 0) throw new IllegalArgumentException("Venue ID must be positive.");
        this.venueId = venueId;
    }
    public void setVenueName(String venueName) {
        if (venueName == null || venueName.trim().isEmpty())
            throw new IllegalArgumentException("Venue name cannot be empty.");
        this.venueName = venueName.trim();
    }
    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty())
            throw new IllegalArgumentException("Venue location cannot be empty.");
        this.location = location.trim();
    }
    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Venue capacity must be positive.");
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
