package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {

    @NotNull(message = "Логин не заданн")
    @NotBlank(message = "Логин состоит из пустой строки")
    private final String login;
    private String name;
    private long id;
    @Email(message = "Не верный формат email")
    @NotNull(message = "Поле email не заданно")
    @NotBlank(message = "email состоит из пустой строки")
    private final String email;


    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;

    private final Set<Long> friends = new HashSet<>();

    public void addFriend(User user) {
        this.friends.add(user.getId());
    }

    public void deleteFriend(User user) {
        friends.remove(user.getId());
    }

    public Set<Long> getFriends() {
        return friends;
    }
}
