package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {
    @Autowired
    @Qualifier("userDbStorage")
    private final UserStorage storage;

    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public User addFriend(long user1Id, long user2Id) {  //добавление в друзья
        return storage.addFriend(user1Id, user2Id);
    }

    public ResponseEntity<User> deleteFriend(Long user1Id, Long user2Id) {   //удаление друга
        return storage.deleteFriend(user1Id, user2Id);
    }

    public List<User> getFriends(Long userId) {  //получение списка друзей
        return storage.getFriends(userId);
    }

    public List<User> getListOfMutualFriends(Long user1Id, Long user2Id) {  //получение списка общих друзей
        return storage.getListOfMutualFriends(user1Id, user2Id);
    }

    public ResponseEntity<User> addUser(User user) throws ValidationException {  //добавление пользователя
        return storage.addUser(user);
    }

    public ResponseEntity<User> updateUser(User user) throws ValidationException {  //обновление пользователя
        return storage.updateUser(user);
    }

    public List<User> getAllFilms() {  //получение всех пользователей
        return storage.getAllUsers();
    }

    public User getUserById(long id) {  //получение пользователя по id
        return storage.getUserById(id);
    }

    public void clearUserMap() { //удаление всех пользователей
        storage.clearUserMap();
    }
}
