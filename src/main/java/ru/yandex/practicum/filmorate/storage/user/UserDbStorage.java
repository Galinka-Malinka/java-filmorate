package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override  //добавление пользователя
    public ResponseEntity<?> addUser(User user) throws ValidationException {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "insert into \"user\" (login, name, email, birthday) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        long userId = keyHolder.getKey().longValue();
        user.setId(userId);

        log.debug("Добавление пользователя: {}", user.getLogin());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @Override  //обновление пользователя
    public ResponseEntity<?> updateUser(User user) throws ValidationException {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, user.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sql = "update \"user\" set " +
                "user_id = ?, login = ?, name = ?, email = ?, birthday = ? " +
                "where user_id = ?";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());

        log.debug("Обновление пользователя: {}", user.getLogin());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override  //получение всех пользователей
    public List<User> getAllUsers() {
        String sql = "select * from \"user\"";
        List<User> userList = jdbcTemplate.query(sql, this::mapRowToUser);

        for (User user : userList) {
            String sqlForFriendsConfirmed = "select friend_id as user_id from friends where user_id = ? "
                    + "and friendship_status_id = 1"
                    + "union "
                    + "select user_id from friends where friend_id = ? and friendship_status_id = 1";
            List<Long> listConfirmedFriendsId = jdbcTemplate.query(sqlForFriendsConfirmed, this::mapRowToIdUser,
                    user.getId(), user.getId());

            if (!listConfirmedFriendsId.isEmpty()) {
                for (Long friendId : listConfirmedFriendsId) {
                    String sqlForFriend = "select * from \"user\" where user_id = ?";
                    User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendId);
                    user.addFriend(friend);
                    user.setStatusFriendship(friendId, "confirmed");
                }
            }

            String sqlForFriendsUnconfirmed = "select friend_id from friends where user_id = ? "
                    + "and friendship_status_id = 2";
            List<Long> listUnconfirmedFriendsId = jdbcTemplate.query(sqlForFriendsUnconfirmed, this::mapRowToIdFriend,
                    user.getId());

            if (!listUnconfirmedFriendsId.isEmpty()) {
                for (Long friendId : listUnconfirmedFriendsId) {
                    String sqlForFriend = "select * from \"user\" where user_id = ?";
                    User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendId);
                    user.addFriend(friend);
                    user.setStatusFriendship(friendId, "unconfirmed");
                }
            }
        }
        return userList;
    }

    private Long mapRowToIdFriend(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("friend_id");
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .email(resultSet.getString("email"))
                .birthday(LocalDate.parse(resultSet.getString("birthday")))
                .build();
    }

    @Override  //получение пользователя по id
    public User getUserById(long id) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sqlForUser = "select * from \"user\" where user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlForUser, this::mapRowToUser, id);

        String sqlForFriendsConfirmed = "select friend_id as user_id from friends where user_id = ? "
                + "and friendship_status_id = 1"
                + "union "
                + "select user_id from friends where friend_id = ? and friendship_status_id = 1";
        List<Long> listConfirmedFriendsId = jdbcTemplate.query(sqlForFriendsConfirmed, this::mapRowToIdUser, id, id);

        if (!listConfirmedFriendsId.isEmpty()) {
            for (Long friendId : listConfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendId);
                user.addFriend(friend);
                user.setStatusFriendship(friendId, "confirmed");
            }
        }

        String sqlForFriendsUnconfirmed = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 2";
        List<Long> listUnconfirmedFriendsId = jdbcTemplate.query(sqlForFriendsUnconfirmed, this::mapRowToIdFriend, id);

        if (!listUnconfirmedFriendsId.isEmpty()) {
            for (Long friendId : listUnconfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendId);
                user.addFriend(friend);
                user.setStatusFriendship(friendId, "unconfirmed");
            }
        }
        return user;
    }

    private Long mapRowToIdUser(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("user_id");
    }

    @Override  //удаление всех пользователей
    public void clearUserMap() {
        String sql = "delete from \"user\"";
        jdbcTemplate.update(sql);

        String sqlForFriends = "delete from friends";
        jdbcTemplate.update(sqlForFriends);

        String sqlForLikes = "delete from likes";
        jdbcTemplate.update(sqlForLikes);
    }

    @Override //добавление в друзья
    public User addFriend(Long userId, Long friendId) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sqlForUser = "select * from \"user\" where user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlForUser, this::mapRowToUser, userId);

        String sqlForFriendsConfirmed = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 1";
        List<Long> listConfirmedFriendsId = jdbcTemplate.query(sqlForFriendsConfirmed, this::mapRowToIdFriend, userId);

        if (!listConfirmedFriendsId.isEmpty()) {
            for (Long friendIdConfirmed : listConfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendIdConfirmed);
                user.addFriend(friend);
                user.setStatusFriendship(friendIdConfirmed, "confirmed");
            }
        }

        String sqlForFriendsUnconfirmed = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 2";
        List<Long> listUnconfirmedFriendsId = jdbcTemplate.query(sqlForFriendsUnconfirmed, this::mapRowToIdFriend,
                userId);

        if (!listUnconfirmedFriendsId.isEmpty()) {
            for (Long friendIdUnconfirmed : listUnconfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendIdUnconfirmed);
                user.addFriend(friend);
                user.setStatusFriendship(friendIdUnconfirmed, "unconfirmed");
            }
        }

        String sqlForFriend = "select * from \"user\" where user_id = ?";
        User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendId);

        String sqlForFriendsConfirmedOfFriend = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 1";
        List<Long> listConfirmedFriends = jdbcTemplate.query(sqlForFriendsConfirmedOfFriend, this::mapRowToIdFriend,
                friendId);

        if (!listConfirmedFriends.isEmpty()) {
            for (Long friendIdConfirmed : listConfirmedFriends) {
                String sqlForFriends = "select * from \"user\" where user_id = ?";
                User friendOfFriend = jdbcTemplate.queryForObject(sqlForFriends, this::mapRowToUser, friendIdConfirmed);
                friend.addFriend(friendOfFriend);
                friend.setStatusFriendship(friendIdConfirmed, "confirmed");
            }
        }

        String sqlForFriendsUnconfirmedOfFriend = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 2";
        List<Long> listUnconfirmedFriends = jdbcTemplate.query(sqlForFriendsUnconfirmedOfFriend, this::mapRowToIdFriend,
                friendId);

        if (!listUnconfirmedFriends.isEmpty()) {
            for (Long friendIdUnconfirmed : listUnconfirmedFriends) {
                String sqlForFriendForFriend = "select * from \"user\" where user_id = ?";
                User friendOfFriend = jdbcTemplate.queryForObject(sqlForFriendForFriend, this::mapRowToUser,
                        friendIdUnconfirmed);
                friend.addFriend(friendOfFriend);
                friend.setStatusFriendship(friendIdUnconfirmed, "unconfirmed");
            }
        }

        if (friend.isFriend(userId)) {
            String sql = "update friends set friendship_status_id = ? where user_id = ? and friend_id = ?";
            jdbcTemplate.update(sql, 1, friendId, userId);
        } else {
            String sql = "insert into friends (user_id, friend_id, friendship_status_id) values (?, ?, ?)";
            jdbcTemplate.update(sql, userId, friendId, 2);
        }

        user.addFriend(friend);
        return user;
    }

    @Override  //получение списка друзей
    public List<User> getFriends(Long userId) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sql = "select friend_id as user_id from friends where user_id = ? "
                + "union "
                + "select user_id from friends where friend_id = ? and friendship_status_id = 1";

        List<Long> listFriendsId = jdbcTemplate.query(sql, this::mapRowToIdUser, userId, userId);

        List<User> listFriends = new ArrayList<>();

        if (!listFriendsId.isEmpty()) {
            for (Long id : listFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, id);
                listFriends.add(friend);
            }
        }
        return listFriends;
    }

    @Override  //получение списка общих друзей
    public List<User> getListOfMutualFriends(Long user1Id, Long user2Id) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, user1Id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, user2Id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sqlUser = "select friend_id as user_id from friends where user_id = ? "
                + "union "
                + "select user_id from friends where friend_id = ? and friendship_status_id = 1";

        List<Long> listFriendsUser1 = jdbcTemplate.query(sqlUser, this::mapRowToIdUser, user1Id, user1Id);

        List<Long> listFriendsUser2 = jdbcTemplate.query(sqlUser, this::mapRowToIdUser, user2Id, user2Id);

        List<Long> listManualFriendsOfUsers = new ArrayList<>();

        for (Long id : listFriendsUser1) {
            if (listFriendsUser2.contains(id)) {
                listManualFriendsOfUsers.add(id);
            }
        }

        List<User> listFriends = new ArrayList<>();

        if (!listManualFriendsOfUsers.isEmpty()) {
            for (Long id : listManualFriendsOfUsers) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, id);
                listFriends.add(friend);
            }
        }
        return listFriends;
    }

    @Override   //удаление друга
    public ResponseEntity<?> deleteFriend(Long userId, Long friendId) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        String sql = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        jdbcTemplate.update(sql, friendId, userId);

        String sqlForUser = "select * from \"user\" where user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlForUser, this::mapRowToUser, userId);

        String sqlForFriendsConfirmed = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 1";
        List<Long> listConfirmedFriendsId = jdbcTemplate.query(sqlForFriendsConfirmed, this::mapRowToIdFriend, userId);

        if (!listConfirmedFriendsId.isEmpty()) {
            for (Long friendIdConfirmed : listConfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendIdConfirmed);
                user.addFriend(friend);
                user.setStatusFriendship(friendIdConfirmed, "confirmed");
            }
        }

        String sqlForFriendsUnconfirmed = "select friend_id from friends where user_id = ? "
                + "and friendship_status_id = 2";
        List<Long> listUnconfirmedFriendsId = jdbcTemplate.query(sqlForFriendsUnconfirmed, this::mapRowToIdFriend,
                userId);

        if (!listUnconfirmedFriendsId.isEmpty()) {
            for (Long friendIdUnconfirmed : listUnconfirmedFriendsId) {
                String sqlForFriend = "select * from \"user\" where user_id = ?";
                User friend = jdbcTemplate.queryForObject(sqlForFriend, this::mapRowToUser, friendIdUnconfirmed);
                user.addFriend(friend);
                user.setStatusFriendship(friendIdUnconfirmed, "unconfirmed");
            }
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
