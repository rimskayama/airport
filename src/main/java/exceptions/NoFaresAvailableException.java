package exceptions;

public class NoFaresAvailableException extends AirportException {

    public NoFaresAvailableException() {
        super("Список тарифов пуст. Сначала добавьте хотя бы один тариф.");
    }
}