import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketReservation {

    private final String reservationId;
    private final Event event;
    private final int quantity;
    private final String ticketType;
    private final LocalDate earlyBirdDeadline;

    private final List<String> seatAssignments = new ArrayList<>();
    private final List<DigitalTicket> digitalTickets = new ArrayList<>();
    private LocalDateTime bookingTimestamp;
    private final double finalPrice;
    private String status;

    public TicketReservation(String reservationId, Event event,
                             int quantity, String ticketType,
                             LocalDate earlyBirdDeadline) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }
        if (event == null || quantity <= 0) {
            throw new IllegalArgumentException("Valid event and ticket quantity required.");
        }
        if (ticketType == null ||
            (!ticketType.equalsIgnoreCase("STANDARD") &&
             !ticketType.equalsIgnoreCase("VIP"))) {
            throw new IllegalArgumentException("Ticket type must be STANDARD or VIP.");
        }
        if (ticketType.equalsIgnoreCase("VIP") &&
            event instanceof ConferenceEvent &&
            !((ConferenceEvent) event).isVipSeatingAvailable()) {
            throw new IllegalArgumentException(
                "VIP tickets are unavailable for this conference.");
        }

        this.reservationId = reservationId;
        this.event = event;
        this.quantity = quantity;
        this.ticketType = ticketType.toUpperCase();
        this.earlyBirdDeadline = earlyBirdDeadline;
        this.status = "PENDING";
        // Save the quote at booking creation instead of recalculating after payment.
        this.finalPrice = calculateFinalPrice();
    }

    public double calculateFinalPrice() {
        double multiplier = ticketType.equals("VIP") ? 1.5 : 1.0;
        double subtotal = event.getBaseTicketPrice() * quantity * multiplier;
        double discounted = applyEarlyBirdDiscount(subtotal);
        return Math.round(discounted * 100.0) / 100.0;
    }

    public double applyEarlyBirdDiscount(double amount) {
        if (earlyBirdDeadline != null &&
            !LocalDate.now().isAfter(earlyBirdDeadline)) {
            return amount * 0.90;
        }
        return amount;
    }

    // Step 1a: automatically allocate the first available seats (legacy flow).
    public boolean lockSeats() {
        return lockSeatRequest(null);
    }

    // Step 1b: allow the attendee to select exact seats in the console.
    public boolean lockSeats(List<String> requestedSeats) {
        if (requestedSeats == null || requestedSeats.size() != quantity) {
            System.out.println("Please select exactly " + quantity + " seat(s).");
            return false;
        }
        return lockSeatRequest(requestedSeats);
    }

    private boolean lockSeatRequest(List<String> requestedSeats) {
        if (!status.equals("PENDING")) {
            System.out.println("Cannot lock seats for a " + status + " reservation.");
            return false;
        }
        if (!event.isUpcoming()) {
            status = "EVENT_UNAVAILABLE";
            System.out.println("Event must have a future schedule to accept bookings.");
            return false;
        }
        List<String> heldSeats;
        try {
            heldSeats = requestedSeats == null
                ? event.getTicketInventory().lockSeats(reservationId, quantity)
                : event.getTicketInventory().lockSeats(reservationId, requestedSeats);
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid seat selection: " + ex.getMessage());
            return false;
        } catch (IllegalStateException ex) {
            status = "DUPLICATE_ID";
            System.out.println("Cannot lock seats: " + ex.getMessage());
            return false;
        }
        if (heldSeats.isEmpty()) {
            System.out.println("Not enough seats available. Please choose another selection.");
            return false;
        }
        seatAssignments.addAll(heldSeats);
        status = "SEATS_LOCKED";
        System.out.println("Seats locked: " + seatAssignments);
        return true;
    }

    // Step 2: simulated payment, then confirmation or lock release.
    // An implicit lock supports the earlier makeReservation() call pattern.
    public boolean confirmReservation(boolean paymentSuccessful) {
        if (status.equals("PENDING") && !lockSeats()) {
            return false;
        }
        if (!status.equals("SEATS_LOCKED")) {
            System.out.println("Reservation is already processed.");
            return false;
        }

        // An admin may have rescheduled the event during checkout.
        if (!event.isUpcoming()) {
            event.getTicketInventory().releaseLockedSeats(reservationId);
            seatAssignments.clear();
            status = "EVENT_UNAVAILABLE";
            System.out.println("Event is not upcoming. Held seats were released.");
            return false;
        }

        if (!paymentSuccessful) {
            event.getTicketInventory().releaseLockedSeats(reservationId);
            seatAssignments.clear();
            status = "PAYMENT_FAILED";
            System.out.println("Payment failed. Seats released.");
            return false;
        }

        if (!event.getTicketInventory().confirmLockedSeats(reservationId)) {
            status = "UNAVAILABLE";
            seatAssignments.clear();
            System.out.println("Unable to confirm seat lock.");
            return false;
        }

        // Composition: digital tickets are created only after confirmed payment.
        for (int i = 0; i < seatAssignments.size(); i++) {
            digitalTickets.add(new DigitalTicket(
                "T-" + reservationId + "-" + (i + 1),
                seatAssignments.get(i), ticketType));
        }
        bookingTimestamp = LocalDateTime.now();
        status = "CONFIRMED";
        System.out.println("Reservation confirmed: " + reservationId);
        return true;
    }

    // Release held seats when an attendee exits checkout without paying.
    public boolean cancelReservation() {
        if (!status.equals("SEATS_LOCKED")) {
            return false;
        }
        event.getTicketInventory().releaseLockedSeats(reservationId);
        seatAssignments.clear();
        status = "CANCELLED";
        System.out.println("Checkout cancelled. Seats released.");
        return true;
    }

    public void displayInvoice() {
        System.out.println("\n===== BOOKING INVOICE =====");
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Event: " + event.getTitle());
        System.out.println("Event Schedule: " + (event.getEventDateTime() == null
                ? "Not scheduled"
                : event.getEventDateTime().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))));
        System.out.println("Ticket Type: " + ticketType);
        System.out.println("Quantity: " + quantity);
        System.out.println("Status: " + status);
        System.out.println("Booking Time: " +
            (bookingTimestamp == null ? "Not confirmed" : bookingTimestamp));
        System.out.println("Seat(s): " + seatAssignments);
        System.out.printf("Final Price: RM%.2f%n", finalPrice);
        System.out.println("===========================");
    }

    public void displayTickets() {
        for (DigitalTicket ticket : digitalTickets) {
            ticket.displayTicket();
        }
    }

    public String getReservationId() { return reservationId; }
    public Event getEvent() { return event; }
    public String getStatus() { return status; }
    public LocalDateTime getBookingTimestamp() { return bookingTimestamp; }
    public double getFinalPrice() { return finalPrice; }
    public List<String> getSeatAssignments() {
        return Collections.unmodifiableList(seatAssignments);
    }
    public List<DigitalTicket> getDigitalTickets() {
        return Collections.unmodifiableList(digitalTickets);
    }
}
