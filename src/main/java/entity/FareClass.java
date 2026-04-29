package entity;

public enum FareClass {
    ECONOMY("Эконом",0),
    BUSINESS("Бизнес",1),
    FIRST("Первый",2);

    private final String name;
    private final int code;

    FareClass(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public int getCode() { return code; }

    public String getName() {
        return name;
    }

    public static FareClass fromIndex(int index) {
        FareClass[] values = values(); // массив констант enum в порядке объявления
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        throw new IllegalArgumentException("Неверный номер класса: " + index);
    }
}