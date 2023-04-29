package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    public ResponseEntity<?> addUser(User user) throws ValidationException;  //добавление пользователя

    public ResponseEntity<?> updateUser(User user) throws ValidationException;  //обновление пользователя

    public List<User> getAllUsers();  //получение всех пользователей

    public User getUserById(long id);  //получение пользователя по id

    public void clearUserMap();  //удаление всех пользователей

    public User addFriend(long user1Id, long user2Id); //добавление в друзья

    public List<User> getFriends(Long userId);  //получение списка друзей

    public List<User> getListOfMutualFriends(Long user1Id, Long user2Id);  //получение списка общих друзей

    public ResponseEntity<?> deleteFriend(Long user1Id, Long user2Id);   //удаление друга
}
