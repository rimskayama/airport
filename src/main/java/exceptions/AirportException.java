package exceptions;

// наследование от Exception
public class AirportException extends Exception {

    public AirportException(String message) {
        super(message);
    }

}
