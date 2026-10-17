
public class DigitalTicket {

    // 1. Attributes
    private String ticketId;
    private String seatNumber;
    private String ticketType;
    private String qrCode;

    // 2. Constructor
    public DigitalTicket(String ticketId, String seatNumber,
                         String ticketType) {
        this.ticketId = ticketId;
        this.seatNumber = seatNumber;
        this.ticketType = ticketType;
        generateTicket();
    }

    // 3. Getters
    public String getTicketId() {
        return ticketId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getQrCode() {
        return qrCode;
    }

    // 4. Generate ticket reference code
    public void generateTicket() {
        qrCode = "QR-" + ticketId + "-" + seatNumber;
    }

    // 5. Display digital ticket
    public void displayTicket() {
        System.out.println("\n===== DIGITAL TICKET =====");
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Seat Number: " + seatNumber);
        System.out.println("Ticket Type: " + ticketType);
        System.out.println("QR Reference: " + qrCode);
        System.out.println("==========================");
    }
}
