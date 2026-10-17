public class Main {

    public static void main(String[] args) {

        // 1. Create a normal Event
        Event event = new Event(
            101, "Jazz Night", "KL Arena", 120.00, 500
        );

        event.displayEventDetails();

        // 2. Reserve seats
        System.out.println("\nEvent Reservation:");
        event.reserveSeats(100);
        System.out.println("Available Seats: " + event.getAvailableSeats());


        // 3. Create a ConcertEvent
        ConcertEvent concert = new ConcertEvent(
            102, "Live Concert", "Bukit Jalil Stadium",
            180.00, 800, "Taylor Swift", "Pop"
        );

        System.out.println("\nConcert Details:");
        concert.displayEventDetails(); // overridden method also prints performer and genre


        // 4. Create a ConferenceEvent
        ConferenceEvent conference = new ConferenceEvent(
            103, "AI Technology Summit", "MCKL Auditorium",
            150.00, 300, "Artificial Intelligence", true
        );

        System.out.println("\nConference Details:");
        conference.displayEventDetails(); // overridden method also prints conference details


        // 5. Create a Venue
        Venue venue = new Venue(
            201, "KL Arena", "Kuala Lumpur", 1000
        );

        System.out.println("\nVenue Details:");
        venue.displayVenueDetails();


        // 6. Create an Admin
        Admin admin = new Admin(
            301, "Faith FU", "faith@gmail.com"
        );

        System.out.println("\nAdmin Details:");
        admin.displayAdminDetails();


        // 7. Admin adds and updates an event
        System.out.println("\nAdmin Event Management:");
        admin.addEvent(event);
        admin.updateEvent(event, "Jazz Night 2026");

        // 8. Display updated event details
        event.displayEventDetails();
    }
}

