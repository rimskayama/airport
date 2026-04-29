import core.Airport;
import util.InputUtils;

public class Main {
    public static void main(String[] args) {
        Airport airport = Airport.getInstance();

        // обработчик завершения - аналог деструктора
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            airport.shutdown();
        }));

        boolean exit = false;

        while (!exit) {
            try {
                printMenu();
                int choice = InputUtils.getIntInput("Выберите пункт меню: ", 0, 6);

                switch (choice) {
                    case 1:
                        airport.addFare();
                        break;
                    case 2:
                        airport.showFares();
                        break;
                    case 3:
                        airport.buyTicket();
                        break;
                    case 4:
                        airport.showAllTickets();
                        break;
                    case 5:
                        int passengerTotal = airport.calculatePassengerTotal();
                        System.out.println("Общее количество пассажиров: " + passengerTotal);
                        break;
                    case 6:
                        double totalRevenue = airport.calculateTotalRevenue();
                        System.out.printf("Общая выручка от всех билетов: %.2f руб.\n", totalRevenue);
                        break;
                    case 0:
                        exit = true;
                        System.out.println("Программа завершена");
                        break;
                    default:
                        System.out.println("Неверный выбор");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
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
        System.out.println("0. Выход");
        System.out.println("-----------------------------------");
    }
}