package ru.yandex.practicum.filmorate.model.exception;

public class ValidationException extends Throwable {

    public ValidationException(String message) {
        super(message);
    }
}
