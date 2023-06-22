package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
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

    private final Map<Long, String> mapFriends = new HashMap<>();

    public void addFriend(User user) {
        if (user.mapFriends.containsKey(this.getId())) {
            user.mapFriends.replace(this.getId(), "confirmed");
            this.mapFriends.put(user.getId(), "confirmed");
        } else {
            this.mapFriends.put(user.getId(), "unconfirmed");
        }
    }

    public boolean isFriend(Long friendId) {
        return mapFriends.containsKey(friendId);
    }

    public void setStatusFriendship(Long friendId, String status) {
        this.mapFriends.replace(friendId, status);
    }

    public void deleteFriend(User user) {
        mapFriends.remove(user.getId());
    }

    public List<Long> getFriends() {
        List<Long> listFriends = new ArrayList<>();
        for (Long userId : mapFriends.keySet()) {
            listFriends.add(userId);
        }
        return listFriends;
    }
}
