import core.Airport;
import entity.Fare;
import entity.FareClass;
import entity.Passenger;
import entity.Ticket;
import exceptions.NoFaresAvailableException;
import strategy.*;
import util.InputUtils;

import java.util.List;

public class ConsoleApp {
    private static final Airport airport = Airport.getInstance();

    public static void run() {
        boolean exit = false;

        while (!exit) {
            printMenu();
            int choice = InputUtils.getIntInput("Выберите пункт меню: ", 0, 7);

            switch (choice) {
                case 1:
                    handleAddFare();
                    break;
                case 2:
                    showFares();
                    break;
                case 3:
                    handleBuyTicket();
                    break;
                case 4:
                    showAllTickets();
                    break;
                case 5:
                    handlePassengerTotal();
                    break;
                case 6:
                    handleTotalRevenue();
                    break;
                case 7:
                    handleMaxPriceFare();
                    break;
                case 0:
                    exit = true;
                    System.out.println("Программа завершена");
                    break;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Система управления аэропортом ---");
        System.out.println("1. Добавить тариф");
        System.out.println("2. Показать все тарифы");
        System.out.println("3. Зарегистрировать покупку билета");
        System.out.println("4. Показать все билеты");
        System.out.println("5. Общее количество пассажиров");
        System.out.println("6. Стоимость всех проданных билетов");
        System.out.println("7. Максимальная стоимость тарифа");
        System.out.println("0. Выход");
        System.out.println("-----------------------------------");
    }

    private static void handleAddFare() {
        try {
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

            airport.addFare(newFare);

            System.out.println("Система: Тариф " + newFare.fromLocation() + " -> " +
                    newFare.toLocation() + " добавлен в БД.");

        } catch (Exception e) {
            System.err.println("Ошибка сохранения тарифа: " + e.getMessage());
        }
    }

    private static void showFares() {
        List<Fare> fares = airport.getFares();
        if (fares.isEmpty()) {
            System.out.println("Нет доступных тарифов...");
            return;
        }
        for (int i = 0; i < fares.size(); i++) {
            System.out.println((i+1) + ". " + fares.get(i));
        }
    }

    private static void handleBuyTicket() {
        if (airport.getFares().isEmpty()) {
            System.out.println("Нет доступных тарифов. Сначала добавьте тариф.");
            return;
        }

        ConsoleApp.showFares();
        int fareChoice = InputUtils.getIntInput("Выберите номер тарифа: ", 1, airport.getFares().size());
        Fare selectedFare = airport.getFares().get(fareChoice - 1);

        String name = InputUtils.getStringInput("Введите имя пассажира: ");
        String passportId = InputUtils.getStringInput("Введите серию и номер паспорта: ");
        String birthDate = InputUtils.getStringInput("Введите дату рождения в формате дд.мм.гггг: ");

        System.out.println("\nЕсть ли у пассажира льгота?");
        System.out.println("0 - Нет");
        System.out.println("1 - Студент (-10%)");
        System.out.println("2 - Пенсионер (-15%)");
        int discountChoice = InputUtils.getIntInput("Ваш выбор: ", 0, 2);

        DiscountStrategy passengerDiscount = switch (discountChoice) {
            case 1 -> new StudentDiscount();
            case 2 -> new SeniorDiscount();
            default -> new NoDiscount();
        };

        double finalPrice = airport.calculateFinalPrice(selectedFare, passengerDiscount);

        Passenger passenger = airport.findOrCreatePassenger(name, passportId, birthDate);

        try {
            String ticketNumber = "TKT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String purchaseDate = java.time.LocalDate.now().toString();

            airport.buyTicket(passenger, selectedFare, finalPrice);

            System.out.println("\n✅ Билет успешно куплен!");
            System.out.println("Номер билета: " + ticketNumber);
            System.out.println("Пассажир: " + passenger.getName());
            System.out.println("Маршрут: " + selectedFare.fromLocation() + " -> " + selectedFare.toLocation());
            System.out.println("Класс: " + selectedFare.getClassName());
            System.out.println("Дата покупки: " + purchaseDate);
            System.out.println("Базовая цена: " + selectedFare.getPrice() + " руб.");
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

    private static void showAllTickets() {
        System.out.println("\n--- Купленные билеты ---");
        List<Ticket> tickets = airport.getTickets();

        if (tickets.isEmpty()) {
            System.out.println("Билетов нет");
            return;
        }
        for (Ticket ticket : tickets) {
            System.out.println(ticket);
        }
    }

    private static void handlePassengerTotal() {
        int passengerTotal = airport.calculatePassengerTotal();
        System.out.println("Общее количество пассажиров: " + passengerTotal);
    }

    private static void handleTotalRevenue() {
        double totalRevenue = airport.calculateTotalRevenue();
        System.out.printf("Общая выручка от всех билетов: %.2f руб.\n", totalRevenue);
    }

    private static void handleMaxPriceFare() {
        try {
            Fare maxPriceFare = airport.findMaxPriceFare();
            System.out.println("Тариф с максимальной ценой:");
            System.out.println("   " + maxPriceFare);
        } catch (NoFaresAvailableException e) {
            System.out.println("Нет доступных тарифов.");
        }
    }
}