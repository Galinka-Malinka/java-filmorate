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
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

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
                .build();

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
    void shouldUpdateFilm() throws Exception {
        Film film = Film.builder()
                .name("Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2023, 04, 16))
                .duration(Duration.ofSeconds(120))
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
                .build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAnErrorIfBodyRequestIsEmpty() throws Exception {
        Film film = Film.builder().build();

        mockMvc.perform(post("http://localhost:8080/films")
                        .content(objectMapper.writeValueAsString(film))
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
                .build();

        mockMvc.perform(
                        put("/films")  //Проверка обновления фильма
                                .content(objectMapper.writeValueAsString(updateFilm))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateUser() throws Exception {
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
}
