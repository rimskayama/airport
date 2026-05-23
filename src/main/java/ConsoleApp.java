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
            int choice = InputUtils.getIntInput("Выберите пункт меню: ", 0, 9);

            switch (choice) {
                case 1:
                    handleAddFare();
                    break;
                case 2:
                    showFares();
                    break;
                case 3:
                    handleEditFare();
                    break;
                case 4:
                    handleDeleteFare();
                    break;
                case 5:
                    handleBuyTicket();
                    break;
                case 6:
                    showAllTickets();
                    break;
                case 7:
                    handlePassengerTotal();
                    break;
                case 8:
                    handleTotalRevenue();
                    break;
                case 9:
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
        System.out.println("\n=== Система управления аэропортом ===");

        System.out.println("\n📋 Управление тарифами:");
        System.out.println("1. Добавить тариф");
        System.out.println("2. Показать все тарифы");
        System.out.println("3. Редактировать тариф");
        System.out.println("4. Удалить тариф");

        System.out.println("\n🎫 Операции с билетами:");
        System.out.println("5. Зарегистрировать покупку билета");
        System.out.println("6. Показать все билеты");

        System.out.println("\n📊 Статистика:");
        System.out.println("7. Общее количество пассажиров");
        System.out.println("8. Стоимость всех проданных билетов");
        System.out.println("9. Максимальная стоимость тарифа");

        System.out.println("\n0. Выход");
        System.out.println("-----------------------------------");
    }

    private static void handleAddFare() {
        try {
            String fromLocation = InputUtils.getStringInput("Введите пункт отправления: ");
            String toLocation = InputUtils.getStringInput("Введите пункт назначения: ");
            double price = InputUtils.getDoubleInput("Введите цену: ", 1, 1000000);

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

    private static void handleEditFare() {
        try {
            showFares();
            if (airport.getFares().isEmpty()) return;

            int index = InputUtils.getIntInput("Введите номер тарифа для редактирования: ", 1, airport.getFares().size()) - 1;
            Fare oldFare = airport.getFares().get(index);

            System.out.println("\nРедактирование: " + oldFare.fromLocation() + " → " + oldFare.toLocation());
            System.out.println("Подсказка: нажмите Enter чтобы оставить значение без изменений\n");

            String fromLocation = InputUtils.getStringInput("Откуда [" + oldFare.fromLocation() + "]: ");
            if (fromLocation.isEmpty()) fromLocation = oldFare.fromLocation();

            String toLocation = InputUtils.getStringInput("Куда [" + oldFare.toLocation() + "]: ");
            if (toLocation.isEmpty()) toLocation = oldFare.toLocation();

            double price = InputUtils.getDoubleInputWithSkip(
                    "Цена", 1.0, 1000000.0, oldFare.getPrice());

            System.out.println("Класс (текущий: " + oldFare.getClassChoice().getName() + "):");
            for (FareClass fc : FareClass.values()) {
                System.out.println(fc.ordinal() + " - " + fc.getName());
            }

            int classChoice = InputUtils.getIntInputWithSkip(
                    "Ваш выбор",
                    0,
                    FareClass.values().length - 1,
                    oldFare.getClassChoice().ordinal()
            );

            FareClass selectedClass = FareClass.fromIndex(classChoice);

            int routeDiscountPercent = InputUtils.getIntInputWithSkip("Скидка %", 0, 100, oldFare.getRouteDiscountPercent());

            // создание стратегии и нового объекта
            DiscountStrategy routeDiscount = (routeDiscountPercent > 0)
                    ? new RouteDiscount(routeDiscountPercent)
                    : new NoDiscount();

            Fare newFare = new Fare(fromLocation, toLocation, price, selectedClass, routeDiscount);

            // обновление в системе
            if (airport.updateFare(oldFare, newFare)) {
                System.out.println("Тариф обновлён: " + newFare.fromLocation() + " → " + newFare.toLocation());
            } else {
                System.out.println("Не удалось обновить тариф");
            }

        } catch (Exception e) {
            System.err.println("Ошибка редактирования: " + e.getMessage());
        }
    }

    // удаление тарифа
    private static void handleDeleteFare() {
        try {
            showFares();
            if (airport.getFares().isEmpty()) return;

            int index = InputUtils.getIntInput("Введите номер тарифа для удаления: ", 1, airport.getFares().size()) - 1;
            Fare toDelete = airport.getFares().get(index);

            System.out.println("\nВы собираетесь удалить:");
            System.out.println("   " + toDelete);

            String confirm = InputUtils.getStringInput("Подтвердите удаление (да/нет): ").toLowerCase();
            if (!confirm.equals("да") && !confirm.equals("y") && !confirm.equals("yes")) {
                System.out.println("Удаление отменено");
                return;
            }

            // удаление
            if (airport.removeFare(toDelete)) {
                System.out.println("Тариф удалён");
            } else {
                System.out.println("Не удалось удалить тариф");
            }

        } catch (Exception e) {
            System.err.println("Ошибка удаления: " + e.getMessage());
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