public class ConferenceEvent extends Event {

    // 1. Attributes
    private String conferenceTopic;
    private boolean vipSeatingAvailable;

    // 2. Constructor
    public ConferenceEvent(int eventId, String title, String venueName,
                           double baseTicketPrice, int maxCapacity,
                           String conferenceTopic, boolean vipSeatingAvailable) {

        super(eventId, title, venueName, baseTicketPrice, maxCapacity);
        this.conferenceTopic = conferenceTopic;
        this.vipSeatingAvailable = vipSeatingAvailable;
    }

    // 3. Getters
    public String getConferenceTopic() {
        return conferenceTopic;
    }

    public boolean isVipSeatingAvailable() {
        return vipSeatingAvailable;
    }

    // 4. Setters
    public void setConferenceTopic(String conferenceTopic) {
        this.conferenceTopic = conferenceTopic;
    }

    public void setVipSeatingAvailable(boolean vipSeatingAvailable) {
        this.vipSeatingAvailable = vipSeatingAvailable;
    }

    // 5. Display conference details
    public void displayConferenceDetails() {
        System.out.println("Conference Topic: " + conferenceTopic);
        System.out.println("VIP Seating Available: " + vipSeatingAvailable);
    }
}

