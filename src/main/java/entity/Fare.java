package entity;

public class Fare {
    private int id;
    private String fromLocation;
    private String toLocation;
    private double price;
    private FareClass classChoice;

    public Fare(String fromLocation, String toLocation, double price, FareClass classChoice) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.price = price;
        this.classChoice = classChoice;
    }

    public int getId() { return id; }
    public String fromLocation() { return fromLocation; }
    public String toLocation() { return toLocation; }
    public double getPrice() { return price; }
    public FareClass getClassChoice() { return classChoice; }

    public void setId(int id) { this.id = id; }

    public String getClassName() {
        return classChoice.getName();
    }
}