import core.Airport;

public class Main {
    public static void main(String[] args) {
        Airport airport = Airport.getInstance();

        // обработчик завершения — аналог деструктора
        Runtime.getRuntime().addShutdownHook(new Thread(() -> airport.shutdown()));

        ConsoleApp.run();
    }
}