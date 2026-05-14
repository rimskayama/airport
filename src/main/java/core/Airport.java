package core;

import database.DatabaseManager;
import entity.Fare;
import entity.FareClass;
import entity.Passenger;
import entity.Ticket;
import exceptions.NoFaresAvailableException;
import strategy.*;
import util.InputUtils;

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
    public void addFare() {
        String fromLocation = InputUtils.getStringInput("Введите пункт отправления: ");
        String toLocation = InputUtils.getStringInput("Введите пункт назначения: ");
        double price = InputUtils.getIntInput("Введите цену: ", 1, 1000000);
        System.out.println("Выберите класс:");
        for (FareClass fc : FareClass.values()) {
            System.out.println(fc.ordinal() + " - " + fc.getName());
        }
        int classChoice = InputUtils.getIntInput("Ваш выбор: ", 0, 2);
        FareClass selectedClass = FareClass.fromIndex(classChoice);

        System.out.print("Введите скидку направления в % (0 = без скидки, 10 = 10%): ");
        int routeDiscountPercent = InputUtils.getIntInput("Ваш выбор:", 0, 100);

        DiscountStrategy routeDiscount = (routeDiscountPercent > 0)
                ? new RouteDiscount(routeDiscountPercent)
                : new NoDiscount();

        Fare newFare = new Fare(fromLocation, toLocation, price, selectedClass, routeDiscount);

        try {
            DatabaseManager.saveFare(newFare);
            fares.add(newFare);
            System.out.println("Система: Тариф " + newFare.fromLocation() + " -> " +
                    newFare.toLocation() + " добавлен в БД.");
        } catch (Exception e) {
            System.err.println("Ошибка сохранения тарифа: " + e.getMessage());
        }
    }

    public void showFares() {
        System.out.println("\n--- Текущие тарифы ---");
        if (fares.isEmpty()) {
            System.out.println("Нет доступных тарифов. Сначала добавьте тариф.");
            return;
        }
            for (int i = 0; i < fares.size(); i++) {
                System.out.println((i + 1) + ". " + fares.get(i));
            }
        }

    private DiscountStrategy getPassengerDiscountStrategy(int choice) {
        switch (choice) {
            case 1: return new StudentDiscount();
            case 2: return new SeniorDiscount();
            default: return new NoDiscount();
        }
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

    // Билеты
    public void buyTicket() {
        if (fares.isEmpty()) {
            System.out.println("Нет доступных тарифов. Сначала добавьте тариф.");
            return;
        }

        showFares();
        int fareChoice = InputUtils.getIntInput("Выберите номер тарифа: ", 1, fares.size());
        Fare selectedFare = fares.get(fareChoice - 1);

        String name = InputUtils.getStringInput("Введите имя пассажира: ");
        String passportId = InputUtils.getStringInput("Введите серию и номер паспорта: ");
        String birthDate = InputUtils.getStringInput("Введите дату рождения в формате дд.мм.гггг: ");

        System.out.println("\nЕсть ли у пассажира льгота?");
        System.out.println("0 - Нет");
        System.out.println("1 - Студент (-10%)");
        System.out.println("2 - Пенсионер (-15%)");
        int discountChoice = InputUtils.getIntInput("Ваш выбор: ", 0, 2);
        DiscountStrategy passengerDiscount = getPassengerDiscountStrategy(discountChoice);

        // рассчитать цену билета с учётом скидок
        double basePrice = selectedFare.getPrice();
        // скидка направления
        double priceWithRouteDiscount = selectedFare.getPriceWithRouteDiscount();
        // цена со льготной скидкой
        double finalPrice = passengerDiscount.applyDiscount(priceWithRouteDiscount);

        Passenger passenger = findPassengerByPassport(passportId);
        if (passenger == null) {
            passenger = new Passenger(name, passportId, birthDate);
            passengers.add(passenger);
            System.out.println("Новый пассажир добавлен в систему.");
        } else {
            System.out.println("Пассажир найден в системе.");
        }

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

            System.out.println("\n✅ Билет успешно куплен!");
            System.out.println("Номер билета: " + ticketNumber);
            System.out.println("Пассажир: " + passenger.getName());
            System.out.println("Маршрут: " + selectedFare.fromLocation() + " -> " + selectedFare.toLocation());
            System.out.println("Класс: " + selectedFare.getClassName());
            System.out.println("Дата покупки: " + purchaseDate);
            System.out.println("Базовая цена: " + basePrice + " руб.");
            if (selectedFare.getRouteDiscountPercent() > 0) {
                System.out.println("Скидка направления: -" + selectedFare.getRouteDiscountPercent() + "%");
            }
            if (!(passengerDiscount instanceof NoDiscount)) {
                System.out.println("Льгота: " + passengerDiscount.getName());
            }
            System.out.println("Итого: " + String.format("%.2f", finalPrice) + " руб.");

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

    public void showAllTickets() {
        System.out.println("\n--- Купленные билеты ---");
        if (tickets.isEmpty()) {
            System.out.println("Билетов нет");
            return;
        }
        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
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