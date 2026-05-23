package util;
import java.util.Scanner;

public class InputUtils {
    private static Scanner scanner = new Scanner(System.in);

    public static int getIntInput(String invite, int min, int max) {
            while (true) {
                try {
                    System.out.print(invite);
                    return parseAndValidateInt(scanner.nextLine().trim(), min, max);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: должно быть числом. Попробуйте снова.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: " + e.getMessage() + ". Попробуйте снова.");
                }
            }
        }

    public static double getDoubleInput(String invite, double min, double max) {
        while (true) {
            try {
                System.out.print(invite);
                return parseAndValidateDouble(scanner.nextLine().trim(), min, max);

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: должно быть числом. Попробуйте снова.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    public static String getStringInput(String invite) {
        System.out.print(invite);
        return scanner.nextLine();
    }

    // ввод double с возможностью пропуска
    public static double getDoubleInputWithSkip(String prompt, double min, double max, double defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return defaultValue;
        }

        while (true) {
            try {
                return parseAndValidateDouble(input, min, max);
            } catch (NumberFormatException e) {
                System.out.print("Должно быть числом. Попробуйте снова: ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return defaultValue;
            } catch (IllegalArgumentException e) {
                System.out.print(e.getMessage() + ". Попробуйте снова: ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return defaultValue;
            }
        }
    }

    // ввод int с возможностью пропуска
    public static int getIntInputWithSkip(String invite, int min, int max, int defaultValue) {
        System.out.print(invite + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return defaultValue;
        }

        while (true) {
            try {
                return parseAndValidateInt(input, min, max);
            } catch (NumberFormatException e) {
                System.out.print("Должно быть числом. Попробуйте снова: ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return defaultValue;
            } catch (IllegalArgumentException e) {
                System.out.print(e.getMessage() + ". Попробуйте снова: ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return defaultValue;
            }
        }
    }

    private static int parseAndValidateInt(String raw, int min, int max) {
        int value = Integer.parseInt(raw);
        if (value < min || value > max) {
            throw new IllegalArgumentException("Диапазон от " + min + " до " + max);
        }
        return value;
    }

    private static double parseAndValidateDouble(String raw, double min, double max) {
        double value = Double.parseDouble(raw);
        if (value < min || value > max) {
            throw new IllegalArgumentException("Диапазон от " + min + " до " + max);
        }
        return value;
    }
}
