package ru.yandex.practicum.filmorate.model;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Exceptions.ValidationException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserController {
    private Map<Integer, User> userMap = new HashMap<>();
    private int id = 0;

    @PostMapping("/users")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user) {  //добавление пользователя
        try {
            if (user.getLogin().contains(" ")) {
                throw new ValidationException("В логине не должно быть пробелов");
            }
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            user.setId(id + 1);
            id++;
            this.userMap.put(user.getId(), user);
        } catch (ValidationException e) {
            log.warn(e.getMessage());
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
        log.debug("Добавление пользователя: {}", user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/users")
    public ResponseEntity<?> updateFilm(@Valid @RequestBody User user) {  //обновление пользователя
        try {
            if (user.getLogin().contains(" ")) {
                throw new ValidationException("В логине не должно быть пробелов");
            }
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            if (!userMap.containsKey(user.getId())) {
                return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
            }
            this.userMap.replace(user.getId(), user);
        } catch (ValidationException e) {
            log.warn(e.getMessage());
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
        log.debug("Обновление пользователя: {}", user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/users")
    public List<User> getAllFilms() {  //получение всех пользователей
        return new ArrayList<>(userMap.values());
    }

    @DeleteMapping("/users")
    public void clearUserMap() {
        userMap.clear();
        id = 0;
        log.debug("Удаление всех пользователей");
    }
}
