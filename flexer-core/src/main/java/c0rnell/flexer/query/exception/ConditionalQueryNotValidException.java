package c0rnell.flexer.query.exception;

public class ConditionalQueryNotValidException extends Exception {

    public ConditionalQueryNotValidException() {
    }

    public ConditionalQueryNotValidException(String message) {
        super(message);
    }

    public ConditionalQueryNotValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionalQueryNotValidException(Throwable cause) {
        super(cause);
    }

    public ConditionalQueryNotValidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
