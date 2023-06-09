package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {
    @Autowired
    @Qualifier("filmDBStorage")
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public ResponseEntity<Film> addFilm(Film film) throws ValidationException {  //добавление фильма
        return filmStorage.addFilm(film);
    }

    public ResponseEntity<Film> updateFilm(Film film) throws ValidationException {  //обновление фильма
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {  //получение всех фильмов
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long filmId) {  //получение фильма по id
        return filmStorage.getFilmById(filmId);
    }

    public void clearFilmMap() {  //удаление всех фильмов
        filmStorage.clearFilmMap();
    }

    public Film addLike(long filmId, long userId) {  //добавление лайка
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(long filmId, long userId) {  //удаление лайка
        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getRatingOfFilms(Long number) throws ValidationException {  //вывод определённого колличества
        // фильмов из рейтинга
        return filmStorage.getRatingOfFilms(number);
    }

    public RatingMPA getRatingMPAById(Integer id) {  //получение рейтинга mpa по id
        return filmStorage.getRatingMPAById(id);
    }

    public List<RatingMPA> getAllRatingMPA() {  //вывод всех рейтингов mpa
        return filmStorage.getAllRatingMPA();
    }

    public Genre getGenreById(Integer id) {  //получение жанра по id
        return filmStorage.getGenreById(id);
    }

    public List<Genre> getAllGenre() {  //вывод всех жанров
        return filmStorage.getAllGenre();
    }
}
