package ru.practicum.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(Class<?> entity, long id) {
        return new NotFoundException(ErrorMessages.NOT_FOUND.format(entity.getSimpleName(), id));
    }
}
