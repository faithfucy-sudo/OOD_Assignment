import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Attendee {

    // 1. Attendee details (encapsulation)
    private final String accountUserKey;
    private final String fullName;
    private final String email;
    private int loyaltyPoints;

    // Aggregation: an attendee keeps references to existing reservations.
    private final List<TicketReservation> reservationHistory = new ArrayList<>();

    // 2. Constructor
    public Attendee(String accountUserKey, String fullName, String email) {
        if (accountUserKey == null || accountUserKey.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Attendee key, name and email are required.");
        }

        this.accountUserKey = accountUserKey;
        this.fullName = fullName;
        this.email = email;
        this.loyaltyPoints = 0;
    }

    // 3. Inspect only upcoming events in event-date order.
    public void inspectUpcomingSchedules(List<Event> events) {
        System.out.println("\n===== AVAILABLE EVENTS =====");
        if (events == null || events.isEmpty()) {
            System.out.println("No upcoming events available.");
            return;
        }
        List<Event> upcoming = new ArrayList<>();
        for (Event event : events) {
            if (event != null && event.isUpcoming()) {
                upcoming.add(event);
            }
        }
        upcoming.sort((a, b) -> a.getEventDateTime().compareTo(b.getEventDateTime()));
        if (upcoming.isEmpty()) {
            System.out.println("No upcoming events available.");
            return;
        }
        for (Event event : upcoming) {
            event.displayEventDetails();
            System.out.println("----------------------------");
        }
    }

    // 4. Make a booking using an independently created reservation.
    // The reservation is added to this attendee's history only if confirmed.
    public boolean makeReservation(TicketReservation reservation,
                                   boolean paymentSuccessful) {
        if (reservation == null) {
            System.out.println("No reservation was provided.");
            return false;
        }

        if (reservation.confirmReservation(paymentSuccessful)) {
            reservationHistory.add(reservation);
            System.out.println("Booking added to " + fullName + "'s history.");
            return true;
        }
        return false;
    }

    // 5. View invoices for confirmed bookings.
    public void reviewBookingHistory() {
        System.out.println("\n===== BOOKING HISTORY: " + fullName + " =====");
        if (reservationHistory.isEmpty()) {
            System.out.println("No confirmed bookings yet.");
            return;
        }
        for (TicketReservation reservation : reservationHistory) {
            reservation.displayInvoice();
        }
    }

    // 6. Loyalty points can be awarded by an agreed group pricing rule later.
    public void addLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative.");
        }
        loyaltyPoints += points;
    }

    // 7. Getters
    public String getAccountUserKey() { return accountUserKey; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public int getLoyaltyPoints() { return loyaltyPoints; }

    // Return a read-only view to protect the reservation list.
    public List<TicketReservation> getReservationHistory() {
        return Collections.unmodifiableList(reservationHistory);
    }
}
