package ru.practicum.exception;

public class ValidateException extends RuntimeException {
    public ValidateException(String message) {
        super(message);
    }

    public static ValidateException of(ErrorMessages message, Object... args) {
        return new ValidateException(message.format(args));
    }
}
