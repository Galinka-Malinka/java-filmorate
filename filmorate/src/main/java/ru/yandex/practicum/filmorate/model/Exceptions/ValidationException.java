package ru.yandex.practicum.filmorate.model.Exceptions;

public class ValidationException extends Throwable {

    public ValidationException(String message) {
        super(message);
    }
}
