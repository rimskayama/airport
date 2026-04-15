package entity;

public class Ticket {
    private String ticketNumber;
    private Passenger passenger;
    private Fare fare;
    private String purchaseDate;

    public Ticket(String ticketNumber, Passenger passenger, Fare fare, String purchaseDate) {
        this.ticketNumber = ticketNumber;
        this.passenger = passenger;
        this.fare = fare;
        this.purchaseDate = purchaseDate;
    }

    public double price() {
        return fare.getPrice();
    }

    public String getTicketNumber() { return ticketNumber; }
    public Passenger getPassenger() { return passenger; }
    public Fare getFare() { return fare; }
    public String getPurchaseDate() { return purchaseDate; }

    @Override
    public String toString() {
        return "Билет " + ticketNumber + " | " + passenger.getName() +
                " | " + fare.fromLocation() + " -> " + fare.toLocation() +
                " | " + fare.getClassName() + " | " + fare.getPrice() + " руб. | " + purchaseDate;
    }
}