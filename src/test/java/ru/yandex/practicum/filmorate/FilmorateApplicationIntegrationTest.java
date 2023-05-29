package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmorateApplicationIntegrationTest {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private void createUser() throws ValidationException {
        User user = User.builder()
                .id(1)
                .login("TestLoginUser")
                .name("Test name user")
                .email("TestEmailUser@mail.ru")
                .birthday(LocalDate.ofEpochDay(2000 - 10 - 11))
                .build();
        userStorage.addUser(user);
    }

    private void createSecondUser() throws ValidationException {
        User user = User.builder()
                .id(2)
                .login("TestLoginUser2")
                .name("Test name user2")
                .email("TestEmailUser2@mail.ru")
                .birthday(LocalDate.ofEpochDay(2002 - 12 - 12))
                .build();
        userStorage.addUser(user);
    }

    private void createThirdUser() throws ValidationException {
        User user = User.builder()
                .id(3)
                .login("TestLoginUser3")
                .name("Test name user3")
                .email("TestEmailUser3@mail.ru")
                .birthday(LocalDate.ofEpochDay(2003 - 12 - 13))
                .build();
        userStorage.addUser(user);
    }

    @Test
    public void testFindUserById() throws ValidationException {
        createUser();

        User user = userStorage.getUserById(1);

        assertThat(user != null);
        assertThat(user.getId() == 1L);
        assertThat(user.getLogin().equals("TestLoginUser"));
        assertThat(user.getName().equals("Test name user"));
        assertThat(user.getEmail().equals("TestEmailUser@mail.ru"));
        assertThat(user.getBirthday().equals(LocalDate.ofEpochDay(2000 - 10 - 11)));
    }

    @Test
    public void testUpdateUser() throws ValidationException {
        createUser();

        User user = User.builder()
                .id(1)
                .login("TestUpdateLoginUser")
                .name("Test update name user")
                .email("TestUpdateEmailUser@mail.ru")
                .birthday(LocalDate.ofEpochDay(2001 - 11 - 11))
                .build();

        userStorage.updateUser(user);
        User updatedUser = userStorage.getUserById(1);

        assertThat(updatedUser != null);
        assertThat(updatedUser.getId() == 1L);
        assertThat(updatedUser.getLogin().equals("TestUpdateLoginUser"));
        assertThat(updatedUser.getName().equals("Test update name user"));
        assertThat(updatedUser.getEmail().equals("TestUpdateEmailUser@mail.ru"));
        assertThat(updatedUser.getBirthday().equals(LocalDate.ofEpochDay(2001 - 11 - 11)));
    }

    @Test
    public void testGetAllUsers() throws ValidationException {
        createUser();
        createSecondUser();

        List<User> userList = userStorage.getAllUsers();

        assertThat(!userList.isEmpty());
        assertThat(userList.size() == 2);
        assertThat(userList.get(0).getId() == 1L);
        assertThat(userList.get(0).getLogin().equals("TestLoginUser"));
        assertThat(userList.get(0).getName().equals("Test name user"));
        assertThat(userList.get(0).getEmail().equals("TestEmailUser@mail.ru"));
        assertThat(userList.get(0).getBirthday().equals(LocalDate.ofEpochDay(2000 - 10 - 11)));
        assertThat(userList.get(1).getId() == 2L);
        assertThat(userList.get(1).getLogin().equals("TestLoginUser2"));
        assertThat(userList.get(1).getName().equals("Test name user2"));
        assertThat(userList.get(1).getEmail().equals("TestEmailUser2@mail.ru"));
        assertThat(userList.get(1).getBirthday().equals(LocalDate.ofEpochDay(2002 - 12 - 12)));
    }

    @Test
    public void testAddAndGetFriend() throws ValidationException {
        createUser();
        createSecondUser();
        createThirdUser();

        userStorage.addFriend(userStorage.getUserById(1).getId(), userStorage.getUserById(2).getId());

        User user = userStorage.getUserById(1);
        User friend = userStorage.getUserById(2);

        assertThat(user.getFriends().contains(2));
        assertThat(friend.getFriends().isEmpty());

        userStorage.addFriend(userStorage.getUserById(2).getId(), userStorage.getUserById(1).getId());
        userStorage.addFriend(userStorage.getUserById(2).getId(), userStorage.getUserById(3).getId());

        assertThat(friend.getFriends().contains(1));
        assertThat(friend.getFriends().contains(3));
    }

    @Test
    public void testGetManualFriends() throws ValidationException {
        createUser();
        createSecondUser();
        createThirdUser();

        User user = userStorage.getUserById(1);
        User friend = userStorage.getUserById(2);

        userStorage.addFriend(user.getId(), userStorage.getUserById(3).getId());
        userStorage.addFriend(friend.getId(), userStorage.getUserById(3).getId());

        assertThat(userStorage.getListOfMutualFriends(user.getId(), friend.getId()).contains(3));
    }

    @Test
    public void testDeleteFriend() throws ValidationException {
        createUser();
        createSecondUser();

        User user = userStorage.getUserById(1);

        userStorage.addFriend(user.getId(), userStorage.getUserById(2).getId());
        userStorage.deleteFriend(user.getId(), userStorage.getUserById(2).getId());

        assertThat(userStorage.getFriends(user.getId()).isEmpty());
    }

    public void createFilm() throws ValidationException {
        Film film = Film.builder()
                .id(1)
                .name("Test name film")
                .description("Test description of film")
                .releaseDate(LocalDate.ofEpochDay(2000 - 10 - 11))
                .duration(Duration.ofMinutes(100))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();
        film.addGenre(Genre.builder().id(1).name("Комедия").build());

        filmStorage.addFilm(film);
    }

    public void createSecondFilm() throws ValidationException {
        Film film = Film.builder()
                .id(2)
                .name("Test name film2")
                .description("Test description of film2")
                .releaseDate(LocalDate.ofEpochDay(2002 - 12 - 12))
                .duration(Duration.ofMinutes(120))
                .mpa(RatingMPA.builder().id(2).name("PG").build())
                .build();
        film.addGenre(Genre.builder().id(2).name("Драмма").build());

        filmStorage.addFilm(film);
    }

    public void createThirdFilm() throws ValidationException {
        Film film = Film.builder()
                .id(3)
                .name("Test name film3")
                .description("Test description of film3")
                .releaseDate(LocalDate.ofEpochDay(2003 - 12 - 12))
                .duration(Duration.ofMinutes(130))
                .mpa(RatingMPA.builder().id(4).name("R").build())
                .build();
        film.addGenre(Genre.builder().id(4).name("Триллер").build());

        filmStorage.addFilm(film);
    }

    @Test
    public void testFindFilmById() throws ValidationException {
        createFilm();

        Film film = filmStorage.getFilmById(1L);

        assertThat(film != null);
        assertThat(film.getId() == 1L);
        assertThat(film.getName().equals("Test name film"));
        assertThat(film.getDescription().equals("Test description of film"));
        assertThat(film.getReleaseDate().equals(LocalDate.ofEpochDay(2000 - 10 - 11)));
        assertThat(film.getDuration().equals(Duration.ofMinutes(100)));
        assertThat(film.getMpa().equals(RatingMPA.builder().id(1).name("G").build()));
        assertThat(film.getGenres().equals(Genre.builder().id(1).name("Комедия").build()));
    }

    @Test
    public void testUpdateFilm() throws ValidationException {
        createFilm();
        Film film = Film.builder()
                .id(1)
                .name("Test update name film")
                .description("Test update description of film")
                .releaseDate(LocalDate.ofEpochDay(2001 - 11 - 11))
                .duration(Duration.ofMinutes(110))
                .mpa(RatingMPA.builder().id(3).name("PG-13").build())
                .build();
        film.addGenre(Genre.builder().id(1).name("Комедия").build());
        film.addGenre(Genre.builder().id(6).name("Боевик").build());

        filmStorage.updateFilm(film);

        Film updatedFilm = filmStorage.getFilmById(1);

        assertThat(updatedFilm != null);
        assertThat(updatedFilm.getId() == 1L);
        assertThat(updatedFilm.getName().equals("Test update name film"));
        assertThat(updatedFilm.getDescription().equals("Test update description of film"));
        assertThat(updatedFilm.getReleaseDate().equals(LocalDate.ofEpochDay(2001 - 11 - 11)));
        assertThat(updatedFilm.getDuration().equals(Duration.ofMinutes(110)));
        assertThat(updatedFilm.getMpa().equals(RatingMPA.builder().id(3).name("PG-13").build()));
        assertThat(updatedFilm.getGenres().contains(Genre.builder().id(1).name("Комедия").build()));
        assertThat(updatedFilm.getGenres().contains(Genre.builder().id(6).name("Боевик").build()));
    }

    @Test
    public void testGetAllFilms() throws ValidationException {
        createFilm();
        createSecondFilm();

        Film film = filmStorage.getFilmById(1);
        Film secondFilm = filmStorage.getFilmById(2);

        List<Film> filmList = filmStorage.getAllFilms();

        assertThat(!filmList.isEmpty());
        assertThat(filmList.size() == 2);
        assertThat(filmList.get(0).equals(film));
        assertThat(filmList.get(1).equals(secondFilm));
    }

    @Test
    public void testAddLike() throws ValidationException {
        createUser();
        createFilm();

        filmStorage.addLike(1, 1);
        Film film = filmStorage.getFilmById(1);

        assertThat(!film.getLikes().isEmpty());
        assertThat(film.getLikes().size() == 1);
        assertThat(film.getLikes().contains(1));
    }

    @Test
    public void testDeleteLike() throws ValidationException {
        createUser();
        createFilm();

        filmStorage.addLike(1, 1);
        filmStorage.deleteLike(1, 1);
        Film film = filmStorage.getFilmById(1);

        assertThat(film.getLikes().isEmpty());
    }

    @Test
    public void testGetRatingOfFilms() throws ValidationException {
        createUser();
        createSecondUser();
        createThirdUser();

        createFilm();
        createSecondFilm();
        createThirdFilm();

        filmStorage.addLike(1, 1);
        filmStorage.addLike(2, 1);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 3);

        List<Film> ratingOfFilms = filmStorage.getRatingOfFilms(5L);

        assertThat(!ratingOfFilms.isEmpty());
        assertThat(ratingOfFilms.size() == 3);
        assertThat(ratingOfFilms.get(0).getId() == 2);
        assertThat(ratingOfFilms.get(1).getId() == 1);
        assertThat(ratingOfFilms.get(2).getId() == 3);

        List<Film> limitRatingOfFilms = filmStorage.getRatingOfFilms(1L);

        assertThat(!limitRatingOfFilms.isEmpty());
        assertThat(limitRatingOfFilms.size() == 1);
        assertThat(limitRatingOfFilms.get(0).getId() == 2);
    }
}
