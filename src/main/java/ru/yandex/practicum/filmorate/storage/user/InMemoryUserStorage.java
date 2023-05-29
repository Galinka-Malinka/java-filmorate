package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Primary
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> userMap = new HashMap<>();
    private int id = 0;

    @Override
    public ResponseEntity<?> addUser(User user) throws ValidationException {  //добавление пользователя
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("В логине не должно быть пробелов");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(id + 1);
        id++;
        this.userMap.put(user.getId(), user);
        log.debug("Добавление пользователя: {}", user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateUser(User user) throws ValidationException {  //обновление пользователя
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("В логине не должно быть пробелов");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (!userMap.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователя с id = " + user.getId() + " не существует");
        }
        this.userMap.replace(user.getId(), user);
        log.debug("Обновление пользователя: {}", user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public List<User> getAllUsers() {  //получение всех пользователей
        return new ArrayList<>(userMap.values());
    }

    @Override
    public void clearUserMap() {  //удаление всех пользователей
        userMap.clear();
        id = 0;
        log.debug("Удаление всех пользователей");
    }

    @Override
    public User getUserById(long id) {  //получение пользователя по id
        if (!userMap.containsKey(id)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        return userMap.get(id);
    }

    @Override
    public User addFriend(Long userId, Long friendId) { //добавление в друзья
        if (!userMap.containsKey(userId)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        if (!userMap.containsKey(friendId)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        userMap.get(userId).addFriend(userMap.get(friendId));
        return userMap.get(userId);
    }

    @Override
    public List<User> getFriends(Long userId) {  //получение списка друзей
        if (!userMap.containsKey(userId)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        List<User> listOfFriends = new ArrayList<>();
        for (Long id : userMap.get(userId).getFriends()) {
            listOfFriends.add(userMap.get(id));
        }
        return listOfFriends;
    }

    @Override
    public List<User> getListOfMutualFriends(Long user1Id, Long user2Id) {  //получение списка общих друзей
        if (!userMap.containsKey(user1Id)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        if (!userMap.containsKey(user2Id)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        List<User> listOfMutualFriends = new ArrayList<>();
        for (Long id : userMap.get(user1Id).getFriends()) {
            if (userMap.get(user2Id).getFriends().contains(id)) {
                listOfMutualFriends.add(userMap.get(id));
            }
        }
        return listOfMutualFriends;
    }

    @Override
    public ResponseEntity<?> deleteFriend(Long userId, Long friendId) {  //удаление друга
        if (!userMap.containsKey(userId)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        if (!userMap.containsKey(friendId)) {
            throw new UserNotFoundException("Пользователя с id = " + id + " не существует");
        }
        userMap.get(userId).deleteFriend(userMap.get(friendId));
        userMap.get(friendId).deleteFriend(userMap.get(userId));
        return new ResponseEntity<>(userMap.get(userId), HttpStatus.OK);
    }
}
