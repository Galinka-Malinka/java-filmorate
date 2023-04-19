package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {

    @NotNull(message = "Логин не заданн")
    @NotBlank(message = "Логин состоит из пустой строки")
    private final String login;
    private String name;
    private int id;
    @Email(message = "Не верный формат email")
    @NotNull(message = "Поле email не заданно")
    @NotBlank(message = "email состоит из пустой строки")
    private final String email;


    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;
}
