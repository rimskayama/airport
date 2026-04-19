package util;
import java.util.Scanner;

public class InputUtils {
    private static Scanner scanner = new Scanner(System.in);

    public static int getIntInput(String invite, int min, int max) {
        while (true) {
            int input = 0;
            try {
                System.out.println(invite);
                input = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Должно быть числом. Попробуйте снова.");
            }
            if (input < min || input > max) {
                System.out.println("Ошибка: Диапазон выбора от 0 до 6. Попробуйте снова.");
            }
            else return input;
        }
    }

    public static double getDoubleInput(String invite) {
        while (true) {
            try {
                System.out.println(invite);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Должно быть числом. Попробуйте снова.");
            }
        }
    }

    public static String getStringInput(String invite) {
        System.out.println(invite);
        return scanner.nextLine();
    }
}
