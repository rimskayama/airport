package entity;

public class Passenger {
    private int id;
    private String name;
    private String passportId;
    private String birthDate;

    public Passenger(String name, String passportId, String birthDate) {
        this.name = name;
        this.passportId = passportId;
        this.birthDate = birthDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPassportId() { return passportId; }
    public String getBirthDate() { return birthDate; }

    public void setId(int id) { this.id = id; }
}