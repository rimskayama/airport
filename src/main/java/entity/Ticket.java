package entity;

public class Ticket {
    private String ticketNumber;
    private Passenger passenger;
    private Fare fare;
    private String purchaseDate;
    private double finalPrice;

    public Ticket(
            String ticketNumber, Passenger passenger, Fare fare,
            String purchaseDate, double finalPrice) {
        this.ticketNumber = ticketNumber;
        this.passenger = passenger;
        this.fare = fare;
        this.purchaseDate = purchaseDate;
        this.finalPrice = finalPrice;
    }

    public String getTicketNumber() { return ticketNumber; }
    public Passenger getPassenger() { return passenger; }
    public Fare getFare() { return fare; }
    public String getPurchaseDate() { return purchaseDate; }
    public double getPrice() { return finalPrice; }

    @Override
    public String toString() {
        return "Билет " + ticketNumber + " | " + passenger.getName() +
                " | " + fare.fromLocation() + " -> " + fare.toLocation() +
                " | " + fare.getClassName() + " | " + this.getPrice() + " руб. | " + purchaseDate;
    }
}