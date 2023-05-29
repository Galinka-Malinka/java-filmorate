package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.List;

public interface FilmStorage {

    public ResponseEntity<?> addFilm(Film film) throws ValidationException;  //добавление фильма

    public ResponseEntity<?> updateFilm(Film film) throws ValidationException;  //обновление фильма

    public List<Film> getAllFilms(); //получение всех фильмов

    public Film getFilmById(long filmId);  //получение фильма по id

    public void clearFilmMap();  //удаление всех фильмов

    public Film addLike(long filmId, long userId);  //добавление лайка

    public Film deleteLike(long filmId, long userId);  //удаление лайка

    public List<Film> getRatingOfFilms(Long number) throws ValidationException;  //вывод определённого колличества
    // фильмов из рейтинга

    public RatingMPA getRatingMPAById(Integer id);  //получение рейтинга mpa по id

    public List<RatingMPA> getAllRatingMPA();  //вывод всех рейтингов mpa

    public Genre getGenreById(Integer id);  //получение жанра по id

    public List<Genre> getAllGenre();  //вывод всех жанров
}
