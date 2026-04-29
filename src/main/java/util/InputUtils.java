package util;
import java.util.Scanner;

public class InputUtils {
    private static Scanner scanner = new Scanner(System.in);

    public static int getIntInput(String invite, int min, int max) {
        while (true) {
            try {
                System.out.println(invite);
                int input = Integer.parseInt(scanner.nextLine());

                if (input < min || input > max) {
                    System.out.println("Ошибка: Диапазон выбора от " + min + " до " + max + ". Попробуйте снова.");
                    continue;
                }

                return input;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Должно быть числом. Попробуйте снова.");
            }
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
