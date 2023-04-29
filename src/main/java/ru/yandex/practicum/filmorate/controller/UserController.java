package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping  //добавление пользователя
    public ResponseEntity<?> addUser(@Valid @RequestBody User user) throws ValidationException {
        return userService.addUser(user);
    }

    @PutMapping  //обновление пользователя
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) throws ValidationException {
        return userService.updateUser(user);
    }

    @GetMapping  //получение всех пользователей
    public List<User> getAllFilms() {
        return userService.getAllFilms();
    }

    @DeleteMapping  //удаление всех пользователей
    public void clearUserMap() {
        userService.clearUserMap();
    }

    @GetMapping("/{id}")  //получение пользователя по id
    public User getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")  //добавление в друзья
    public User addFriend(@PathVariable long id, @PathVariable long friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")  //удаление друга
    public ResponseEntity<?> deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")  //получение списка друзей
    public List<User> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")  //получение списка общих друзей
    public List<User> getListOfMutualFriends(@PathVariable("id") long user1Id, @PathVariable("otherId") long user2Id) {
        return userService.getListOfMutualFriends(user1Id, user2Id);
    }
}
