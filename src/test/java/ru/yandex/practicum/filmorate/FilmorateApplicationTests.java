package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void clear() throws Exception {
        mockMvc.perform(delete("/films"));
        mockMvc.perform(delete("/users"));
    }

    @Test
    void shouldCreateFilm() throws Exception {

        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();
        System.out.println(film);

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Film"))
                .andExpect(jsonPath("$.description").value("Film description"))
                .andExpect(jsonPath("$.releaseDate").value("2023-04-16"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void shouldGetFilm() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        film.setId(1);

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(film))));
    }

    @Test
    void shouldGetFilmById() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Film"))
                .andExpect(jsonPath("$.description").value("Film description"))
                .andExpect(jsonPath("$.releaseDate").value("2023-04-16"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void shouldUpdateFilm() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        Film updateFilm = Film.builder()
                .id(1)
                .name("updateFilm")
                .description("updateFilm description")
                .releaseDate(LocalDate.of(2021, 04, 16))
                .duration(Duration.ofSeconds(121))
                .mpa(RatingMPA.builder().id(2).name("PG").build())
                .build();

        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(updateFilm))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("updateFilm"))
                .andExpect(jsonPath("$.description").value("updateFilm description"))
                .andExpect(jsonPath("$.releaseDate").value("2021-04-16"))
                .andExpect(jsonPath("$.duration").value(121));
    }


    @Test
    void shouldBeAnErrorIfNameOfFilmNull() throws Exception {
        Film film = Film.builder()
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfNameOfFilmIsBlank() throws Exception {
        Film film = Film.builder()
                .name("")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfDescriptionMoreThan200Characters() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                        "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                        "1234567890123456789012345678901")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfReleaseDateIsFuture() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2033, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfReleaseDateIsBeforFirstFilm() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(1880, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfDurationIsNegative() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(-120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfBodyRequestIsEmpty() throws Exception {

        String invalidFileJSON = "{}";
        mockMvc.perform(post("http://localhost:8080/films")
                        .content(invalidFileJSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfFilmIdIsNotFound() throws Exception {

        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        Film updateFilm = Film.builder()
                .id(11)
                .name("updateFilm")
                .description("updateFilm description")
                .releaseDate(LocalDate.of(2021, 04, 16))
                .duration(Duration.ofSeconds(121))
                .mpa(RatingMPA.builder().id(2).name("PG").build())
                .build();

        mockMvc.perform(
                        put("/films")  //Проверка обновления фильма
                                .content(objectMapper.writeValueAsString(updateFilm))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateUser() throws Exception {
        java.util.Map<Long, String> friendMap = new HashMap<>();
        friendMap.put(2L, "newUser");
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        user.getMapFriends().put(2L, "newUser");


        mockMvc.perform(post("http://localhost:8080/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.login").value("UserLogin"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@email.ru"))
                .andExpect(jsonPath("$.birthday").value("2000-01-16"));
    }

    @Test
    void shouldBeUseLoginIfNameNull() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("UserLogin"));
    }

    @Test
    void shouldGetUser() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("http://localhost:8080/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.login").value("UserLogin"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@email.ru"))
                .andExpect(jsonPath("$.birthday").value("2000-01-16"));

        user.setId(1);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(user))));
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("http://localhost:8080/users")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON));

        user.setId(1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.login").value("UserLogin"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("User@email.ru"))
                .andExpect(jsonPath("$.birthday").value("2000-01-16"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("http://localhost:8080/users")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON));

        User updateUser = User.builder()
                .id(1)
                .login("updateUserLogin")
                .name("updateUser")
                .email("UpdateUser@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(updateUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.login").value("updateUserLogin"))
                .andExpect(jsonPath("$.name").value("updateUser"))
                .andExpect(jsonPath("$.email").value("UpdateUser@email.ru"))
                .andExpect(jsonPath("$.birthday").value("2001-01-16"));
    }

    @Test
    void shouldBeAdditionFriends() throws Exception {
        User user1 = User.builder()
                .login("UserLogin1")
                .name("User1")
                .email("User1@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = User.builder()
                .login("UserLogin2")
                .name("User2")
                .email("User2@email.ru")
                .birthday(LocalDate.of(2002, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/2")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friends").value(2));
        List<Long> longList = new ArrayList<>();

        mockMvc.perform(get("/users/2"))
                .andExpect(jsonPath("$.friends").value(longList));
    }

    @Test
    void shouldBeDeleteFriends() throws Exception {
        User user1 = User.builder()
                .login("UserLogin1")
                .name("User1")
                .email("User1@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = User.builder()
                .login("UserLogin2")
                .name("User2")
                .email("User2@email.ru")
                .birthday(LocalDate.of(2002, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/2")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/users/1/friends/2")
                        .content(objectMapper.writeValueAsString(user2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friends").isEmpty());

        mockMvc.perform(get("/users/2"))
                .andExpect(jsonPath("$.friends").isEmpty());
    }

    @Test
    void shouldGetListOfFriends() throws Exception {
        User user1 = User.builder()
                .login("UserLogin1")
                .name("User1")
                .email("User1@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON));

        user1.setId(1);

        User user2 = User.builder()
                .login("UserLogin2")
                .name("User2")
                .email("User2@email.ru")
                .birthday(LocalDate.of(2002, 01, 16))
                .build();


        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        user2.setId(2);

        User user3 = User.builder()
                .login("UserLogin3")
                .name("User3")
                .email("User3@email.ru")
                .birthday(LocalDate.of(2003, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

        user3.setId(3);

        mockMvc.perform(put("/users/1/friends/2")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

//        user2.addFriend(user1);
        user1.addFriend(user2);

        mockMvc.perform(put("/users/1/friends/3")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

//        user3.addFriend(user1);
        user1.addFriend(user3);

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(user2, user3))));
    }

    @Test
    void shouldGetListOfMutualFriends() throws Exception {
        User user1 = User.builder()
                .login("UserLogin1")
                .name("User1")
                .email("User1@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON));

        user1.setId(1);

        User user2 = User.builder()
                .login("UserLogin2")
                .name("User2")
                .email("User2@email.ru")
                .birthday(LocalDate.of(2002, 01, 16))
                .build();


        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        user2.setId(2);

        User user3 = User.builder()
                .login("UserLogin3")
                .name("User3")
                .email("User3@email.ru")
                .birthday(LocalDate.of(2003, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

        user3.setId(3);

        mockMvc.perform(put("/users/1/friends/2")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

//        user2.addFriend(user1);
        user1.addFriend(user2);

        mockMvc.perform(put("/users/1/friends/3")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

//        user3.addFriend(user1);
        user1.addFriend(user3);

        mockMvc.perform(put("/users/2/friends/3")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

//        user3.addFriend(user2);
        user2.addFriend(user3);

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(user3))));
    }

    @Test
    void shouldBeAnErrorIfLoginNull() throws Exception {
        User user = User.builder()
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfLoginIsBlank() throws Exception {
        User user = User.builder()
                .login("")
                .name("")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfLoginHaveWhitespace() throws Exception {
        User user = User.builder()
                .login(" UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfEmailInvalidFormat() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("UserEmail.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfEmailNull() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfEmailIsBlank() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfBirthdayIsFuture() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2030, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfUserIdNotFound() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2020, 01, 16))
                .build();

        mockMvc.perform(post("/users")  //Проверка создания фильма
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        User updateUser = User.builder()
                .id(11)
                .login("updateUserLogin")
                .name("updateUser")
                .email("UpdateUser@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(updateUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBeAdditionLike() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON));

        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/films/1/like/1")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Film"))
                .andExpect(jsonPath("$.description").value("Film description"))
                .andExpect(jsonPath("$.releaseDate").value("2023-04-16"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.likes").value(1));
    }

    @Test
    void shouldBeDeleteLike() throws Exception {
        User user = User.builder()
                .login("UserLogin")
                .name("User")
                .email("User@email.ru")
                .birthday(LocalDate.of(2000, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON));

        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        film.setId(1);

        mockMvc.perform(put("/films/1/like/1")
                .content(objectMapper.writeValueAsString(film))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/films/1/like/1")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(film)));
    }

    @Test
    void shouldBeGetPopularFilms() throws Exception {
        User user1 = User.builder()
                .login("UserLogin1")
                .name("User1")
                .email("User1@email.ru")
                .birthday(LocalDate.of(2001, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = User.builder()
                .login("UserLogin2")
                .name("User2")
                .email("User2@email.ru")
                .birthday(LocalDate.of(2002, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON));

        User user3 = User.builder()
                .login("UserLogin3")
                .name("User3")
                .email("User3@email.ru")
                .birthday(LocalDate.of(2003, 01, 16))
                .build();

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user3))
                .contentType(MediaType.APPLICATION_JSON));

        Film film1 = Film.builder()
                .name("Film1")
                .description("Film1 description")
                .releaseDate(LocalDate.of(2021, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON));

        film1.setId(1);

        Film film2 = Film.builder()
                .name("Film2")
                .description("Film2 description")
                .releaseDate(LocalDate.of(2022, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(2).name("PG").build())
                .build();

        mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON));

        film2.setId(2);

        Film film3 = Film.builder()
                .name("Film3")
                .description("Film3 description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
                .mpa(RatingMPA.builder().id(1).name("G").build())
                .build();

        mockMvc.perform(post("/films")
                .content(objectMapper.writeValueAsString(film3))
                .contentType(MediaType.APPLICATION_JSON));

        film3.setId(3);

        mockMvc.perform(put("/films/1/like/1")
                .content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON));

        film1.addLike(1);

        mockMvc.perform(put("/films/2/like/1")
                .content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON));

        film2.addLike(1);

        mockMvc.perform(put("/films/2/like/2")
                .content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON));

        film2.addLike(2);

        mockMvc.perform(put("/films/2/like/3")
                .content(objectMapper.writeValueAsString(film2))
                .contentType(MediaType.APPLICATION_JSON));

        film2.addLike(3);

        mockMvc.perform(put("/films/3/like/2")
                .content(objectMapper.writeValueAsString(film3))
                .contentType(MediaType.APPLICATION_JSON));

        film3.addLike(2);

        mockMvc.perform(put("/films/3/like/3")
                .content(objectMapper.writeValueAsString(film3))
                .contentType(MediaType.APPLICATION_JSON));

        film3.addLike(3);

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(film2, film3, film1))));

        mockMvc.perform(delete("/films/1/like/1")
                .content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON));

        film1.deleteLike(1);

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(film2, film3, film1))));
    }
}
