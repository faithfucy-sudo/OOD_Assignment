import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks individual seats for an event, including temporary checkout locks.
 * Everything is kept in memory for this command-line assignment.
 */
public class TicketInventory {
    private final int totalTickets;
    private final Set<Integer> reservedSeats = new HashSet<>();
    private final Set<Integer> lockedSeats = new HashSet<>();
    private final Map<String, List<Integer>> locksByReservation = new HashMap<>();
    // Do not reuse an ID for another attempt on the same event.
    private final Set<String> usedReservationIds = new HashSet<>();

    public TicketInventory(int totalTickets) {
        if (totalTickets <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.totalTickets = totalTickets;
    }

    public int getTotalTickets() { return totalTickets; }

    public synchronized int getAvailableTickets() {
        return totalTickets - reservedSeats.size() - lockedSeats.size();
    }

    public synchronized int getReservedTickets() {
        return reservedSeats.size();
    }

    public synchronized int getLockedTickets() {
        return lockedSeats.size();
    }

    // Used by Member 1's existing Event.reserveSeats(quantity) method.
    // Allocates the first available seat numbers immediately.
    public synchronized boolean reserveTickets(int quantity) {
        if (quantity <= 0 || quantity > getAvailableTickets()) {
            return false;
        }
        List<Integer> free = findFreeSeats(quantity);
        reservedSeats.addAll(free);
        return true;
    }

    // Hold seats during checkout, before payment is confirmed.
    // Returns an empty list when there are insufficient seats.
    public synchronized List<String> lockSeats(String reservationId, int quantity) {
        if (reservationId == null || reservationId.trim().isEmpty() || quantity <= 0) {
            throw new IllegalArgumentException("Valid reservation ID and quantity required.");
        }
        if (usedReservationIds.contains(reservationId)) {
            throw new IllegalStateException(
                "Reservation ID already used for this event: " + reservationId);
        }
        if (quantity > getAvailableTickets()) {
            return Collections.emptyList();
        }

        List<Integer> seatNumbers = findFreeSeats(quantity);
        locksByReservation.put(reservationId, seatNumbers);
        usedReservationIds.add(reservationId);
        lockedSeats.addAll(seatNumbers);
        return seatLabels(seatNumbers);
    }

    // Hold the exact seat labels selected by an attendee (e.g. S1, S12).
    // Validation takes place before any seat is locked, so an invalid request
    // cannot leave half of a group booking locked.
    public synchronized List<String> lockSeats(String reservationId, List<String> requestedSeats) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }
        if (usedReservationIds.contains(reservationId)) {
            throw new IllegalStateException("Reservation ID already used: " + reservationId);
        }
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            throw new IllegalArgumentException("Select at least one seat.");
        }

        List<Integer> seatNumbers = new ArrayList<>();
        Set<Integer> requestedNumbers = new HashSet<>();
        for (String label : requestedSeats) {
            if (label == null || !label.trim().toUpperCase().matches("S[0-9]+")) {
                throw new IllegalArgumentException("Use seat IDs such as S1 or S25.");
            }
            int number;
            try {
                number = Integer.parseInt(label.trim().substring(1));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid seat number: " + label);
            }
            if (number < 1 || number > totalTickets) {
                throw new IllegalArgumentException("Seat S" + number + " does not exist.");
            }
            if (!requestedNumbers.add(number)) {
                throw new IllegalArgumentException("Seat S" + number + " was selected twice.");
            }
            if (reservedSeats.contains(number) || lockedSeats.contains(number)) {
                throw new IllegalArgumentException("Seat S" + number + " is already taken.");
            }
            seatNumbers.add(number);
        }

        locksByReservation.put(reservationId, seatNumbers);
        usedReservationIds.add(reservationId);
        lockedSeats.addAll(seatNumbers);
        return seatLabels(seatNumbers);
    }

    // A short preview avoids printing hundreds of seat IDs at once.
    public synchronized List<String> previewAvailableSeats(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return seatLabels(findFreeSeats(limit));
    }

    // Only a successful payment converts the lock into a confirmed booking.
    public synchronized boolean confirmLockedSeats(String reservationId) {
        List<Integer> seatNumbers = locksByReservation.remove(reservationId);
        if (seatNumbers == null) {
            return false;
        }
        lockedSeats.removeAll(seatNumbers);
        reservedSeats.addAll(seatNumbers);
        return true;
    }

    // Payment failure makes those seats available again.
    public synchronized void releaseLockedSeats(String reservationId) {
        List<Integer> seatNumbers = locksByReservation.remove(reservationId);
        if (seatNumbers != null) {
            lockedSeats.removeAll(seatNumbers);
        }
    }

    private List<Integer> findFreeSeats(int quantity) {
        List<Integer> seats = new ArrayList<>();
        for (int number = 1; number <= totalTickets && seats.size() < quantity; number++) {
            if (!reservedSeats.contains(number) && !lockedSeats.contains(number)) {
                seats.add(number);
            }
        }
        return seats;
    }

    private List<String> seatLabels(List<Integer> seatNumbers) {
        List<String> result = new ArrayList<>();
        for (Integer number : seatNumbers) {
            result.add("S" + number);
        }
        return Collections.unmodifiableList(result);
    }
}
