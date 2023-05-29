package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component("filmDBStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override  //добавление фильма
    public ResponseEntity<?> addFilm(Film film) throws ValidationException {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895");
        }
        if (film.getDuration().isNegative()) {
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }

        Set<Genre> genresSet = new LinkedHashSet<>(film.getGenres());
        film.deleteAllGenres();
        for (Genre genre : genresSet) {
            String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
            Genre newGenre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genre.getId());
            film.addGenre(newGenre);
        }

        String sql = "insert into film (name, description, release_date, duration, rating_id) values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDurationInt());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        long filmId = keyHolder.getKey().longValue();

        film.setId(filmId);

        for (Genre genre : film.getGenres()) { // добавление информации о жанрах фильма в таблицу genre_of_film
            String sqlQuery = "insert into genre_of_film (film_id, genre_id) values(?, ?)";
            jdbcTemplate.update(sqlQuery, filmId, genre.getId());

            String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
            Genre newGenre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genre.getId());
            film.setNameGenre(newGenre);
        }

        String sqlForMpa = "select name from rating where rating_id =?";
        String mpa = jdbcTemplate.queryForObject(sqlForMpa, this::mapRowToNameMPA, film.getMpa().getId());
        film.getMpa().setName(mpa);

        log.debug("Добавление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @Override  //обновление фильма
    public ResponseEntity<?> updateFilm(Film film) throws ValidationException {
        try {
            String sqlForUserId = "select film_id from film where film_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdFilm, film.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }

        Set<Genre> genresSet = new LinkedHashSet<>(film.getGenres());
        film.deleteAllGenres();
        for (Genre genre : genresSet) {
            String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
            Genre newGenre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genre.getId());
            film.addGenre(newGenre);
        }

        String sql = "update film set " +
                "film_id = ?, name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "where film_id = ?";

        jdbcTemplate.update(sql,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        String sqlForDeleteAllGenres = "delete from genre_of_film where film_id = ?";
        jdbcTemplate.update(sqlForDeleteAllGenres, film.getId());

        for (Genre genre : film.getGenres()) {
            String sqlQuery = "insert into genre_of_film (film_id, genre_id) values(?, ?)";
            jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());

            String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
            Genre newGenre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genre.getId());
            film.setNameGenre(newGenre);
        }

        String sqlForMpa = "select name from rating where rating_id =?";
        String mpa = jdbcTemplate.queryForObject(sqlForMpa, this::mapRowToNameMPA, film.getMpa().getId());
        film.getMpa().setName(mpa);

        String sqlForLikes = "select user_id from likes where film_id = ?";
        List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, film.getId());

        for (Long likeId : listOfLikes) {
            film.addLike(likeId);
        }

        log.debug("Обновление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @Override  //получение всех фильмов
    public List<Film> getAllFilms() {

        String sql = "select * from film";
        List<Film> filmList = jdbcTemplate.query(sql, this::mapRowToFilm);

        for (Film film : filmList) {
            String sqlForGenreId = "select genre_id from genre_of_film where film_id = ?";
            List<Integer> genreList = jdbcTemplate.query(sqlForGenreId, this::mapRowToIDGenre, film.getId());

            if (!genreList.isEmpty()) {
                for (Integer genreId : genreList) {
                    String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
                    Genre genre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genreId);
                    film.addGenre(genre);
                }
            }
            String sqlForLikes = "select user_id from likes where film_id = ?";
            List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, film.getId());
            for (Long likeId : listOfLikes) {
                film.addLike(likeId);
            }
        }
        return filmList;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(Duration.ofSeconds(resultSet.getInt("duration")))
                .mpa(mapRowToMPA(resultSet.getString("rating_id")))
                .build();

        return film;
    }

    private RatingMPA mapRowToMPA(String id) throws SQLException {
        Integer mpaId = Integer.valueOf(id);
        String sql = "select name from rating where rating_id = ?";
        String nameMPA = jdbcTemplate.queryForObject(sql, this::mapRowToNameMPA, mpaId);

        return RatingMPA.builder()
                .id(mpaId)
                .name(nameMPA)
                .build();
    }

    private String mapRowToNameMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getString("name");
    }

    private Integer mapRowToIDGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getInt("genre_id");
    }

    private Long mapRowToIdFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("film_id");
    }

    private Long mapRowToIdUser(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getLong("user_id");
    }

    @Override  //получение фильма по id
    public Film getFilmById(long filmId) {
        try {
            String sqlForUserId = "select film_id from film where film_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdFilm, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }

        String sql = "select * from film where film_id = ?";
        Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, filmId);

        String sqlForGenreId = "select genre_id from genre_of_film where film_id = ?";
        List<Integer> genreList = jdbcTemplate.query(sqlForGenreId, this::mapRowToIDGenre, filmId);
        if (!genreList.isEmpty()) {
            for (Integer genreId : genreList) {
                String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
                Genre genre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genreId);
                film.addGenre(genre);
            }
        }

        String sqlForLikes = "select user_id from likes where film_id = ?";
        List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, filmId);
        for (Long likeId : listOfLikes) {
            film.addLike(likeId);
        }

        return film;
    }

    @Override  //удаление всех фильмов
    public void clearFilmMap() {
        String sql = "delete from film";
        jdbcTemplate.update(sql);

        String sqlForGenreOfFilm = "delete from genre_of_film";
        jdbcTemplate.update(sqlForGenreOfFilm);

        String sqlForLikes = "delete from likes";
        jdbcTemplate.update(sqlForLikes);
    }

    @Override   //добавление лайка
    public Film addLike(long filmId, long userId) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        try {
            String sqlForUserId = "select film_id from film where film_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdFilm, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }

        String sqlForFilm = "select * from film where film_id = ?";
        Film film = jdbcTemplate.queryForObject(sqlForFilm, this::mapRowToFilm, filmId);

        String sqlForGenreId = "select genre_id from genre_of_film where film_id = ?";
        List<Integer> genreList = jdbcTemplate.query(sqlForGenreId, this::mapRowToIDGenre, filmId);
        if (!genreList.isEmpty()) {
            for (Integer genreId : genreList) {
                String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
                Genre genre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genreId);
                film.addGenre(genre);
            }
        }

        String sqlForLikes = "select user_id from likes where film_id = ?";
        List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, filmId);
        for (Long likeId : listOfLikes) {
            film.addLike(likeId);
        }

        film.addLike(userId);

        String sql = "insert into likes (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        return film;
    }

    @Override   //удаление лайка
    public Film deleteLike(long filmId, long userId) {
        try {
            String sqlForUserId = "select user_id from \"user\" where user_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }

        try {
            String sqlForUserId = "select film_id from film where film_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIdFilm, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }

        String sqlForFilm = "select * from film where film_id = ?";
        Film film = jdbcTemplate.queryForObject(sqlForFilm, this::mapRowToFilm, filmId);

        String sqlForGenreId = "select genre_id from genre_of_film where film_id = ?";
        List<Integer> genreList = jdbcTemplate.query(sqlForGenreId, this::mapRowToIDGenre, filmId);
        if (!genreList.isEmpty()) {
            for (Integer genreId : genreList) {
                String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
                Genre genre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genreId);
                film.addGenre(genre);
            }
        }

        String sqlForLikes = "select user_id from likes where film_id = ?";
        List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, filmId);
        for (Long likeId : listOfLikes) {
            film.addLike(likeId);
        }

        film.deleteLike(userId);

        String sqlForDeleteLikes = "delete from likes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sqlForDeleteLikes, filmId, userId);
        return film;
    }


    @Override  //вывод определённого колличества фильмов из рейтинга
    public List<Film> getRatingOfFilms(Long number) throws ValidationException {
        if (number < 0) {
            throw new ValidationException("Параметр count не может быть отрицательным");
        }

        String sqlForRating = "(select film_id from likes group by film_id order by count(user_id) desc) limit ?";
        List<Long> listFilmID = jdbcTemplate.query(sqlForRating, this::mapRowToIdFilm, number);

        if (listFilmID.size() != number) {
            Long remainingNumber = number - listFilmID.size();
            String sqlForFilmWithoutRating = "select film_id from film where film_id not in "
                    + "(select film_id from likes) limit ?";
            List<Long> listOfFilmWithoutRating = jdbcTemplate.query(sqlForFilmWithoutRating, this::mapRowToIdFilm,
                    remainingNumber);
            listFilmID.addAll(listOfFilmWithoutRating);
        }

        List<Film> filmList = new ArrayList<>();
        for (Long filmId : listFilmID) {
            String sqlForFilm = "select * from film where film_id = ?";
            Film film = jdbcTemplate.queryForObject(sqlForFilm, this::mapRowToFilm, filmId);
            filmList.add(film);
        }

        for (Film film : filmList) {
            String sqlForGenreId = "select genre_id from genre_of_film where film_id = ?";
            List<Integer> genreList = jdbcTemplate.query(sqlForGenreId, this::mapRowToIDGenre, film.getId());
            if (!genreList.isEmpty()) {
                for (Integer genreId : genreList) {
                    String sqlForGenre = "select genre_id, name from genre where genre_id = ?";
                    Genre genre = jdbcTemplate.queryForObject(sqlForGenre, this::mapRowToGenre, genreId);
                    film.addGenre(genre);
                }
            }

            String sqlForLikes = "select user_id from likes where film_id = ?";
            List<Long> listOfLikes = jdbcTemplate.query(sqlForLikes, this::mapRowToIdUser, film.getId());
            for (Long likeId : listOfLikes) {
                film.addLike(likeId);
            }
        }
        return filmList;
    }

    @Override  //получение рейтинга mpa по id
    public RatingMPA getRatingMPAById(Integer id) {
        try {
            String sqlForUserId = "select rating_id, name from rating where rating_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToRatingMPA, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MPANotFoundException("mpa с таким id не существует");
        }

        String sql = "select rating_id, name from rating where rating_id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToRatingMPA, id);
    }

    @Override  //вывод всех рейтингов mpa
    public List<RatingMPA> getAllRatingMPA() {
        String sql = "select rating_id, name from rating";
        return jdbcTemplate.query(sql, this::mapRowToRatingMPA);
    }

    private RatingMPA mapRowToRatingMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return RatingMPA.builder()
                .id(resultSet.getInt("rating_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override   //получение жанра по id
    public Genre getGenreById(Integer id) {
        try {
            String sqlForUserId = "select genre_id from genre where genre_id = ?";
            jdbcTemplate.queryForObject(sqlForUserId, this::mapRowToIDGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Жанра с таким id не существует");
        }

        String sql = "select genre_id, name from genre where genre_id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
    }

    @Override  //вывод всех жанров
    public List<Genre> getAllGenre() {
        String sql = "select genre_id, name from genre";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
