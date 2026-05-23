package core;

import database.DatabaseManager;
import entity.Fare;
import entity.Passenger;
import entity.Ticket;
import exceptions.NoFaresAvailableException;
import strategy.DiscountStrategy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Airport {
    private static Airport instance;
    private List<Fare> fares;
    private List<Passenger> passengers;
    private List<Ticket> tickets;

    private Airport() {
        fares = new ArrayList<>();
        passengers = new ArrayList<>();
        tickets = new ArrayList<>();

        // инициализация БД, загрузка данных
        DatabaseManager.initializeDatabase();
        loadDataFromDatabase();
    }

    public static Airport getInstance() {
        if (instance == null) {
            instance = new Airport();
        }
        return instance;
    }

    private void loadDataFromDatabase() {
        try {
            fares = DatabaseManager.loadAllFares();
            passengers = DatabaseManager.loadAllPassengers();
            tickets = DatabaseManager.loadAllTickets();

            System.out.println("Данные загружены из БД. Тарифов: " + fares.size() +
                    ", пассажиров: " + passengers.size() +
                    ", билетов: " + tickets.size());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    // Тарифы
    // добавить тариф
    public void addFare(Fare newFare) {
        try {
            DatabaseManager.saveFare(newFare);
            fares.add(newFare);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения тарифа: " + e.getMessage());
        }
    }

    public List<Fare> getFares() {
        return new ArrayList<>(fares);
    }

    public Fare findMaxPriceFare() throws NoFaresAvailableException {
        if (fares.isEmpty()) {
            throw new NoFaresAvailableException();
        }

        Fare maxFare = fares.get(0);
        for (Fare fare : fares) {
            if (fare.getPrice() > maxFare.getPrice()) {
                maxFare = fare;
            }
        }
        return maxFare;
    }

    // обновить тариф
    public boolean updateFare(Fare oldFare, Fare newFare) {
        try {
            if (!DatabaseManager.updateFare(oldFare, newFare)) {
                return false;
            }

            // Потом обновляем список
            for (int i = 0; i < fares.size(); i++) {
                if (fares.get(i).getId() == oldFare.getId()) {
                    fares.set(i, newFare);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Ошибка обновления тарифа: " + e.getMessage());
            return false;
        }
    }

    // удалить тариф
    public boolean removeFare(Fare fare) {
        try {
            if (DatabaseManager.deleteFare(fare)) {  // Сначала БД
                // Удаляем из списка по ID, а не по equals()
                for (int i = 0; i < fares.size(); i++) {
                    if (fares.get(i).getId() == fare.getId()) {
                        fares.remove(i);
                        return true;
                    }
                }
                return false; // Не нашли в списке
            }
            return false; // Не удалилось из БД
        } catch (Exception e) {
            System.err.println("Ошибка удаления тарифа: " + e.getMessage());
            return false;
        }
    }

    // Билеты
    public double calculateFinalPrice(Fare fare, DiscountStrategy passengerDiscount) {
        // скидка направления
        double priceWithRoute = fare.getPriceWithRouteDiscount();
        // цена со льготной скидкой
        return passengerDiscount.applyDiscount(priceWithRoute);

    }

    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets);
    }

    public void buyTicket(Passenger passenger, Fare selectedFare, double finalPrice) {
        try {
            // получить ID пассажира
            int passengerId = DatabaseManager.savePassenger(passenger);

            // создать билет
            String ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String purchaseDate = java.time.LocalDate.now().toString();
            Ticket newTicket = new Ticket(ticketNumber, passenger, selectedFare, purchaseDate, finalPrice);

            // сохранить билет
            DatabaseManager.saveTicket(newTicket, passengerId, selectedFare.getId());
            tickets.add(newTicket);

        } catch (Exception e) {
            System.err.println("Ошибка при покупке билета: " + e.getMessage());
        }
    }

    private Passenger findPassengerByPassport(String passportId) {
        for (Passenger p : passengers) {
            if (p.getPassportId().equals(passportId)) {
                return p;
            }
        }
        return null;
    }

    public Passenger findOrCreatePassenger(String name, String passportId, String birthDate) {
        Passenger existing = findPassengerByPassport(passportId);
        if (existing != null) {
            return existing;
        }

        Passenger newPassenger = new Passenger(name, passportId, birthDate);
        passengers.add(newPassenger);
        try {
            DatabaseManager.savePassenger(newPassenger);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пассажира в БД: " + e.getMessage(), e);
        }
        return newPassenger;
    }

    public int calculatePassengerTotal() {
        return passengers.size();
    }

    public double calculateTotalRevenue() {
        try {
            return DatabaseManager.calculateTotalRevenue();
        } catch (SQLException e) {
            System.err.println("Ошибка подсчёта выручки: " + e.getMessage());
            double total = 0;
            for (Ticket ticket : tickets) {
                total += ticket.getPrice();
            }
            return total;
        }
    }

    public void shutdown() {
        DatabaseManager.closeConnection(); // аналог деструктора
        System.out.println("Соединение с БД закрыто");
    }
}