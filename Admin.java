import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Manages events and venue records in memory. */
public class Admin {

    private int adminId;
    private String adminName;
    private String email;
    private final List<Event> events = new ArrayList<>();
    private final List<Venue> venues = new ArrayList<>();

    public Admin(int adminId, String adminName, String email) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.email = email;
    }

    public int getAdminId() { return adminId; }
    public String getAdminName() { return adminName; }
    public String getEmail() { return email; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setEmail(String email) { this.email = email; }

    public boolean addEvent(Event event) {
        if (event == null) {
            System.out.println("Cannot add an invalid event.");
            return false;
        }
        if (findEventById(event.getEventId()) != null) {
            System.out.println("An event with this ID already exists.");
            return false;
        }
        Venue venue = findVenueByName(event.getVenueName());
        if (venue != null && event.getMaxCapacity() > venue.getCapacity()) {
            System.out.println("Event allocation exceeds venue capacity.");
            return false;
        }
        events.add(event);
        System.out.println("Event added: " + event.getTitle());
        return true;
    }

    public void displayAllEvents() {
        if (events.isEmpty()) {
            System.out.println("No events available.");
            return;
        }
        System.out.println("\n===== ALL EVENTS =====");
        for (Event event : events) {
            event.displayEventDetails();  // Runtime polymorphism.
            System.out.println("----------------------------");
        }
    }

    public Event findEventById(int eventId) {
        for (Event event : events) {
            if (event.getEventId() == eventId) return event;
        }
        return null;
    }

    public void updateEvent(Event event, String newTitle) {
        if (event == null || !events.contains(event)) {
            System.out.println("Event not found.");
            return;
        }
        try {
            event.setTitle(newTitle);
            System.out.println("Event updated successfully.");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List<Event> getUpcomingEvents() {
        List<Event> upcoming = new ArrayList<>();
        for (Event event : events) {
            if (event.isUpcoming()) upcoming.add(event);
        }
        upcoming.sort(Comparator.comparing(Event::getEventDateTime));
        return Collections.unmodifiableList(upcoming);
    }

    public boolean updateEventSchedule(Event event, LocalDateTime newDateTime) {
        if (event == null || !events.contains(event)) {
            System.out.println("Event not found in admin records.");
            return false;
        }
        if (newDateTime == null || !newDateTime.isAfter(LocalDateTime.now())) {
            System.out.println("Please choose a future event date and time.");
            return false;
        }
        event.setEventDateTime(newDateTime);
        System.out.println("Event schedule updated successfully.");
        return true;
    }

    public boolean updateEventVenue(Event event, int venueId) {
        Venue venue = findVenueById(venueId);
        if (event == null || !events.contains(event) || venue == null) {
            System.out.println("Event or venue not found.");
            return false;
        }
        if (event.getMaxCapacity() > venue.getCapacity()) {
            System.out.println("Venue is too small for the event's current allocation.");
            return false;
        }
        event.setVenueName(venue.getVenueName());
        System.out.println("Event venue updated successfully.");
        return true;
    }

    public boolean updateEventPrice(Event event, double newPrice) {
        if (event == null || !events.contains(event)) {
            System.out.println("Event not found.");
            return false;
        }
        try {
            event.setBaseTicketPrice(newPrice);
            System.out.println("Event ticket price updated successfully.");
            return true;
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    // Resizing is allowed only before any seats have been sold or locked;
    // this keeps existing seat assignments and tickets valid.
    public boolean updateEventCapacity(Event event, int newCapacity) {
        if (event == null || !events.contains(event)) {
            System.out.println("Event not found.");
            return false;
        }
        Venue venue = findVenueByName(event.getVenueName());
        if (venue != null && newCapacity > venue.getCapacity()) {
            System.out.println("Ticket allocation cannot exceed venue capacity.");
            return false;
        }
        try {
            event.setMaxCapacity(newCapacity);
            System.out.println("Ticket allocation updated successfully.");
            return true;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public boolean removeEvent(int eventId) {
        Event event = findEventById(eventId);
        if (event == null) {
            System.out.println("Event not found.");
            return false;
        }
        TicketInventory inventory = event.getTicketInventory();
        if (inventory.getReservedTickets() > 0 || inventory.getLockedTickets() > 0) {
            System.out.println("Cannot remove an event with existing bookings or locked seats.");
            return false;
        }
        events.remove(event);
        System.out.println("Event removed: " + event.getTitle());
        return true;
    }

    public boolean addVenue(Venue venue) {
        if (venue == null) {
            System.out.println("Invalid venue.");
            return false;
        }
        if (findVenueById(venue.getVenueId()) != null ||
            findVenueByName(venue.getVenueName()) != null) {
            System.out.println("Venue ID or venue name already exists.");
            return false;
        }
        venues.add(venue);
        System.out.println("Venue added: " + venue.getVenueName());
        return true;
    }

    public Venue findVenueById(int venueId) {
        for (Venue venue : venues) {
            if (venue.getVenueId() == venueId) return venue;
        }
        return null;
    }

    private Venue findVenueByName(String name) {
        if (name == null) return null;
        for (Venue venue : venues) {
            if (venue.getVenueName().equalsIgnoreCase(name)) return venue;
        }
        return null;
    }

    public void displayAllVenues() {
        System.out.println("\n===== REGISTERED VENUES =====");
        if (venues.isEmpty()) {
            System.out.println("No venues available.");
            return;
        }
        for (Venue venue : venues) {
            venue.displayVenueDetails();
            System.out.println("----------------------------");
        }
    }

    public boolean updateVenue(int venueId, String name, String location, int capacity) {
        Venue venue = findVenueById(venueId);
        if (venue == null) {
            System.out.println("Venue not found.");
            return false;
        }
        Venue duplicate = findVenueByName(name);
        if (duplicate != null && duplicate != venue) {
            System.out.println("A venue with this name already exists.");
            return false;
        }
        for (Event event : events) {
            if (event.getVenueName().equalsIgnoreCase(venue.getVenueName()) &&
                event.getMaxCapacity() > capacity) {
                System.out.println("Venue capacity is below an assigned event's allocation.");
                return false;
            }
        }
        try {
            // Validate all inputs before changing the venue or its events.
            Venue proposed = new Venue(venueId, name, location, capacity);
            String oldName = venue.getVenueName();
            venue.setVenueName(proposed.getVenueName());
            venue.setLocation(proposed.getLocation());
            venue.setCapacity(proposed.getCapacity());
            for (Event event : events) {
                if (event.getVenueName().equalsIgnoreCase(oldName)) {
                    event.setVenueName(venue.getVenueName());
                }
            }
            System.out.println("Venue updated successfully.");
            return true;
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public boolean removeVenue(int venueId) {
        Venue venue = findVenueById(venueId);
        if (venue == null) {
            System.out.println("Venue not found.");
            return false;
        }
        for (Event event : events) {
            if (event.getVenueName().equalsIgnoreCase(venue.getVenueName())) {
                System.out.println("Cannot remove a venue assigned to an event.");
                return false;
            }
        }
        venues.remove(venue);
        System.out.println("Venue removed: " + venue.getVenueName());
        return true;
    }

    public void displayAdminDetails() {
        System.out.println("Admin ID: " + adminId);
        System.out.println("Admin Name: " + adminName);
        System.out.println("Email: " + email);
    }
}
