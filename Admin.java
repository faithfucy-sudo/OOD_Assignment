import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Admin {

    // 1. Attributes
    private int adminId;
    private String adminName;
    private String email;
    private List<Event> events = new ArrayList<>();

    // 2. Constructor
    public Admin(int adminId, String adminName, String email) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.email = email;
    }

    // 3. Getters
    public int getAdminId() {
        return adminId;
    }
    public String getAdminName() {
        return adminName;
    }
    public String getEmail() {
        return email;
    }

    // 4. Setters
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // 5. Add event
    public void addEvent(Event event) {
        if (event == null) {
            System.out.println("Cannot add an invalid event.");
            return;
        }

        if (findEventById(event.getEventId()) != null) {
            System.out.println("An event with this ID already exists.");
            return;
        }

        events.add(event);
        System.out.println("Event added: " + event.getTitle());
    }

    // 6. Display all events
    public void displayAllEvents() {
        if (events.isEmpty()) {
            System.out.println("No events available.");
            return;
        }

        System.out.println("All Events:");

        for (Event event : events) {
            event.displayEventDetails();
            System.out.println();
        }
    }

    // 7. Find event by ID
    public Event findEventById(int eventId) {
        for (Event event : events) {
            if (event.getEventId() == eventId) {
                return event;
            }
        }
        return null;
    }

    // 8. Update event
    public void updateEvent(Event event, String newTitle) {
        if (event == null || !events.contains(event)) {
            System.out.println("Event not found.");
            return;
        }

        if (newTitle == null || newTitle.trim().isEmpty()) {
            System.out.println("Event title cannot be empty.");
            return;
        }

        event.setTitle(newTitle);
        System.out.println("Event updated successfully.");
    }

    // 9. Filter and order events by their upcoming schedule.
    public List<Event> getUpcomingEvents() {
        List<Event> upcoming = new ArrayList<>();
        for (Event event : events) {
            if (event.isUpcoming()) {
                upcoming.add(event);
            }
        }
        upcoming.sort(Comparator.comparing(Event::getEventDateTime));
        return Collections.unmodifiableList(upcoming);
    }

    // 10. Admin manages an existing event's date and time.
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

    // 11. Remove event
    public void removeEvent(int eventId) {
        Event event = findEventById(eventId);

        if (event == null) {
            System.out.println("Event not found.");
            return;
        }

        events.remove(event);
        System.out.println("Event removed: " + event.getTitle());
    }

    // 10. Display admin details
    public void displayAdminDetails() {
        System.out.println("Admin ID: " + adminId);
        System.out.println("Admin Name: " + adminName);
        System.out.println("Email: " + email);
    }
}
